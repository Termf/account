package com.binance.account.config;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.datasource.apollo.ApolloDataSource;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
public class SentinelConfig {
    @Value("${sentinel.flow.defaultCount:1000}")
    private double defaultFlowCount;
    //并发线程数
    @Value("${sentinel.flow.defaultGrade:1}")
    private int defaultFlowGrade;
    //直接拒绝;Warm Up;均速排队
    @Value("${sentinel.flow.controlBehavior:0}")
    private int defaultFlowControlBehavior;

     /**
      * 默认策略为相应平均时间超过10秒就熔断20秒
      * */
    //超过10000ms=10s
    @Value("${sentinel.degrade.defaultCount:10000}")
    private double defaultDegradeCount;
    //按照RT
    @Value("${sentinel.degrade.defaultGrade:0}")
    private int defaultDegradeGrade;
    //时间为20s窗口
    @Value("${sentinel.degrade.timeWindow:20}")
    private int defaultDegradeTimeWindow;


    // 定义热点限流的规则，对第一个参数设置 qps 限流模式，阈值为100
    @Value("${sentinel.paramFlow.defaultCount:100}")
    private double defaultParamFlowCount;
    //QPS模式
    @Value("${sentinel.paramFlow.defaultGrade:1}")
    private int defaultParamFlowGrade;
    //对第一个参数设置
    @Value("${sentinel.paramFlow.paramIdx:0}")
    private int defaultParamFlowIdx;


    private String namespaceName = "application";
    @PostConstruct
    public void loadRuleManager(){
        loadFlowManager();
        loadDegradeRuleManager();
        loadParamFlowRuleManager();
    }


    /**
     * 限流规则
     */
    private void loadFlowManager(){
        // 限流规则的Key, 在Apollo中用此Key
        String flowRuleKey = "sentinel.flowRules";
        // 限流规则的默认值
        String defaultFlowRules = "[]";
        // 注册数据源
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ApolloDataSource<>(namespaceName,
                flowRuleKey, defaultFlowRules, source -> {
            JSONArray jsonArray = JSON.parseArray(source);
            JSONArray newJsonArray = jsonArray.stream().map(b -> {
                JSONObject jsonObj = (JSONObject) b;
                jsonObj.put("grade", null == jsonObj.getInteger("grade") ? defaultFlowGrade : jsonObj.getInteger("grade"));
                jsonObj.put("count", null == jsonObj.getDouble("count") ? defaultFlowCount : jsonObj.getDouble("count"));
                jsonObj.put("controlBehavior", null == jsonObj.getInteger("controlBehavior") ? defaultFlowControlBehavior : jsonObj.getInteger("controlBehavior"));
                return jsonObj;
            }).collect(Collectors.toCollection(JSONArray::new));
            List<FlowRule> flowRuleList =  newJsonArray.toJavaList(FlowRule.class);
            return flowRuleList;
        });
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());
    }

    /**
     * 熔断规则
     */
    private void loadDegradeRuleManager(){
        // 熔断规则的Key, 在Apollo中用此Key
        String flowRuleKey = "sentinel.degradeRules";
        // 熔断规则的默认值
        String defaultFlowRules = "[]";
        // 注册数据源
        ReadableDataSource<String, List<DegradeRule>> degradeRuleDataSource = new ApolloDataSource<>(namespaceName,
                flowRuleKey, defaultFlowRules, source -> {
            JSONArray jsonArray = JSON.parseArray(source);
            JSONArray newJsonArray = jsonArray.stream().map(b -> {
                JSONObject jsonObj = (JSONObject) b;
                jsonObj.put("grade", null == jsonObj.getInteger("grade") ? defaultDegradeGrade : jsonObj.getInteger("grade"));
                jsonObj.put("count", null == jsonObj.getDouble("count") ? defaultDegradeCount : jsonObj.getDouble("count"));
                jsonObj.put("timeWindow", null == jsonObj.getInteger("timeWindow") ? defaultDegradeTimeWindow : jsonObj.getInteger("timeWindow"));
                return jsonObj;
            }).collect(Collectors.toCollection(JSONArray::new));
            List<DegradeRule> flowRuleList =  newJsonArray.toJavaList(DegradeRule.class);
            return flowRuleList;
        });
        DegradeRuleManager.register2Property(degradeRuleDataSource.getProperty());

    }


    /**
     * 热点参数规则
     */
    private void loadParamFlowRuleManager(){
        // 热点参数规则的Key, 在Apollo中用此Key
        String flowRuleKey = "sentinel.paramFlowRules";
        // 热点参数规则的默认值
        String defaultFlowRules = "[]";
        // 注册数据源
        ReadableDataSource<String, List<ParamFlowRule>> paramFlowRuleSource = new ApolloDataSource<>(namespaceName,
                flowRuleKey, defaultFlowRules, source -> {
            JSONArray jsonArray = JSON.parseArray(source);
            JSONArray newJsonArray = jsonArray.stream().map(b -> {
                JSONObject jsonObj = (JSONObject) b;
                jsonObj.put("grade", null == jsonObj.getInteger("grade") ? defaultParamFlowGrade : jsonObj.getInteger("grade"));
                jsonObj.put("count", null == jsonObj.getDouble("count") ? defaultParamFlowCount : jsonObj.getDouble("count"));
                jsonObj.put("paramIdx", null == jsonObj.getInteger("paramIdx") ? defaultParamFlowIdx : jsonObj.getInteger("paramIdx"));
                return jsonObj;
            }).collect(Collectors.toCollection(JSONArray::new));
            List<ParamFlowRule> flowRuleList =  newJsonArray.toJavaList(ParamFlowRule.class);
            return flowRuleList;
        });
        ParamFlowRuleManager.register2Property(paramFlowRuleSource.getProperty());

    }

}
