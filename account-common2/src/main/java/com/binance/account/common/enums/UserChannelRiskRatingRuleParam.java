package com.binance.account.common.enums;

public enum UserChannelRiskRatingRuleParam {

	Pep0(UserChannelRiskRatingRuleNo.RISK_PEP, "0", "1", UserChannelRiskRatingRuleLevel.Low),
	Pep1(UserChannelRiskRatingRuleNo.RISK_PEP, "1", "20", UserChannelRiskRatingRuleLevel.High),
	SanctionsHits0(UserChannelRiskRatingRuleNo.RISK_SANCTIONS_HITS, "0", "0", UserChannelRiskRatingRuleLevel.Low),
	SanctionsHits1(UserChannelRiskRatingRuleNo.RISK_SANCTIONS_HITS, "1", "10", UserChannelRiskRatingRuleLevel.High),
	SanctionsHits2(UserChannelRiskRatingRuleNo.RISK_SANCTIONS_HITS, "2", "91", UserChannelRiskRatingRuleLevel.Extreme),
	DocumentsGood(UserChannelRiskRatingRuleNo.RISK_BEHAVIOUR, "Good", "1", UserChannelRiskRatingRuleLevel.Low),
	DocumentsFake(UserChannelRiskRatingRuleNo.RISK_BEHAVIOUR, "Fake", "91", UserChannelRiskRatingRuleLevel.Extreme),
	OngoingMonitoring(UserChannelRiskRatingRuleNo.RISK_MANUAL, "0", null, null);

	private UserChannelRiskRatingRuleNo rule;

	private String paramValue;

	private String score;

	private UserChannelRiskRatingRuleLevel level;

	private UserChannelRiskRatingRuleParam(UserChannelRiskRatingRuleNo rule, String paramValue, String score,
			UserChannelRiskRatingRuleLevel level) {
		this.rule = rule;
		this.paramValue = paramValue;
		this.score = score;
		this.level = level;
	}

	public static String getScore(UserChannelRiskRatingRuleNo rule, String paramValue) {
		UserChannelRiskRatingRuleParam param = getParam(rule, paramValue);
		return param == null ? null : param.getScore();
	}
	
	public static UserChannelRiskRatingRuleParam getParam(UserChannelRiskRatingRuleNo rule, String paramValue) {
		UserChannelRiskRatingRuleParam[] params = UserChannelRiskRatingRuleParam.values();
		for (UserChannelRiskRatingRuleParam param : params) {
			if (param.getRule().equals(rule) && param.getParamValue().equals(paramValue)) {
				return param;
			}
		}
		return null;
	}

	public UserChannelRiskRatingRuleNo getRule() {
		return rule;
	}

	public String getParamValue() {
		return paramValue;
	}

	public String getScore() {
		return score;
	}

	public UserChannelRiskRatingRuleLevel getLevel() {
		return level;
	}

}
