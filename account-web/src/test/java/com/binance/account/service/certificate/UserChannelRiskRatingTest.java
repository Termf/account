package com.binance.account.service.certificate;

import com.binance.account.common.enums.UserChannelRiskRatingRuleNo;
import com.binance.account.common.enums.UserChannelRiskRatingRuleParam;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.common.query.UserChannelRiskRatingQuery;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserChannelRiskRatingRule;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingRuleMapper;
import com.binance.account.service.kyc.BaseTest;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.certificate.request.RiskRatingApplyRequest;
import com.binance.account.vo.certificate.response.UserRiskRatingTierLevelVo;
import com.binance.master.utils.DateUtils;
import com.binance.rule.api.CommonRiskApi;
import org.junit.Test;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

public class UserChannelRiskRatingTest extends BaseTest {

	@Resource
	UserChannelRiskRatingRuleMapper userChannelRiskRatingRuleMapper;

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Resource
	IUserChannelRiskRating iUserChannelRiskRating;

	@Resource
	private CommonRiskApi commonRiskApi;
	
	
	public static void main(String[] args) {
		UserChannelRiskRatingRuleParam.DocumentsGood.getLevel().name();
		UserChannelRiskRatingRuleParam.DocumentsGood.getParamValue();
		UserChannelRiskRatingRuleParam.DocumentsGood.getScore();
	}

	@Test
	public void init() {
		UserChannelRiskRating rating = new UserChannelRiskRating();
		rating.setUserId(1111111l);
		rating.setChannelCode(UserRiskRatingChannelCode.CHECKOUT.name());
		rating.setTierLevel(UserRiskRatingTierLevel.Tier2.name());
		rating.setDailyLimit(new BigDecimal("0"));
		rating.setMonthlyLimit(new BigDecimal("0"));
		rating.setLimitUnit("USD");
		rating.setResidenceCountry("CN");
		rating.setCitizenshipCountry("CN");
		rating.setName("刘峰");
		rating.setStatus(UserRiskRatingStatus.DISABLE.name());
		rating.setCreateTime(DateUtils.getNewDate());
		rating.setUpdateTime(DateUtils.getNewDate());
		userChannelRiskRatingMapper.insert(rating);

		UserChannelRiskRatingRuleNo[] rules = UserChannelRiskRatingRuleNo.values();
		for (UserChannelRiskRatingRuleNo userChannelRiskRatingRuleNo : rules) {
			UserChannelRiskRatingRule userChannelRiskRatingRule = new UserChannelRiskRatingRule();
			userChannelRiskRatingRule.setRiskRatingId(rating.getId());
			userChannelRiskRatingRule.setChannelCode(rating.getChannelCode());
			userChannelRiskRatingRule.setUserId(rating.getUserId());
			userChannelRiskRatingRule.setRuleName(userChannelRiskRatingRuleNo.getRuleName());
			userChannelRiskRatingRule.setRuleNo(userChannelRiskRatingRuleNo.getRuleNo());
			switch (userChannelRiskRatingRuleNo) {
			case RISK_BEHAVIOUR:
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleParam.DocumentsGood.getLevel().name());
				userChannelRiskRatingRule.setRuleValue(UserChannelRiskRatingRuleParam.DocumentsGood.getParamValue());
				userChannelRiskRatingRule.setRuleScore(UserChannelRiskRatingRuleParam.DocumentsGood.getScore());
				break;
			case RISK_MANUAL:
				userChannelRiskRatingRule
						.setRuleValue(UserChannelRiskRatingRuleParam.OngoingMonitoring.getParamValue());
				break;
			case RISK_AVG_DAILY:
				userChannelRiskRatingRule.setRuleValue("100.00");
				break;
			default:
				break;
			}
			userChannelRiskRatingRule.setCreateTime(DateUtils.getNewDate());
			userChannelRiskRatingRule.setUpdateTime(DateUtils.getNewDate());
			userChannelRiskRatingRuleMapper.insert(userChannelRiskRatingRule);
		}
	}

	@Test
	public void testPullRiskRating() {
		UserChannelWckAuditVo wckAudit = new UserChannelWckAuditVo();
		wckAudit.setUserId(1111111l);
		wckAudit.setCheckName("刘峰");
		wckAudit.setNationality("CN");
		wckAudit.setBirthDate("1988-05-07");
		wckAudit.setStatus(WckChannelStatus.PASSED);
		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByUk(1111111l,
				UserRiskRatingChannelCode.CHECKOUT.name());
		iUserChannelRiskRating.pullRiskRating(1111111l, wckAudit, riskRating, 0l, 0l);
	}

	@Test
	public void testRiskRatingApply() {
		RiskRatingApplyRequest request = new RiskRatingApplyRequest();
		request.setUserId(1234567l);
		request.setResidenceCountry("CN");
		request.setApplyAmount(new BigDecimal("10.0"));
		request.setChannelCode(UserRiskRatingChannelCode.CHECKOUT);
		iUserChannelRiskRating.riskRatingApply(request);

	}
	@Test
	public void testLimit() {
//		DecisionCommonRequest request = new DecisionCommonRequest();
//		request.setEventCode("recharge_limit");
//		request.setContext(new HashMap<>());
//		request.getContext().put("channel", "checkout");
//		request.getContext().put("tier", "allTier");
		Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> riskResp = iUserChannelRiskRating.getRiskTierLevelLimit("checkout");
		System.out.println(riskResp);
	}
	@Test
	public void testPage() {
		UserChannelRiskRatingQuery query = new UserChannelRiskRatingQuery();
		query.setUserId(350490760l);
		long count = userChannelRiskRatingMapper.getPageCount(query);
		System.out.println(count);
	}
	
	@Test
	public void testAPply() {
		RiskRatingApplyRequest request = new RiskRatingApplyRequest();
		request.setUserId(350595912l);
		request.setApplyAmount(new BigDecimal(10));
		request.setCardCountry("CN");
		request.setChannelCode(UserRiskRatingChannelCode.CHECKOUT);
		request.setResidenceCountry("CN");
		iUserChannelRiskRating.riskRatingApply(request);
	}
}
