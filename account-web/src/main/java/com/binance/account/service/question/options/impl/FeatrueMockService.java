package com.binance.account.service.question.options.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.binance.featureservice.vo.feature.FeatureRequest;
import com.binance.featureservice.vo.feature.FeatureResponse;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.HttpClientUtils;
import com.binance.master.utils.JsonUtils;
import com.fasterxml.jackson.core.type.TypeReference;

import lombok.extern.slf4j.Slf4j;

/**
 * 从risk拿来的代码，用于dev环境的mock
 *
 */
@Slf4j
@Service
public class FeatrueMockService {
	@Value("${feature.mock.url:http://10.100.71.55:5054}")
	private String featureMockUrl;
	private final String LIST_FEATURE = "/feature/value/listFeature";

	public  APIResponse<List<FeatureResponse>> listFeature(List<FeatureRequest> requests) {
		String listFeature = featureMockUrl + LIST_FEATURE;
		try {
			String responseStr = HttpClientUtils.postJson(listFeature, APIRequest.instance(requests));
			log.info("mock->invoke listFeature, response:{}", responseStr);
			APIResponse<List<FeatureResponse>> apiResponse = JsonUtils.parse(responseStr,
					new TypeReference<APIResponse<List<FeatureResponse>>>() {
					});
			if (apiResponse == null) {
				log.error("mock->listFeature() The responseStr parse to APIResponse failed .");
				return null;
			}
			if (apiResponse.getStatus() == APIResponse.Status.OK) {
				return APIResponse.getOKJsonResult(apiResponse.getData());
			} else {
				throw new BusinessException(GeneralCode.findByCode(apiResponse.getCode()));
			}
		} catch (Exception ex) {
			log.error("mock->listFeature() error: ", ex);
		}
		return null;
	}
}
