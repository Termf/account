package com.binance.account.service.certificate.impl;

import java.math.BigDecimal;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.common.enums.UserChannelRiskRatingRuleLevel;
import com.binance.account.common.enums.UserChannelRiskRatingRuleNo;
import com.binance.account.common.enums.UserChannelRiskRatingRuleParam;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.data.entity.certificate.UserChannelRiskCountry;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserChannelRiskRatingRule;
import com.binance.account.data.mapper.certificate.UserChannelRiskCountryMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingRuleMapper;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserChannelRiskRatingHelper {

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Resource
	private KycApiTransferAdapter kycApiTransferAdapter;

	@Resource
	UserChannelRiskCountryMapper userChannelRiskCountryMapper;

	@Resource
	UserChannelRiskRatingRuleMapper userChannelRiskRatingRuleMapper;

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void stockUserImport(UserChannelRiskRatingVo riskRatingVo) throws Exception {
		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByUk(riskRatingVo.getUserId(),
				riskRatingVo.getChannelCode());

		if (riskRating != null) {
			if (StringUtils.isBlank(riskRatingVo.getCardCountry())) {
				log.info("导入riskRating跳过,卡国际为空. userId:{}", riskRating.getUserId());
				return;
			}

			if (StringUtils.isNotBlank(riskRating.getCardCountry())
					&& riskRating.getCardCountry().contains(riskRatingVo.getCardCountry())) {
				log.info("导入riskRating跳过,卡国际已存在. userId:{}", riskRating.getUserId());
				return;
			}

			UserChannelRiskRating record = new UserChannelRiskRating();
			record.setId(riskRating.getId());
			record.setCardCountry(
					StringUtils.isBlank(riskRating.getCardCountry()) ? riskRatingVo.getCardCountry().toUpperCase()
							: riskRating.getCardCountry() + "," + riskRatingVo.getCardCountry().toUpperCase());
			userChannelRiskRatingMapper.updateByPrimaryKeySelective(record);
			log.info("导入riskRating成功,修改卡国际. userId:{}", riskRating.getUserId());
			return;
		}
		Long userId = riskRatingVo.getUserId();

		UserKycCountryResponse resp = kycApiTransferAdapter.getKycCountry(userId);

		BigDecimal zero = new BigDecimal(0);
		StringBuffer name = new StringBuffer()
				.append(StringUtils.isBlank(resp.getFirstName()) ? "" : resp.getFirstName())
				.append(StringUtils.isBlank(resp.getLastName()) ? "" : " " + resp.getLastName());

		UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRatingVo.getChannelCode());
		riskRating = new UserChannelRiskRating();
		riskRating.setUserId(riskRatingVo.getUserId());
		riskRating.setChannelCode(riskRatingVo.getChannelCode());
		riskRating.setCardCountry(riskRatingVo.getCardCountry());
		riskRating.setTierLevel(UserRiskRatingTierLevel.Tier1.name());
		riskRating.setLimitUnit(channelCode.getCurrency());
		riskRating.setApplyAmount(zero.toString());
		riskRating.setName(name.toString());
		riskRating.setBirthday(resp.getBirthday());
		riskRating.setStatus(UserRiskRatingStatus.ENABLE.name());
		riskRating.setWorldCheckStatus(WckChannelStatus.PASSED.name());
		riskRating.setAuditorName("SYS");
		riskRating.setAuditTime(DateUtils.getNewUTCDate());
		riskRating.setAuditRemark("system import");
		riskRating.setRiskRatingLevel(UserChannelRiskRatingRuleLevel.Lower.name());
		riskRating.setRiskRatingScore(zero);
		riskRating.setCitizenshipCountry(
				StringUtils.isBlank(resp.getCountryCode()) ? null : resp.getCountryCode().toUpperCase());
		riskRating.setResidenceCountry(
				StringUtils.isBlank(resp.getCountryCode()) ? null : resp.getCountryCode().toUpperCase());
		riskRating.setCardCountry(StringUtils.isBlank(riskRatingVo.getCardCountry()) ? null
				: riskRatingVo.getCardCountry().toUpperCase());
		riskRating.setIpAddress(null);
		riskRating.setCreateTime(riskRating.getAuditTime());
		riskRating.setUpdateTime(riskRating.getAuditTime());

		userChannelRiskRatingMapper.insert(riskRating);

		List<UserChannelRiskRatingRuleNo> ruleNos = UserChannelRiskRatingRuleNo
				.getRuleByChannelCode(channelCode.name());
		for (UserChannelRiskRatingRuleNo userChannelRiskRatingRuleNo : ruleNos) {
			UserChannelRiskRatingRule userChannelRiskRatingRule = new UserChannelRiskRatingRule();
			userChannelRiskRatingRule.setRiskRatingId(riskRating.getId());
			userChannelRiskRatingRule.setChannelCode(riskRating.getChannelCode());
			userChannelRiskRatingRule.setUserId(riskRating.getUserId());
			userChannelRiskRatingRule.setRuleNo(userChannelRiskRatingRuleNo.getRuleNo());
			userChannelRiskRatingRule.setRuleName(userChannelRiskRatingRuleNo.getRuleName());

			switch (userChannelRiskRatingRuleNo) {
			case RISK_PEP:
				userChannelRiskRatingRule.setRuleValue("0");
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Low.name());
				userChannelRiskRatingRule.setRuleScore("1");
				break;
			case RISK_AGE:
				userChannelRiskRatingRule.setRuleValue(riskRating.getBirthday());
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Lower.name());
				userChannelRiskRatingRule.setRuleScore("0");
				break;
			case RISK_SANCTIONS_HITS:
				userChannelRiskRatingRule.setRuleValue("0");
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Low.name());
				userChannelRiskRatingRule.setRuleScore("0");
				break;
			case RISK_BEHAVIOUR:
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleParam.DocumentsGood.getLevel().name());
				userChannelRiskRatingRule.setRuleValue(UserChannelRiskRatingRuleParam.DocumentsGood.getParamValue());
				userChannelRiskRatingRule.setRuleScore(UserChannelRiskRatingRuleParam.DocumentsGood.getScore());
				break;
			case RISK_NATIONALITY:
				userChannelRiskRatingRule.setRuleLevel(riskRating.getCitizenshipCountry());
			case RISK_COUNTRY:
				userChannelRiskRatingRule.setRuleValue(riskRating.getResidenceCountry());

				String countryCode = userChannelRiskRatingRule.getRuleLevel();
				UserChannelRiskCountry riskCountry = userChannelRiskCountryMapper.selectByPrimaryKey(countryCode,
						UserRiskRatingChannelCode.CHECKOUT.name());

				userChannelRiskRatingRule.setRuleScore(riskCountry == null ? "0" : riskCountry.getRiskScore());
				userChannelRiskRatingRule.setRuleLevel(
						riskCountry == null ? UserChannelRiskRatingRuleLevel.Lower.name() : riskCountry.getRiskLevel());

				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Lower.name());
				userChannelRiskRatingRule.setRuleScore("0");
				break;
			case RISK_PRODUCT:
				userChannelRiskRatingRule.setRuleValue(riskRating.getTierLevel());
				userChannelRiskRatingRule.setRuleScore("5.0");
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Medium.name());
				break;
			case RISK_AVG_DAILY:
				userChannelRiskRatingRule.setRuleValue(riskRating.getApplyAmount());
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Lower.name());
				userChannelRiskRatingRule.setRuleScore(zero.toString());
				break;
			case RISK_MANUAL:
				userChannelRiskRatingRule
						.setRuleValue(UserChannelRiskRatingRuleParam.OngoingMonitoring.getParamValue());
				userChannelRiskRatingRule.setRuleLevel(UserChannelRiskRatingRuleLevel.Lower.name());
				userChannelRiskRatingRule.setRuleScore(zero.toString());
				break;

			default:
				break;
			}
			userChannelRiskRatingRule.setCreateTime(DateUtils.getNewDate());
			userChannelRiskRatingRule.setUpdateTime(DateUtils.getNewDate());
			userChannelRiskRatingRuleMapper.insert(userChannelRiskRatingRule);
		}
		log.info("导入riskRating成功 userId:{}", riskRatingVo.getUserId());
	}
}
