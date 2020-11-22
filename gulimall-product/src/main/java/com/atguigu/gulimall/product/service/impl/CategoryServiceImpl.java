package com.atguigu.gulimall.product.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.CategoryBrandRelationService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.Catelog2Vo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //    @Autowired
    //    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<CategoryEntity> page = this.page(
                new Query<CategoryEntity>().getPage(params),
                new QueryWrapper<CategoryEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public List<CategoryEntity> listWithTree() {
        //1、查出所有分类
        List<CategoryEntity> categoryEntities = baseMapper.selectList(null);
//        System.out.println("查出所有分类" + categoryEntities.size());

        //2、组装成父子的树形结构

        //2.1）、找到所有的一级分类
        List<CategoryEntity> level1Menu =
                categoryEntities.stream()
                                .filter((categoryEntity -> categoryEntity.getParentCid() == 0))
                                .map((menu) -> {
                                    menu.setChildren(getChildren(menu, categoryEntities));
                                    return menu;
                                })
                                .sorted((menu1, menu2) -> {
                                    return (menu1.getSort() == null ? 0 : menu1.getSort()) -
                                            (menu2.getSort() == null ? 0 : menu2.getSort());
                                })
                                .collect(Collectors.toList());
        int total = level1Menu.size();
        int j = 0;
        for (CategoryEntity subLevels : level1Menu) {
//            System.out.println(j++ + "二级" + subLevels.getChildren().size());
            total += subLevels.getChildren().size();
            int i = 0;
            for (CategoryEntity subChildren : subLevels.getChildren()) {
                total += subChildren.getChildren().size();
//                System.out.println("    " + i++ + "    三级" + subChildren.getChildren().size());

            }
        }
//        System.out.println("total" + total);
        return level1Menu;
    }

    @Override
    public void removeMenuByIds(List<Long> asList) {
        // TODO 1、检查当前删除的菜单，是否被别的地方引用

        //逻辑删除
        baseMapper.deleteBatchIds(asList);
    }

    @Override
    public Long[] findCatelogPath(Long catelogId) {
        List<Long> paths = new ArrayList<>();

        List<Long> parentPath = findParentPath(catelogId, paths);
        Collections.reverse(parentPath);

        return parentPath.toArray(new Long[paths.size()]);
    }

    /**
     * 级联更新所有关联的数据
     *
     * @param category
     */
    @Transactional
    @Override
    public void updateCascade(CategoryEntity category) {
        this.updateById(category);
        categoryBrandRelationService.updateCategory(category.getCatId(), category.getName());
    }

    @Override
    public List<CategoryEntity> getLevel1Categorys() {
        long l = System.currentTimeMillis();
        List<CategoryEntity> categoryEntities = baseMapper.selectList(new QueryWrapper<CategoryEntity>().eq("parent_cid", 0));
        System.out.println("消耗的时间： " + (System.currentTimeMillis() - l));
        return categoryEntities;
    }

    // TODO 产生堆外内存溢出：OutOfDirectMemery
    // 1) SpringBoot2.0以后默认使用lettuce作为操作redis的客户端。它使用netty进行网络通信。
    // 2） lettuce的bug导致netty堆外内存溢出 -Xmx300m; netty如果没有指定堆外内存，默认使用-Xmx300m
    // 可以通过 maxDirectMemory 进行设置
    // 1) 升级lettuce客户端    2） 切换使用jedis
    // redisTemplate
    // lettuce/jedis 操作redis的底层客户端。Spring再次封装redisTemplate

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {
        // 给缓存中放json字符串，拿出的json字符串，还用逆转为能用的对象类型；【序列化与反序列化】

        /**
         * 1. 空结果缓存，解决缓存穿透
         * 2。 设置过期时间（加随机值），解决缓存雪崩
         * 3。 加锁： 解决缓存击穿
         */

        // 1。 加入缓存逻辑，缓存中存的数据是json字符串
        // JSON跨语言，跨平台兼容。
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (StringUtils.isEmpty(catelogJSON) || catelogJSON == null) {
            // 2. 缓存中没有，查询数据库
            System.out.println("redis cache do not have... query database....");
            Map<String, List<Catelog2Vo>> catelogJsonFromDb = getCatelogJsonFromDbWithRedisLock();


            return catelogJsonFromDb;
        }

        System.out.println("redis cache have...return directly....");
        // 转为我们指定的对象
        Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
        });
        return result;

    }

    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithRedisLock() {

        // 1。 占分布式锁，去redis占坑
        String uuid = UUID.randomUUID().toString();
        Boolean lock = stringRedisTemplate.opsForValue().setIfAbsent("lock", uuid, 300, TimeUnit.SECONDS);
        if (lock) {
            System.out.println("获取分布式锁成功");
            // 加锁成功。。。执行业务
            // 2。 设置过期时间,必须和加锁是同步的，原子的
            // stringRedisTemplate.expire("lock", 30, TimeUnit.SECONDS);
            Map<String, List<Catelog2Vo>> dataFromDb;
            try {
                dataFromDb = getDataFromDb();
            } finally {
                String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
                // 删除锁
                Long lock1 = stringRedisTemplate.execute(new DefaultRedisScript<Long>(script, Long.class), Arrays.asList("lock"), uuid);
            }

            // 获取值对比+删除锁=原子脚本 Lua 脚本解锁
//            String lockValue = stringRedisTemplate.opsForValue().get("lock");
//            if (uuid.equals(lockValue)) {
//                // 删除自己的锁
//                stringRedisTemplate.delete("lock"); // delete lock
//            }

            return dataFromDb;
        } else {
            // 加锁失败。。。重试。
            // 休眠100ms重试
            System.out.println("获取分布式锁不成功，等待重试");
            try {
                Thread.sleep(200);
            } catch (Exception e) {

            }
            return getCatelogJsonFromDbWithRedisLock(); // self spin
        }
    }

    private Map<String, List<Catelog2Vo>> getDataFromDb() {
        String catelogJSON = stringRedisTemplate.opsForValue().get("catelogJSON");
        if (!StringUtils.isEmpty(catelogJSON)) {
            // 缓存不为null直接返回
            Map<String, List<Catelog2Vo>> result = JSON.parseObject(catelogJSON, new TypeReference<Map<String, List<Catelog2Vo>>>() {
            });
            return result;
        }
        System.out.println("Already query database......");


        List<CategoryEntity> selectList = baseMapper.selectList(null);


        List<CategoryEntity> level1Categorys = getParent_cid(selectList, 0L); // getLevel1Categorys();

        // 封装数据
        Map<String, List<Catelog2Vo>> parent_cid = level1Categorys.stream()
                                                                  .collect(Collectors.toMap(k -> k.getCatId()
                                                                                                  .toString(), v -> {
                                                                      // 1. 每一个的一级分类，查到这个一级分类的二级分类
                                                                      List<CategoryEntity> categoryEntities = getParent_cid(
                                                                              selectList, v.getCatId());

                                                                      // 2. 封装上面的结果
                                                                      List<Catelog2Vo> catelog2Vos = null;
                                                                      if (categoryEntities != null) {
                                                                          catelog2Vos = categoryEntities.stream()
                                                                                                        .map(l2 -> {
                                                                                                            Catelog2Vo catelog2Vo = new Catelog2Vo(
                                                                                                                    v.getCatId()
                                                                                                                     .toString(),
                                                                                                                    null,
                                                                                                                    l2.getCatId()
                                                                                                                      .toString(),
                                                                                                                    l2.getName());
                                                                                                            // 1. 找当前二级分类的三级分类封装成vo
                                                                                                            List<CategoryEntity> level3Catelog = getParent_cid(
                                                                                                                    selectList, l2.getCatId());

                                                                                                            if (level3Catelog != null) {
                                                                                                                List<Catelog2Vo.Catelog3Vo> collect = level3Catelog
                                                                                                                        .stream()
                                                                                                                        .map(l3 -> {
                                                                                                                            // 2. 封装成指定格式
                                                                                                                            Catelog2Vo.Catelog3Vo catelog3Vo = new Catelog2Vo.Catelog3Vo(
                                                                                                                                    l2.getCatId()
                                                                                                                                      .toString(),
                                                                                                                                    l3.getCatId()
                                                                                                                                      .toString(),
                                                                                                                                    l3.getName());
                                                                                                                            return catelog3Vo;
                                                                                                                        })
                                                                                                                        .collect(
                                                                                                                                Collectors
                                                                                                                                        .toList());
                                                                                                                catelog2Vo.setCatalog3List(collect);

                                                                                                            }


                                                                                                            return catelog2Vo;
                                                                                                        })
                                                                                                        .collect(
                                                                                                                Collectors
                                                                                                                        .toList());

                                                                      }
                                                                      return catelog2Vos;
                                                                  }));
        // 3。 查到的数据再放入缓存，将对象转为json放在缓存中
        String s = JSON.toJSONString(parent_cid);
        stringRedisTemplate.opsForValue().set("catelogJSON", s, 1, TimeUnit.DAYS);
        return parent_cid;
    }

    // 从数据库查询并封装分类数据
    public Map<String, List<Catelog2Vo>> getCatelogJsonFromDbWithLocalLock() {

        // 只要是同一把锁，就能锁住需要这个锁的所有线程
        // 1。 synchronized (this)： SpringBoot所有的组件在容器中都是单例的。
        // TODO 本地锁： synchronized, JUC(Lock),在分布式情况下，想要锁住所有，必须使用分布式锁
        // 2。

        synchronized (this) {
            // 得到锁以后，我们应该再去缓存中再确定一次，如果没有才需要继续查询
            return getDataFromDb();
        }

    }

    private List<CategoryEntity> getParent_cid(List<CategoryEntity> selectList, Long parent_cid) {
        List<CategoryEntity> collect = selectList.stream().filter(item -> {
            return item.getParentCid() == parent_cid;
        }).collect(Collectors.toList());

        return collect;
        //        return baseMapper
//                .selectList(new QueryWrapper<CategoryEntity>()
//                                    .eq(
//                                            "parent_cid",
//                                            v.getCatId()));
    }

    private List<Long> findParentPath(Long catelogId, List<Long> paths) {
        // 收集当前节点id
        paths.add(catelogId);
        CategoryEntity byId = this.getById(catelogId);
        if (byId.getParentCid() != 0) {
            findParentPath(byId.getParentCid(), paths);
        }
        return paths;
    }

    //递归查找所有菜单的子菜单
    private List<CategoryEntity> getChildren(CategoryEntity root, List<CategoryEntity> all) {
//        if (Long.compare(root.getCatId(), 128) == 0) System.out.println(root.getChildren());
        List<CategoryEntity> children = all.stream()
                                           .filter(categoryEntity -> {
                                               return Long.compare(categoryEntity.getParentCid(), root.getCatId()) == 0;
                                           })
                                           .map(categoryEntity -> {
                                               categoryEntity.setChildren(getChildren(categoryEntity, all));
                                               return categoryEntity;
                                           })
                                           .sorted((menu1, menu2) -> {
                                               return (menu1.getSort() == null ? 0 : menu1.getSort()) -
                                                       (menu2.getSort() == null ? 0 : menu2.getSort());
                                           })
                                           .collect(Collectors.toList());
//        if (Long.compare(root.getCatId(), 128) == 0) System.out.println(root.getChildren());
        return children;
    }

}