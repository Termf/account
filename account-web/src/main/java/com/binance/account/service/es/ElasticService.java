package com.binance.account.service.es;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.query.es.ESResultSet;
import com.binance.master.error.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.javasimon.aop.Monitored;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;

/**
 * Elasticsearch相关服务
 * Created by Shining.Cai on 2018/09/20.
 **/
@Monitored
@Log4j2
@Component
public class ElasticService {

    @Autowired(required = false)
    private RestClient restClient;

    public ESResultSet search(String endpoint, Map<String, Object> params){

        try {
            Request request = new Request("GET", endpoint);
            request.setEntity(new NStringEntity(JSON.toJSONString(params), ContentType.APPLICATION_JSON));

            Response response = restClient.performRequest(request);
            if (response.getStatusLine().getStatusCode() == 200){
                return JSON.parseObject(EntityUtils.toString(response.getEntity())).getObject("hits", ESResultSet.class);
            } else {
                log.warn("ElasticService.search failed, {}-{}, {}", endpoint, params, EntityUtils.toString(response.getEntity()));
                throw new BusinessException("search failed");
            }
        } catch (IOException e) {
            log.warn("ElasticService.search error, {}-{}, {}", endpoint, params, e);
            throw new BusinessException("search error:"+e.getMessage());
        }
    }

}
