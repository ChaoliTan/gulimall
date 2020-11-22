package com.atguigu.gulimall.product.service.impl;

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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service("categoryService")
public class CategoryServiceImpl extends ServiceImpl<CategoryDao, CategoryEntity> implements CategoryService {

    //    @Autowired
    //    CategoryDao categoryDao;

    @Autowired
    CategoryBrandRelationService categoryBrandRelationService;

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

    @Override
    public Map<String, List<Catelog2Vo>> getCatelogJson() {

        /**
         * 1. 将数据库的多次查询变为一次
         */
        List<CategoryEntity> selectList = baseMapper.selectList(null);

        // 1. 查出所有分类
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
        return parent_cid;
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
                                              return Long.compare(categoryEntity.getParentCid(), root.getCatId()) == 0;})
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