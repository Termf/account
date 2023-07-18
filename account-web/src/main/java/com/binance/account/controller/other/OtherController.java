package com.binance.account.controller.other;

import com.binance.account.api.OtherApi;
import com.binance.account.common.enums.CacheRefreshType;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.mq.CacheRefreshMsgSender;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.other.IOther;
import com.binance.account.vo.kyc.CountryStateVo;
import com.binance.account.vo.other.CleanLocalCacheRequest;
import com.binance.account.vo.other.GetCountryStateRequest;
import com.binance.account.vo.other.GetMessageMapRequest;
import com.binance.account.vo.other.MessageMapVo;
import com.binance.account.vo.other.SendDisableTokenEmailRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.matchbox.api.AccountApi;

import org.springframework.beans.BeanUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.List;

@RestController
public class OtherController implements OtherApi {

	@Resource
	private AccountApi accountApi;

	@Resource
	private IOther iOther;

	@Resource
	private CacheRefreshMsgSender cacheRefreshMsgSender;

	@Override
	public APIResponse<?> removeRedisCache(@RequestBody() @Validated() APIRequest<String> request) throws Exception {
		RedisCacheUtils.delFuzzy(request.getBody());
		return APIResponse.getOKJsonResult(null);
	}

	@Override
	public APIResponse<Object> getDetailsByTradingAccountId(@RequestBody() @Validated() APIRequest<Long> request)
			throws Exception {
		return APIResponse.getOKJsonResult(this.accountApi.getDetailsByTradingAccountId(request.getBody()));
	}

	@Override
	public APIResponse<String> sendDisableTokenEmail(
			@RequestBody() @Validated() APIRequest<SendDisableTokenEmailRequest> request) {
		return this.iOther.sendDisableTokenEmail(request);
	}

	@Override
	public APIResponse<Void> cleanLocalCache(@Validated @RequestBody APIRequest<CleanLocalCacheRequest> request) {
		cacheRefreshMsgSender.notifyMQ(request.getBody().getType());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<List<MessageMapVo>> getMessageMapList(
			@Validated @RequestBody APIRequest<GetMessageMapRequest> request) {
		List<MessageMapVo> vos = MessageMapHelper.getMessageList(request.getBody().getCode());
		return APIResponse.getOKJsonResult(vos);
	}

	@Override
	public APIResponse<String> getMessageByKey(@Validated @RequestBody APIRequest<GetMessageMapRequest> request) {
		return APIResponse
				.getOKJsonResult(MessageMapHelper.getMessage(request.getBody().getCode(), request.getLanguage()));
	}

	@Override
	public APIResponse<List<CountryStateVo>> getCountryStateInfoList(
			@Validated @RequestBody APIRequest<GetCountryStateRequest> request) {
		List<CountryState> results = CountryStateHelper.getCountryStateByCode(request.getBody().getCode(),
				request.getBody().getEnable());
		List<CountryStateVo> vos = new ArrayList<CountryStateVo>();
		for (CountryState countryState : results) {
			CountryStateVo vo = new CountryStateVo();
			BeanUtils.copyProperties(countryState, vo);
			vos.add(vo);
		}
		return APIResponse.getOKJsonResult(vos);
	}

	@Override
	public APIResponse<CountryStateVo> getCountryStateByKey(
			@Validated @RequestBody APIRequest<GetCountryStateRequest> request) {
		CountryState results = CountryStateHelper.getCountryStateByPk(request.getBody().getCode(),
				request.getBody().getStateCode());
		CountryStateVo vo = new CountryStateVo();
		BeanUtils.copyProperties(results, vo);
		return APIResponse.getOKJsonResult(vo);
	}

	@Override
	public APIResponse<Void> updateCountryState(@Validated @RequestBody APIRequest<GetCountryStateRequest> request) {
		CountryState countyState = new CountryState();
		BeanUtils.copyProperties(request.getBody(), countyState);
		CountryStateHelper.updateCountryState(countyState);
		cacheRefreshMsgSender.notifyMQ(CacheRefreshType.COUNTRY_STATE);
		// 防止主从同步问题，先暂停下
        try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
        }
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> modifyMessageMap(@Validated @RequestBody APIRequest<List<MessageMapVo>> request) {
		MessageMapHelper.saveMessageMap(request.getBody());
		cacheRefreshMsgSender.notifyMQ(CacheRefreshType.MESSAGE_MAP);
		// 防止主从同步问题，先暂停下
        try {
			Thread.sleep(600);
		} catch (InterruptedException e) {
        }
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<List<MessageMapVo>> fuzzySearch(@Validated @RequestBody APIRequest<MessageMapVo> request) {
		List<MessageMapVo> vos = MessageMapHelper.fuzzyGetByCode(request.getBody().getCode());
		return APIResponse.getOKJsonResult(vos);
	}
}
