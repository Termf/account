package com.binance.account.service.device.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.es.ESQueryBuilder;
import com.binance.account.common.query.es.ESQueryCondition;
import com.binance.account.common.query.es.ESResultSet;
import com.binance.account.common.query.es.ESSortCondition;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.service.es.ElasticService;
import com.binance.master.utils.StringUtils;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.google.common.base.CaseFormat;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * user device 搜索相关
 * Created by Shining.Cai on 2018/09/20.
 **/
@Log4j2
@Service
public class UserDeviceSearchService {

    @Autowired
    private ElasticService elasticService;
    @Autowired
    private SysConfigVarCacheService configVarCacheService;

    private static final String DEVICE_ES_SWITCH = "device_elasticsearch_switch";
    private static final String DEVICE_ES_PAGE_SIZE = "device_elasticsearch_page_size";

    private Field[] fields = UserDevice.class.getDeclaredFields();

    public boolean isSearchSwitchOn(){
        return configVarCacheService.getBoolean(DEVICE_ES_SWITCH);
    }

    public SearchResult<UserDevice> listDevice(Long userId, String agentType, String source, boolean showDeleted, String searchParams){

        ESQueryBuilder builder = ESQueryBuilder.instance()
                .must(parseConditions(searchParams))
                .must(ESQueryCondition.term("user_id", userId), ESQueryCondition.term("agent_type", agentType));
        if (!showDeleted){
            builder.must(ESQueryCondition.term("is_del", 0));
        }
        if (StringUtils.isNotBlank(source)){
            builder.must(ESQueryCondition.term("source", source));
        }
        builder.sort(ESSortCondition.desc("active_time"));

        // 每页数据量
        Integer pageSize = configVarCacheService.getInteger(DEVICE_ES_PAGE_SIZE);
        if (pageSize==null || pageSize<=0){
            pageSize=100;
        }
        builder.limit(0, pageSize);

        Map<String, Object> paramsMap = builder.build();
        ESResultSet resultSet = elasticService.search("/device_search/device/_search", paramsMap);

        return parseResults(resultSet);
    }

    /**
     * 根据设备id查询设备
     */
    public List<UserDevice> searchDeviceByDeviceId(String deviceId){
        ESQueryBuilder builder = ESQueryBuilder.instance()
                .must(ESQueryCondition.term("device_id", deviceId))
                .limit(0, 20);
        ESResultSet resultSet = elasticService.search("/device_search/device/_search", builder.build());
        return parseResults(resultSet).getRows();
    }




    public List<ESQueryCondition> parseConditions(String searchParams){
        if (StringUtils.isEmpty(searchParams)){
            return Collections.emptyList();
        }
        JSONObject object = JSON.parseObject(searchParams);
        List<ESQueryCondition> conditions = new ArrayList<>();
        object.forEach((k,v)->{
            if (k!=null && v!=null){
                conditions.add(ESQueryCondition.wildcard(k, v));
            }
        });
        return conditions;
    }

    public SearchResult<UserDevice> parseResults(ESResultSet resultSet){
        List<UserDevice> devices = new ArrayList<>(resultSet.getHits().size());
        resultSet.getHits().toJavaList(JSONObject.class).forEach(hit->{
            // 获取原始数据
            JSONObject source = hit.getJSONObject("_source");
            UserDevice device = new UserDevice();

            //将json中的下划线数据转换为对象的值
            this.stealFieldFromJson(device, source);
            device.setContent(source.toJSONString());

            devices.add(device);
        });
        return new SearchResult<>(devices, resultSet.getTotal());
    }

    private void stealFieldFromJson(UserDevice device, JSONObject map) {
        try {
            for (Field field:fields){
                String key = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, field.getName());
                if (map.containsKey(key)){
                    boolean accessible = field.isAccessible();
                    if (!accessible){
                        field.setAccessible(true);
                    }
                    if ("status".equals(key)) {
                        Object status = map.get(key);
                        if (status != null) {
                            try {
                                Integer statusOrdinal = Integer.valueOf(status.toString());
                                field.set(device, UserDevice.Status.valueOfOrdinal(statusOrdinal));
                            } catch (Exception e) {
                                log.warn("device status format error: {}", status);
                            }
                        }
                    } else {
                        field.set(device, map.getObject(key, field.getType()));
                    }
                    map.remove(key);
                }
            }
        } catch (IllegalAccessException e) {
            log.error("stealFieldFromJson error", e);
        }
    }

}
