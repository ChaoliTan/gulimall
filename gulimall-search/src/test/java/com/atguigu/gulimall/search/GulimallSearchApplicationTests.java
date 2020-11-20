package com.atguigu.gulimall.search;

import com.alibaba.fastjson.JSON;
import com.atguigu.gulimall.search.config.GulimallElasticSearchConfig;
import lombok.Data;
import lombok.ToString;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.Aggregations;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.aggregations.bucket.terms.TermsAggregationBuilder;
import org.elasticsearch.search.aggregations.metrics.Avg;
import org.elasticsearch.search.aggregations.metrics.AvgAggregationBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GulimallSearchApplicationTests {

    @Autowired
    private RestHighLevelClient client;

    @ToString
    @Data
    static class Account {

        private int account_number;
        private int balance;
        private String firstname;
        private String lastname;
        private int age;
        private String gender;
        private String address;
        private String employer;
        private String email;
        private String city;
        private String state;
    }

    /**
     * 1. Easy to search {
     *     skuId: 1
     *     spuId: 11
     *     skuTitle: huaweiXX
     *     price: 998
     *     saleCount:99
     *     attrs:[
     *          {},
     *          {},
     *          {}
     *          ]
     * }
     *
     * 冗余：
     *  100 million * 20 = 100 million * 2kb = 2G
     * 2.
     *     sku index {
     *         skuId: 1
     *         spuId: 11
     *         xxxx
     *     }
     *
     *     attr index {
     *          spuId: 11
     *          attrs:[
     *          {}
     *          {}
     *          {}
     *          ]
     *     }
     *
     *     Search xiaomi: food, cell, applicances
     *     10 000, 4 000 spu
     *     分步，4 000 spu对应对所有可能属性
     *     esClient： spuId: [4000 spuid] 4000*8=32000byte=32kb
     *
     *     32kb*10000=320mb
     *
     *
     * @throws IOException
     */

    @Test
    public void searchData() throws IOException {
        // 1. Create search request
        SearchRequest searchRequest = new SearchRequest();
        // Designate index
        searchRequest.indices("bank");
        // Designate DSL (Domain Specific Language), Search Conditions
        // SearchSourceBuilder sourceBuilder Encapsulate condition
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        // 1.1 Create search conditions
        sourceBuilder.query(QueryBuilders.matchQuery("address", "mill"));
//        sourceBuilder.from();
//        sourceBuilder.size();
//        sourceBuilder.aggregation();

        // 1.2
        TermsAggregationBuilder ageAgg = AggregationBuilders.terms("ageAgg").field("age").size(10);
        sourceBuilder.aggregation(ageAgg);

        // 1.3
        AvgAggregationBuilder balanceAve = AggregationBuilders.avg("balanceAvg").field("balance");
        sourceBuilder.aggregation(balanceAve);

        searchRequest.source(sourceBuilder);

        // 2. Execute search
        SearchResponse searchResponse = client.search(searchRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // 3. Analyse searchResponse
        System.out.println(searchResponse.toString());
//        JSON.parseObject(searchResponse.toString(), Map.class);

        // 3.1 Get all hits
        SearchHits totalHits = searchResponse.getHits();
        SearchHit[] hits = totalHits.getHits();
        for (SearchHit hit : hits) {
//            hit.getIndex();hit.getType();hit.getId();
            String string = hit.getSourceAsString();
            Account account = JSON.parseObject(string, Account.class);
            System.out.println("account:"+account);
        }

        // 3.2 Get nested aggregation data
        Aggregations aggregations = searchResponse.getAggregations();

//        for (Aggregation aggregation : aggregations.asList()) {
//            aggregation.getName();
//            System.out.println("current aggregation's name: " + aggregation.getName());
//        }
        Terms ageAgg1 = aggregations.get("ageAgg");
        for (Terms.Bucket bucket : ageAgg1.getBuckets()) {
            String keyAsString = bucket.getKeyAsString();
            System.out.println("age: " + keyAsString + "==>" + bucket.getDocCount());
        }

        Avg balanceAve1 = aggregations.get("balanceAvg");
        System.out.println("Average salary: "+balanceAve1.getValue());
    }
    /**
     * Test index data to es
     * Update works as well
     */
    @Test
    public void indexData() throws IOException {
        IndexRequest indexRequest = new IndexRequest("users");
        indexRequest.id("1");
//        indexRequest.source("userName", "John", "age", "18", "gender", "M");
        User user = new User();
        user.setUserName("John");
        user.setAge(18);
        user.setGender("M");

        String jsonString = JSON.toJSONString(user);
        indexRequest.source(jsonString, XContentType.JSON); // Content to save

        // Execute
        IndexResponse index = client.index(indexRequest, GulimallElasticSearchConfig.COMMON_OPTIONS);

        // Extract response data
        System.out.println(index);
    }

    @Data
    class User {
        private String userName;
        private String gender;
        private Integer age;
    }

    @Test
    public void contextLoads() {
        System.out.println(client);
    }

}
