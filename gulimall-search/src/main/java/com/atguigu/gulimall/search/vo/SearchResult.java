package com.atguigu.gulimall.search.vo;

import com.atguigu.common.to.es.SkuEsModel;
import lombok.Data;

import java.util.List;

@Data
public class SearchResult {

    // 查询到的所有商品信息
    private List<SkuEsModel> products;

    /**
     * 分页信息
     */
    private Integer pageNum; // 当前页面
    private Long total; // 总记录数
    private Integer totalPages; // 总页码
    //导航页
    private List<Integer> pageNavs;

    private List<BrandVo> brands; // 当前查询到的结果，所有设计到的品牌
    private List<CatelogVo> catelogs; // 当前查询到的结果，所有设计到的所有分类
    private List<AttrVo> attrs; // 当前查询到的结果，所有设计到的属性

    // ============以上是需要返给页面的所有信息=============

    //面包屑导航
    private List<NavVo> navs;

    @Data
    public static class NavVo{
        private String navName;
        private String navValue;
        private String link;
    }

    @Data
    public static class BrandVo{
        //品牌ID
        private Long brandId;
        //品牌名
        private String brandName;
        //品牌图片
        private String brandImg;
    }

    @Data
    public static class AttrVo{
        private Long attrId;
        private String attrName;
        private List<String> attrValue;
    }

    @Data
    public static class CatelogVo{
        //分类ID
        private Long catalogId;
        //分类名
        private String catalogName;
    }
}
