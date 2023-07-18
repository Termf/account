package com.binance.account.controller.certificate;

import com.binance.account.api.UserChannelRiskRatingApi;
import com.binance.account.common.query.UserChannelRiskCountryQuery;
import com.binance.account.common.query.UserChannelRiskRatingQuery;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.certificate.impl.UserChannelRiskRatingHelper;
import com.binance.account.vo.UserChannelRiskCountryVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingRuleVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.certificate.request.ChannelRiskRatingRuleAuditRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyResponse;
import com.binance.account.vo.certificate.request.RiskRatingChangeLimitRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeStatusRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeTierLevelRequest;
import com.binance.account.vo.certificate.request.RiskRatingLimitResponse;
import com.binance.account.vo.certificate.request.RiskRatingStockUserImport;
import com.binance.account.vo.certificate.request.SyncRiskRatingCardCountryRequest;
import com.binance.account.vo.certificate.request.UserChannelRiskRatingRequest;
import com.binance.master.commons.SearchResult;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@Log4j2
@RestController
public class UserChannelRiskRatingController implements UserChannelRiskRatingApi {

	@Resource
	private IUserChannelRiskRating userChannelRiskRating;

	@Resource
	private UserChannelRiskRatingHelper userChannelRiskRatingHelper;

	@Override
	public APIResponse<SearchResult<UserChannelRiskRatingVo>> getUserChannelRiskRatings(
			@Validated @RequestBody APIRequest<UserChannelRiskRatingQuery> request) {
		return APIResponse.getOKJsonResult(userChannelRiskRating.getUserChannelRiskRatings(request.getBody()));
	}

	@Override
	public APIResponse<Void> changeUserRiskRatingStatus(
			@Validated @RequestBody APIRequest<RiskRatingChangeStatusRequest> request) {
		userChannelRiskRating.changeUserRiskRatingStatus(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> changeUserRiskRatingTierLevel(
			@Validated @RequestBody APIRequest<RiskRatingChangeTierLevelRequest> request) {
		userChannelRiskRating.changeUserRiskRatingTierLevel(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<List<UserChannelRiskRatingRuleVo>> getRiskRuleInfoByRiskRatingId(
			@RequestParam("userId") Long userId, @RequestParam("riskRatingId") Integer riskRatingId) {
		return APIResponse.getOKJsonResult(userChannelRiskRating.getRiskRuleInfoByRiskRatingId(userId, riskRatingId));
	}

	@Override
	public APIResponse<Void> redoRiskRatingRule(@RequestParam("userId") Long userId,
			@RequestParam("riskRatingId") Integer riskRatingId) {
		userChannelRiskRating.redoRiskRatingRule(userId, riskRatingId);
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> auditRiskRule(
			@Validated @RequestBody APIRequest<ChannelRiskRatingRuleAuditRequest> request) {
		userChannelRiskRating.auditRiskRule(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<SearchResult<UserChannelRiskCountryVo>> getChannelRiskCountryList(
			@Validated @RequestBody APIRequest<UserChannelRiskCountryQuery> request) {
		return APIResponse.getOKJsonResult(userChannelRiskRating.getChannelRiskCountryList(request.getBody()));
	}

	@Override
	public APIResponse<Integer> saveChannelRiskCountry(
			@Validated @RequestBody APIRequest<UserChannelRiskCountryVo> request) {
		return APIResponse.getOKJsonResult(userChannelRiskRating.saveChannelRiskCountry(request.getBody()));
	}

	@Override
	public APIResponse<Integer> deleteChannelRiskCountry(
			@Validated @RequestBody APIRequest<UserChannelRiskCountryVo> request) {
		UserChannelRiskCountryVo countryVo = request.getBody();
		return APIResponse.getOKJsonResult(
				userChannelRiskRating.deleteChannelRiskCountry(countryVo.getCountryCode(), countryVo.getChannelCode()));
	}

	@Override
	public APIResponse<RiskRatingLimitResponse> riskRatingLimit(
			@Validated @RequestBody APIRequest<UserChannelRiskRatingRequest> request) {
		UserChannelRiskRatingRequest body = request.getBody();
		return APIResponse
				.getOKJsonResult(userChannelRiskRating.riskRatingLimit(body.getUserId(), body.getChannelCode().getCode(),body.getAutoApply()));
	}

	@Override
	public APIResponse<RiskRatingApplyResponse> riskRatingApply(
			@Validated @RequestBody APIRequest<RiskRatingApplyRequest> request) {
		return APIResponse.getOKJsonResult(userChannelRiskRating.riskRatingApply(request.getBody()));
	}

	public APIResponse<Void> syncCardCountry(
			@Validated @RequestBody APIRequest<SyncRiskRatingCardCountryRequest> request) {
		SyncRiskRatingCardCountryRequest body = request.getBody();
		userChannelRiskRating.syncCardCountry(body.getUserId(), body.getChannelCode().name(), body.getCardCountry());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> changeRiskRatingLimit(
			@Validated @RequestBody APIRequest<RiskRatingChangeLimitRequest> request) {
		userChannelRiskRating.changeRiskRatingLimit(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<UserChannelRiskRatingVo> getUserRiskRating(Long userId, Integer riskRatingId) {
		return APIResponse.getOKJsonResult(userChannelRiskRating.getUserChannelRiskRating(userId, riskRatingId));
	}

	@Override
	public APIResponse<Void> batchImportRiskRating(@Validated @RequestBody APIRequest<RiskRatingStockUserImport> request) {
		List<UserChannelRiskRatingVo> riskRatings = request.getBody().getRiskRatings();
		for (UserChannelRiskRatingVo riskRatingVo : riskRatings) {
			try {
				userChannelRiskRatingHelper.stockUserImport(riskRatingVo);
			} catch (Exception e) {
				log.warn("导入riskRating异常 userId:{}", riskRatingVo.getUserId(), e);
			}
		}
		return null;
	}
}
