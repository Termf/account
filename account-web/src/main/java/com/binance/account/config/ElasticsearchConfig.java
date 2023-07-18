package com.binance.account.config;

import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.EC2ContainerCredentialsProviderWrapper;
import com.binance.platform.env.EnvUtil;
import lombok.extern.log4j.Log4j2;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequestInterceptor;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import vc.inreach.aws.request.AWSSigner;
import vc.inreach.aws.request.AWSSigningRequestInterceptor;

import java.time.LocalDateTime;
import java.time.ZoneOffset;

/**
 * Elasticsearch config
 * Created by Shining.Cai on 2018/09/20.
 **/
@Log4j2
@Configuration
public class ElasticsearchConfig {

    @Value("${amazon.es.host:}")
    private String host;
    @Value("${amazon.es.accesskey:null}")
    private String accessKeyId;
    @Value("${amazon.es.secretKey:null}")
    private String secretKey;
    @Value("${amazon.es.region:null}")
    private String region;


    @Bean
    @ConditionalOnProperty(prefix = "amazon.es", name = "host")
    public RestClient restClient(){
        AWSCredentialsProvider credentialsProvider;
        if (EnvUtil.isDev()){
            // 本地使用ak、sk授权访问
            log.info("ElasticsearchConfig.. use secretKey-secretKey");
            credentialsProvider = new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey));
        }else {
            // dev、qa、prod使用EC2 role方式访问
            log.info("ElasticsearchConfig.. use EC2ContainerCredentialsProviderWrapper");
            credentialsProvider = new EC2ContainerCredentialsProviderWrapper();
        }

        AWSSigner awsSigner = new AWSSigner(credentialsProvider, region, "es", () -> LocalDateTime.now(ZoneOffset.UTC));
        HttpRequestInterceptor interceptor = new AWSSigningRequestInterceptor(awsSigner);

        return RestClient.builder(HttpHost.create(host))
                .setRequestConfigCallback(requestConfigBuilder -> requestConfigBuilder.setConnectTimeout(2000).setSocketTimeout(10000))
                .setMaxRetryTimeoutMillis(30000).setHttpClientConfigCallback(hcb -> hcb.addInterceptorLast(interceptor)).build();
    }
}
