package com.atguigu.common.to.es;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;


/**
 * "properties": {
 *       "skuId": {
 *         "type": "long"
 *       },
 *       "spuId": {
 *         "type": "keyword"
 *       },
 *       "skuTitle": {
 *         "type": "text",
 *         "analyzer": "ik_smart"
 *       },
 *       "skuPrice": {
 *         "type": "keyword"
 *       },
 *       "skuImg": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "saleCount": {
 *         "type": "long"
 *       },
 *       "hasStock": {
 *         "type": "boolean"
 *       },
 *       "hotScore": {
 *         "type": "long"
 *       },
 *       "brandId": {
 *         "type": "long"
 *       },
 *       "catelogId": {
 *         "type": "long"
 *       },
 *       "brandName": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "brandImg": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "catelogName": {
 *         "type": "keyword",
 *         "index": false,
 *         "doc_values": false
 *       },
 *       "attrs": {
 *         "type": "nested", ##嵌套,防止数组扁平化
 *         "properties": {
 *           "attrId": {
 *             "type": "long"
 *           },
 *           "attrName": {
 *             "type": "keyword",
 *             "index": false,
 *             "doc_values": false
 *           },
 *           "attrValue": {
 *             "type": "keyword"
 *           }
 *         }
 *       }
 *     }
 */


@Data
public class SkuEsModel {
    //商品ID
    private Long spuId;
    //sku_id
    private Long skuId;
    //标题
    private String skuTitle;
    //价格
    private BigDecimal skuPrice;
    //图片
    private String skuImg;
    //销售量
    private Long saleCount;
    //是否还有库存
    private Boolean hasStock;
    //热度评分
    private Long hotScore;
    //品牌ID
    private Long brandId;
    //品牌名
    private String brandName;
    //品牌图片
    private String brandImg;
    //分类ID
    private Long catalogId;
    //分类名
    private String catalogName;
    //属性
    private List<AttrsEsModel> attrs;


    @Data
    public static class AttrsEsModel {
        //属性ID
        private Long attrId;
        //属性名
        private String attrName;
        //属性值
        private String attrValue;
    }
}
