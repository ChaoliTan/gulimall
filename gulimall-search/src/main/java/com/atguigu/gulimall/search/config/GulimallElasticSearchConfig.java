package com.atguigu.gulimall.search.config;

import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 1. Import dependency
 * 2. Configure, Inject a "RestHighLevelClient" to bean
 * 3. Look up Official API https://www.elastic.co/guide/en/elasticsearch/client/java-rest/current/java-rest-high.html
 */
@Configuration
public class GulimallElasticSearchConfig {

    public static final RequestOptions COMMON_OPTIONS;
    final CredentialsProvider credentialsProvider =
            new BasicCredentialsProvider();
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
//        builder.addHeader("Authorization", "Bearer " + TOKEN);
//        builder.setHttpAsyncResponseConsumerFactory(
//                new HttpAsyncResponseConsumerFactory
//                        .HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }

    @Bean
    public RestHighLevelClient esRestClient() {


        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials("elastic", "changeme"));

        RestClientBuilder builder = null;
        // String hostname, int port, String scheme
        builder = RestClient.builder(new HttpHost("localhost", 9200, "http"));
        builder.setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                                                @Override
                                                public HttpAsyncClientBuilder customizeHttpClient(
                                                        HttpAsyncClientBuilder httpClientBuilder) {
                                                    return httpClientBuilder
                                                            .setDefaultCredentialsProvider(credentialsProvider);
                                                }
                                            });
        RestHighLevelClient client = new RestHighLevelClient(builder);

//        RestHighLevelClient client = new RestHighLevelClient(
//                RestClient.builder(
//                        new HttpHost("localhost", 9200, "http")));
        return client;
    }
}
