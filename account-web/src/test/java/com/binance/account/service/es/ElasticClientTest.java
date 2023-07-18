
package com.binance.account.service.es;

import com.alibaba.fastjson.JSON;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.binance.account.common.query.es.ESQueryBuilder;
import com.binance.account.common.query.es.ESQueryCondition;
import com.binance.account.common.query.es.ESSortCondition;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.HttpAsyncResponseConsumerFactory;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.StopWatch;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
* Created by Shining.Cai on 09/19/2018.
*/

public class ElasticClientTest {

    private static final RequestOptions COMMON_OPTIONS;
    static {
        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();
        builder.addHeader("Authorization", "Bearer ");
        builder.setHttpAsyncResponseConsumerFactory(
                new HttpAsyncResponseConsumerFactory.HeapBufferedResponseConsumerFactory(30 * 1024 * 1024 * 1024));
        COMMON_OPTIONS = builder.build();
    }


    @Before
    public void before() throws Exception {
    }

    @After
    public void after() throws Exception {
    }

    /**
    *
    * Method: search()
    *
    */
    @Test
    public void testSearch() throws Exception {


        String host = "https://search-logs-dev-xxfq5zobaskreufckhdt7mlmvy.ap-northeast-1.es.amazonaws.com";
        String accessKeyId = "AKIAJV6YR3E7WDTM23UA";
        String secretKey = "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";

        String region = "ap-northeast-1";
        String serviceName = "es";

        RequestOptions.Builder builder = RequestOptions.DEFAULT.toBuilder();



        AWSCredentialsProvider credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey));
        AWSSigner awsSigner = new AWSSigner(credentialsProvider, region, serviceName, () -> LocalDateTime.now(ZoneOffset.UTC));
        HttpRequestInterceptor interceptor = new AWSSigningRequestInterceptor(awsSigner);

        RestClientBuilder clientBuilder = RestClient.builder(HttpHost.create(host))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(5000).setSocketTimeout(60000))
                .setMaxRetryTimeoutMillis(60000).setHttpClientConfigCallback(hcb -> hcb.addInterceptorLast(interceptor));

        RestClient restClient = clientBuilder.build();

        StopWatch watch = new StopWatch();
        watch.start();
        Request request = new Request("GET", "/device_search/device/_search");
//        request.addParameter("pretty", "true");

        Map params = ESQueryBuilder.instance().must(ESQueryCondition.term("user_id", "10000018"))
                .sort(ESSortCondition.desc("active_time")).limit(0, 100).build();

//        Map<String, Object> params = new HashMap<>();
//        params.put("query", ImmutableMap.of("term", ImmutableMap.of("device_id", "107e9d0e-a32c-4927-a9c6-4e11ff6baf6a")));

        request.setEntity(new NStringEntity(JSON.toJSONString(params), ContentType.APPLICATION_JSON));
        request.setOptions(builder.build());

        Response response = restClient.performRequest(request);
        System.out.println(EntityUtils.toString(response.getEntity()));
        watch.stop();
        System.out.println("cost: "+ watch.getLastTaskTimeMillis());
    }


}
