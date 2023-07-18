package com.binance.account.service.certificate;

import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.query.UserChannelRiskCountryQuery;
import com.binance.account.common.query.UserChannelRiskRatingQuery;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.vo.UserChannelRiskCountryVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingRuleVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.certificate.request.ChannelRiskRatingRuleAuditRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyResponse;
import com.binance.account.vo.certificate.request.RiskRatingChangeDailyLimitRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeLimitRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeMonthlyLimitRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeStatusRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeTierLevelRequest;
import com.binance.account.vo.certificate.request.RiskRatingLimitResponse;
import com.binance.account.vo.certificate.response.UserRiskRatingTierLevelVo;
import com.binance.master.commons.SearchResult;

import java.util.List;
import java.util.Map;

public interface IUserChannelRiskRating {

	SearchResult<UserChannelRiskRatingVo> getUserChannelRiskRatings(UserChannelRiskRatingQuery riskRatingQuery);

	void changeUserRiskRatingStatus(RiskRatingChangeStatusRequest request);

	UserChannelRiskRatingVo getUserChannelRiskRating(Long userId, String channelCode);
	
	UserChannelRiskRatingVo getUserChannelRiskRating(Long userId, Integer riskRatingId);

	/**
	 *
	 * @param userId
	 * @param wckAudit 可以为空，如果wckAudit为空，则从wck拉审核记录，选最后修改那条。
	 * @param riskRating
	 * @param isPep 可以为空，为空则通过riskRatingRule里面拿值。可能是wck返回，也可能是操作员修改
	 * @param sanctionsHits 可以为空，为空则通过riskRatingRule里面拿值。可能是wck返回，也可能是操作员修改
	 */
	void pullRiskRating(Long userId, UserChannelWckAuditVo wckAudit, UserChannelRiskRating riskRating, Long isPep,
			Long sanctionsHits);
    
    void changeRiskRatingLimit(RiskRatingChangeLimitRequest request);

    void changeUserRiskRatingTierLevel(RiskRatingChangeTierLevelRequest request);

	List<UserChannelRiskRatingRuleVo> getRiskRuleInfoByRiskRatingId(Long userId, Integer riskRatingId);

	void redoRiskRatingRule(Long userId, Integer riskRatingId);

	void auditRiskRule(ChannelRiskRatingRuleAuditRequest request);

	SearchResult<UserChannelRiskCountryVo> getChannelRiskCountryList(UserChannelRiskCountryQuery query);

	int saveChannelRiskCountry(UserChannelRiskCountryVo riskCountryVo);

	int deleteChannelRiskCountry(String countryCode, String channelCode);

	Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> getRiskTierLevelLimit(String channelCode);

	RiskRatingLimitResponse riskRatingLimit(Long userId,String channelCode,Boolean autoApply);

	RiskRatingApplyResponse riskRatingApply(RiskRatingApplyRequest request);

	void syncCardCountry(Long userId,String channelCode,String cardCountry);
}
