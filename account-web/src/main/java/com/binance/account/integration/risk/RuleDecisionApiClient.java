package com.binance.account.integration.risk;

import com.binance.account.service.device.IUserDevice;
import com.binance.account.vo.device.response.FindMostSimilarUserDeviceResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.rule.api.RuleDecisionApi;
import com.binance.rule.request.RuleRequest;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * Created by yangyang on 2019/10/11.
 */
@Log4j2
@Service
public class RuleDecisionApiClient {

    private static final String HIT = "hit";
    public static final String UNBIND_UPDATE_PWD = "rule_risk_operation_unbind_or_update_pwd_or_withdraw_after_login";
    public static final String RISK_OR_UNDO_FORBID = "rule_risk_operation_reset_or_undo_forbid";
    public static final String FORGET_PWD = "rule_risk_operation_forget_pwd";

    @Autowired
    private RuleDecisionApi ruleDecisionApi;
    @Autowired
    protected IUserDevice userDeviceBusiness;

    public boolean checkWithdrawRule(String ruleName, Map<String,Object> parameters){
        log.info("RuleDecisionApiClient.checkWithdrawRule ruleName:{}",ruleName);
        APIRequest<RuleRequest> apiRequest = new APIRequest<>();
        RuleRequest ruleRequest = new RuleRequest();
        ruleRequest.setRuleName(ruleName);
        ruleRequest.setParameters(parameters);
        apiRequest.setBody(ruleRequest);
        try {
            APIResponse<Object> apiResponse = ruleDecisionApi.doRule(apiRequest);
            log.info("ruleDecisionApi.doRule.result:{}", JsonUtils.toJsonHasNullKey(apiResponse));
            if (APIResponse.Status.OK != apiResponse.getStatus() || apiResponse.getData() == null){
                log.warn("ruleDecisionApi.doRule error" + apiResponse.getErrorData());
                return false;
            }
            Map<String,Object> map = (Map<String, Object>) apiResponse.getData();
            log.info("ruleDecisionApi.objectToMap,map:{}", map);
            if (map != null && HIT.equals(String.valueOf(map.get(ruleName)))) {
                log.info("ruleDecisionApi.doRule.true");
                return true;
            }
        }catch (Exception e){
            log.error("RuleDecisionApiClient.checkWithdrawRule error",e);
        }
        return false;
    }

    public boolean unifyCheckWithdrawRule(String ruleName, Long userId, Map<String, String> deviceInfo){
        log.info("RuleDecisionApiClient.checkWithdrawRule ruleName:{},userId:{},ip:{}",
                ruleName, userId, WebUtils.getRequestIp());
        if (StringUtils.isBlank(ruleName) || userId == null){
            return false;
        }
        try {
            Map<String,Object> parameters = Maps.newHashMap();
            parameters.put("uid",userId);
            parameters.put("ipaddr", WebUtils.getRequestIp());
            if (deviceInfo != null && !deviceInfo.isEmpty()){
                FindMostSimilarUserDeviceResponse mostSimilarDevice = userDeviceBusiness.findMostSimilarDevice(userId, deviceInfo, WebUtils.getAPIRequestHeader().getTerminal().getCode());
                parameters.put("device_pk", (mostSimilarDevice !=null&&mostSimilarDevice.isSame()&&mostSimilarDevice.getMatched()!=null)?mostSimilarDevice.getMatched().getId():null);
            }
            parameters.putIfAbsent("device_pk", "");
            parameters.putIfAbsent("ipaddr","");
            return this.checkWithdrawRule(ruleName, parameters);
        }catch (Exception e){
            log.warn("RuleDecisionApiClient.unifyCheckWithdrawRule error",e);
        }
        return false;
    }
}
