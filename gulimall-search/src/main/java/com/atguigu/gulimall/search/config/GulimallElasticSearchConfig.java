package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Configuration;

/**
 * 1. Import dependency
 * 2. Configure, Inject a "RestHighLevelClient" to bean
 * 3. Look up Official API
 */
@Configuration
public class GulimallElasticSearchConfig {

    public RestHighLevelClient esRestClient() {

        RestClientBuilder builder = null;
        // String hostname, int port, String scheme
        builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        RestHighLevelClient client = new RestHighLevelClient(builder);

//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http")));
        return client;
    }
}
