package com.binance.account.service.security.filter.impl;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.service.question.checker.QuestionModuleChecker;
import com.binance.account.service.security.filter.IDecisionBeforeEmail;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.rule.api.RuleDecisionApi;
import com.binance.rule.request.RuleRequest;
import com.google.common.collect.Maps;

import lombok.extern.slf4j.Slf4j;

/**
 * RM-426 重置手机，重置谷歌，解禁账户成功之后调风控
 *
 */
@Slf4j
@Service
public class ResetSuccessBeforeProcessor implements IDecisionBeforeEmail {

	@Resource
	private RuleDecisionApi api;
	@Resource
	private QuestionModuleChecker checker;

	@Override
	public boolean beforeSuccessEmail(Long userId, String resetId) {
		String pk = getDevicePK(resetId);
		String ip = getRequestIp(resetId);
		return doRule(userId, ip, pk);
	}

	private String getDevicePK(String resetId) {
		return checker.getDevicePK(resetId);
	}

	private String getRequestIp(String resetId) {
//		try {
//			return WebUtils.getRequestIp();
//		} catch (Exception e) {
//			log.warn("", e);
//		}
		// RM-531 从表里获取ip,注释的代码的ip来自请求方，例如后台
		UserSecurityReset reset = checker.userResetExistValidate(resetId);
		if (reset != null) {
			return reset.getApplyIp();
		}
		return "";
	}

	private boolean doRule(Long userId, String requetIp, String devicePk) {
		APIResponse<Object> response = null;
		try {
			RuleRequest request = new RuleRequest();
			request.setRuleName(RULE);
			Map<String, Object> params = Maps.newHashMap();
			params.put("uid", userId);
			params.put("ipaddr", requetIp);
			params.put("device_pk", devicePk);
			request.setParameters(params);
			log.info("查询决策系统doRule->request:{}", request);
			response = api.doRule(APIRequest.instance(request));
			log.info("查询决策系统doRule->request:{},response:{}", request, response);
			if (response == null || response.getStatus() != APIResponse.Status.OK) {
				return false;
			}
			Map<String, String> map = toMap(response.getData() + "");
			return "hit".equalsIgnoreCase(map.get(RULE));
		} catch (Exception e) {
			log.error("查询决策系统doRule异常，userId:" + userId, e);
		}
		return false;
	}

	private Map<String,String> toMap(String toString){
		Map<String,String> map = new HashMap<String,String>();
		if (StringUtils.isNotBlank(toString)) {
			toString = toString.substring(1, toString.length() - 1);
			String[] strs = StringUtils.split(toString, ",");
			for (String string : strs) {
				String[] tmps = StringUtils.split(string, "=");
				if (tmps.length > 1) {
					map.put(tmps[0], tmps[1]);
				}
			}
		}
		return map;
	}
	
}