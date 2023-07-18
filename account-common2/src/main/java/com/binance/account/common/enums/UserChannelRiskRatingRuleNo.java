package com.binance.account.common.enums;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public enum UserChannelRiskRatingRuleNo {

	RISK_PEP("risk_pep","Customer Risk (Pep)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId

	}),
	RISK_AGE("risk_age","Customer Risk (age)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId
	}),
	RISK_SANCTIONS_HITS("risk_sanctions_hits","Customer risk (Sanctions- Hits)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId
	}),
	RISK_BEHAVIOUR("risk_behaviour","Customer behaviour (documents)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId
	}),
	RISK_NATIONALITY("risk_nationality","Geographical Risk (Nationality)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId,
					UserRiskRatingChannelCode.worldpay
	}),
	RISK_COUNTRY("risk_country","Geographical Risk (Country of Residence)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId,
					UserRiskRatingChannelCode.worldpay
	}),
	RISK_PRODUCT("risk_product","Product Risk",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId,
					UserRiskRatingChannelCode.worldpay
	}),
	RISK_AVG_DAILY("risk_avg_daily","Estimated Investment Value / Actual Transaction Value Ongoing Monitoring (daily Cumulative)",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId,
					UserRiskRatingChannelCode.worldpay
	}),
	RISK_MANUAL("risk_manual","Manual Review / Ongoing Monitoring",
			new UserRiskRatingChannelCode[]{
					UserRiskRatingChannelCode.CHECKOUT,
					UserRiskRatingChannelCode.StandardBank,
					UserRiskRatingChannelCode.ClearJunction,
					UserRiskRatingChannelCode.FourBill,
					UserRiskRatingChannelCode.BCB,
					UserRiskRatingChannelCode.Qiwi,
					UserRiskRatingChannelCode.FlutterwaveNGN,
					UserRiskRatingChannelCode.FlutterwaveUGX,
					UserRiskRatingChannelCode.BauPoli,
					UserRiskRatingChannelCode.BauPayId,
					UserRiskRatingChannelCode.worldpay
	});

	private String ruleNo;

	private String ruleName;

	private UserRiskRatingChannelCode[] channelCodes;

	private UserChannelRiskRatingRuleNo(String ruleNo,String ruleName,UserRiskRatingChannelCode[] channelCodes) {
		this.ruleNo = ruleNo;
		this.ruleName = ruleName;
		this.channelCodes = channelCodes;
	}

	public UserRiskRatingChannelCode[] getChannelCodes() {
		return channelCodes;
	}

	public static UserChannelRiskRatingRuleNo getRuleByRuleNo(String ruleNo) {
		UserChannelRiskRatingRuleNo[] rules = UserChannelRiskRatingRuleNo.values();
		for (UserChannelRiskRatingRuleNo userChannelRiskRatingRuleNo : rules) {
			if(userChannelRiskRatingRuleNo.getRuleNo().equals(ruleNo)) {
				return userChannelRiskRatingRuleNo;
			}
		}
		return null;
	}

	public static List<UserChannelRiskRatingRuleNo> getRuleByChannelCode(String channelCode){
		List<UserChannelRiskRatingRuleNo> list = new ArrayList<UserChannelRiskRatingRuleNo>();
		UserChannelRiskRatingRuleNo[] rules = UserChannelRiskRatingRuleNo.values();
		for (UserChannelRiskRatingRuleNo userChannelRiskRatingRuleNo : rules) {
			UserRiskRatingChannelCode[] channelCodes = userChannelRiskRatingRuleNo.getChannelCodes();
			if(channelCodes == null && channelCodes.length <= 0) {
				continue;
			}
			for (UserRiskRatingChannelCode code : channelCodes) {
				if (code.getCode().equals(channelCode)) {
					list.add(userChannelRiskRatingRuleNo);
					break;
				}
			}
		}
		return list;

	}

	public String getRuleNo() {
		return ruleNo;
	}

	public String getRuleName() {
		return ruleName;
	}
}
