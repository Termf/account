package com.binance.account.common.enums;

import java.math.BigDecimal;

public enum UserChannelRiskRatingRuleLevel {
	Unknow,Lower,Low,Medium,High,Extreme;
	
	/**
	 * Extreme (Declined) 91 and over
	 * High is 28 to 90
	 * Medium 21 to 27
	 * Low 8 to 20
	 * Lower 0 to 7
	 * Note: High risk customers will be subject to Enhanced Customer Due Diligence 
	 */
	public static UserChannelRiskRatingRuleLevel totalScore(BigDecimal score) {
		if(new BigDecimal(0).compareTo(score) <=0 && new BigDecimal(8).compareTo(score) >0) {
			return UserChannelRiskRatingRuleLevel.Lower;
		}
		
		if(new BigDecimal(8).compareTo(score) <=0 && new BigDecimal(21).compareTo(score) >0) {
			return UserChannelRiskRatingRuleLevel.Low;
		}
		
		if(new BigDecimal(21).compareTo(score) <=0 && new BigDecimal(28).compareTo(score) >0) {
			return UserChannelRiskRatingRuleLevel.Medium;
		}
		
		if(new BigDecimal(28).compareTo(score) <=0 && new BigDecimal(91).compareTo(score) >0) {
			return UserChannelRiskRatingRuleLevel.High;
		}
		
		if(new BigDecimal(91).compareTo(score) <=0) {
			return UserChannelRiskRatingRuleLevel.Extreme;
		}
		
		return UserChannelRiskRatingRuleLevel.Lower;
		
	}
	
	public static UserChannelRiskRatingRuleLevel detailScore(UserChannelRiskRatingRuleNo rule,BigDecimal score) {
		
		if(rule == null) {
			return UserChannelRiskRatingRuleLevel.Unknow;
		}
		
		switch (rule) {
		case RISK_AGE:
			if(new BigDecimal(0).compareTo(score) >=0 && new BigDecimal(10).compareTo(score) >0) {
				return UserChannelRiskRatingRuleLevel.Low;
			}
			return UserChannelRiskRatingRuleLevel.High;
		case RISK_PRODUCT:
			if(new BigDecimal(0).compareTo(score) >=0 && new BigDecimal(5).compareTo(score) >0) {
				return UserChannelRiskRatingRuleLevel.Low;
			}
			
			if(new BigDecimal(5).compareTo(score) >=0 && new BigDecimal(10).compareTo(score) >0) {
				return UserChannelRiskRatingRuleLevel.Medium;
			}
			return UserChannelRiskRatingRuleLevel.High;
		case RISK_MANUAL:
		case RISK_AVG_DAILY:
			if(new BigDecimal(0).compareTo(score) >=0 && new BigDecimal(5).compareTo(score) >0) {
				return UserChannelRiskRatingRuleLevel.Lower;
			}
			if(new BigDecimal(5).compareTo(score) >=0 && new BigDecimal(10).compareTo(score) >0) {
				return UserChannelRiskRatingRuleLevel.Low;
			}
			if(new BigDecimal(10).compareTo(score) >=0 && new BigDecimal(20).compareTo(score) >0) {
				return UserChannelRiskRatingRuleLevel.Medium;
			}
			return UserChannelRiskRatingRuleLevel.High;
		default:
			break;
		}
		return UserChannelRiskRatingRuleLevel.Unknow;
		
	}
}
