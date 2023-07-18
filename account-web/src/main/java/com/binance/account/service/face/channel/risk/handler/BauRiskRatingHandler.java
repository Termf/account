package com.binance.account.service.face.channel.risk.handler;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingHandler;
import com.binance.certification.common.constant.KycFillExtFields;
import com.binance.certification.common.enums.IdentityType;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Log4j2
@Component
@UserChannelRiskRatingHandler(handlerType = { UserRiskRatingChannelCode.BauPayId, UserRiskRatingChannelCode.BauPoli })
public class BauRiskRatingHandler extends DefaultRiskRatingHandler {

	@Override
	public UserRiskRatingTierLevel nextTierLevel(UserChannelRiskRating riskRating, KycCertificate kycCertificate) {
		if (!WckChannelStatus.PASSED.name().equals(riskRating.getWorldCheckStatus())
				&& !WckChannelStatus.AUTO_PASS.name().equals(riskRating.getWorldCheckStatus())) {
			log.info("RiskRating变更tier等级，wck状态不为通过.暂不处理 userId:{},WorldCheckStatus:{}", kycCertificate.getUserId(),
					riskRating.getWorldCheckStatus());
			return UserRiskRatingTierLevel.NoTier;
		}

		UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());
		// tier2 || tier3
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {

			if (riskRating.getDailyLimit() != null
					&& channelCode.getDailyLimit().compareTo(riskRating.getDailyLimit()) < 0) {
				log.info("RiskRating变更tier等级为tier3. userId:{},channel:{}kyc:{},base:{},address:{}",
						kycCertificate.getUserId(), channelCode.getCode(), kycCertificate.getStatus(),
						kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
				return UserRiskRatingTierLevel.Tier3;
			}
			log.info("RiskRating变更tier等级为tier2. userId:{},channel:{},kyc:{},base:{},address:{}",
					kycCertificate.getUserId(), channelCode.getCode(), kycCertificate.getStatus(),
					kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
			return UserRiskRatingTierLevel.Tier2;
		}

		// tier0
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())
				&& "AUD_AUTH_SUCC".equals(kycCertificate.getBaseSubStatus())) {
			log.info("RiskRating变更tier等级为tier0. userId:{},channel:{},kyc:{},base:{},address:{}",
					kycCertificate.getUserId(), channelCode.getCode(), kycCertificate.getStatus(),
					kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
			return UserRiskRatingTierLevel.Tier1;
		}
		log.info("RiskRating变更tier等级,kyc,base未通过. userId:{},channel:{},kyc:{},base:{},address:{}",
				kycCertificate.getUserId(), channelCode.getCode(), kycCertificate.getStatus(),
				kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
		return UserRiskRatingTierLevel.NoTier;
	}

	public boolean isForbidCountry(String countryCode, UserChannelRiskRating riskRating, String name, String birthday, KycFillInfo baseInfo) {
		boolean isForbid = super.isForbidCountry(countryCode, riskRating, name, birthday, baseInfo);

		if (isForbid) {
			return true;
		}

		if(baseInfo == null || !IdentityType.VISA.name().equals(baseInfo.getIdType())) {
			return false;
		}

		JSONObject ext = null;

		if(StringUtils.isNotBlank(baseInfo.getExt())) {
			ext = JSONObject.parseObject(baseInfo.getExt());
		}else {
			ext = new JSONObject();
		}
		String forbidCountriesCfg = null;
		String country = ext.getString(KycFillExtFields.KYC_FILL_INFO_EXT_COUNTRY_OF_ISSUE.getFieldName());
		if (UserRiskRatingChannelCode.BauPoli.getCode().equals(riskRating.getChannelCode())) {
			forbidCountriesCfg = config.getBauPoliForbidIssueCountries();
		} else if (UserRiskRatingChannelCode.BauPayId.getCode().equals(riskRating.getChannelCode())) {
			forbidCountriesCfg = config.getBauPayIdForbidIssueCountries();
		}
		if (StringUtils.isBlank(forbidCountriesCfg) || StringUtils.isBlank(country)) {
			return false;
		}
		String[] forbidCountries = forbidCountriesCfg.split(",");
		for (String forbid : forbidCountries) {
			if (StringUtils.equalsIgnoreCase(forbid, country)) {
				isForbid =  true;
				break;
			}
		}
		if (isForbid) {
			UserChannelRiskRating record = new UserChannelRiskRating();
			record.setId(riskRating.getId());
			record.setTierLevel(UserRiskRatingTierLevel.NoTier.name());
			record.setStatus(UserRiskRatingStatus.FORBID.name());
			record.setFailReason("forbidden country");
			record.setName(name);
			record.setBirthday(birthday);
			record.setCitizenshipCountry(countryCode);
			record.setUpdateTime(DateUtils.getNewUTCDate());
			userChannelRiskRatingMapper.resetTierLevel(record);
		}
		return isForbid;
	}
}