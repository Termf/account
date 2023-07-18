package com.binance.account.service.face.channel.risk.handler;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskCountryMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingRuleMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IKycCertificate;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingHandlerParam;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.certificate.request.RiskRatingApplyRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.rule.api.CommonRiskApi;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Resource;
import java.util.Map;

@Log4j2
public abstract class AbstractChannelRiskRatingHandler {

	@Resource
	UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Resource
	UserChannelRiskRatingRuleMapper userChannelRiskRatingRuleMapper;

	@Resource
	UserChannelRiskCountryMapper userChannelRiskCountryMapper;

	@Resource
	CommonRiskApi commonRiskApi;

	@Resource
	KycCertificateMapper kycCertificateMapper;

	@Resource
	UserKycApproveMapper userKycApproveMapper;

	@Resource
	KycFillInfoMapper kycFillInfoMapper;

	@Resource
	IKycCertificate iKycCertificate;

	@Resource
	UserIndexMapper userIndexMapper;

	@Resource
	UserMapper userMapper;

	@Resource
	UserCommonBusiness userCommonBusiness;

	@Autowired
	ApolloCommonConfig config;

	public abstract void applyThirdPartyRisk(UserChannelRiskRating userChannelRiskRating, KycCertificate kycCertificate,
			KycFillInfo kycFillInfo, UserKycApprove userKycApprove);

	public UserChannelRiskRating getUserChannelRiskRating(Long userId, UserRiskRatingChannelCode channelCode) {
		return userChannelRiskRatingMapper.selectByUk(userId, channelCode.name());
	}

	public abstract void changeRiskRatingLevel(UserChannelRiskRating userChannelRiskRating,
			KycCertificate kycCertificate, UserChannelRiskRatingHandlerParam param);

	public abstract void pullRiskRating(Long userId, UserChannelRiskRating riskRating,
			UserChannelRiskRatingHandlerParam param);

	public abstract void initRiskRatingRules(UserChannelRiskRating rating);

	public UserChannelRiskRating applyRiskRating(RiskRatingApplyRequest riskRatingApply) {

		if (riskRatingApply.getUserId() == null || riskRatingApply.getChannelCode() == null
				|| riskRatingApply.getApplyAmount() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		log.info("开始发起RiskRating申报 userId:{} {}", riskRatingApply.getUserId(), JSON.toJSONString(riskRatingApply));

//		if ( StringUtils.isBlank(riskRatingApply.getResidenceCountry())) {
//			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
//		}

		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByUk(riskRatingApply.getUserId(),
				riskRatingApply.getChannelCode().getCode());

		if (riskRating != null) {
//			throw new BusinessException(AccountErrorCode.RISK_RATING_HAS_APPLY);
			return riskRating;
		}

		// 判断kyc情况
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(riskRatingApply.getUserId());

		if (kycCertificate != null && KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			log.info("企业认证暂不支持. userId:{}", riskRatingApply.getUserId());
			throw new BusinessException(AccountErrorCode.RISK_RATING_NOT_SUPPORT_COMPANY);
		}

		// kyc没有 只添加记录，
		// address通过，只添加记录
		riskRating = new UserChannelRiskRating();
		riskRating.setUserId(riskRatingApply.getUserId());
		riskRating.setChannelCode(riskRatingApply.getChannelCode().getCode());
		riskRating.setTierLevel(UserRiskRatingTierLevel.NoTier.name());
		riskRating.setLimitUnit(riskRatingApply.getChannelCode().getCurrency());
		riskRating.setApplyAmount(
				riskRatingApply.getApplyAmount() == null ? "0" : riskRatingApply.getApplyAmount().toString());
		riskRating.setStatus(UserRiskRatingStatus.DISABLE.name());
		riskRating.setResidenceCountry(StringUtils.isBlank(riskRatingApply.getResidenceCountry()) ? null
				: riskRatingApply.getResidenceCountry().toUpperCase());
		riskRating.setIpAddress(WebUtils.getRequestIp());
		riskRating.setCardCountry(StringUtils.isBlank(riskRatingApply.getCardCountry()) ? null
				: riskRatingApply.getCardCountry().toUpperCase());
		riskRating.setCreateTime(DateUtils.getNewDate());
		riskRating.setUpdateTime(DateUtils.getNewDate());
		
		userChannelRiskRatingMapper.insert(riskRating);
		// 初始化rules
		initRiskRatingRules(riskRating);
		if (kycCertificate == null) {
			return riskRating;
		}

		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(riskRating.getUserId(),
				KycFillType.BASE.name());

		// base || kyc通过，添加记录+上送wck
//		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
//			applyThirdParyRisk(riskRating, kycCertificate, kycFillInfo, null);
//		}

		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(riskRating.getUserId());
			applyThirdPartyRisk(riskRating, kycCertificate, kycFillInfo, userKycApprove);
		}
		return riskRating;
	}

	public UserChannelRiskRatingHandlerParam buildUserInfo(KycCertificate kycCertificate, KycFillInfo kycFillInfo,
			UserKycApprove userKycApprove) {
		String name = "";
		String birthday = "";
		String country = "";

		if (kycFillInfo != null) {
			StringBuffer nameBuff = new StringBuffer()
					.append(StringUtils.isBlank(kycFillInfo.getFirstName()) ? "" : kycFillInfo.getFirstName().trim())
					.append(StringUtils.isBlank(kycFillInfo.getMiddleName()) ? ""
							: " " + kycFillInfo.getMiddleName().trim())
					.append(StringUtils.isBlank(kycFillInfo.getLastName()) ? ""
							: " " + kycFillInfo.getLastName().trim());
			name = nameBuff.toString();
			birthday = kycFillInfo.getBirthday();
			country = kycFillInfo.getCountry();
		}

		if (userKycApprove != null) {

			iKycCertificate.syncCertificateInfo(userKycApprove, kycCertificate, kycFillInfo);

			StringBuffer nameBuff = new StringBuffer()
					.append((StringUtils.isBlank(userKycApprove.getCertificateFirstName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateFirstName()))  ? ""
							: userKycApprove.getCertificateFirstName().trim().replaceAll("N/A", ""))
					.append((StringUtils.isBlank(userKycApprove.getCertificateLastName()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateLastName())) ? ""
							: " " + userKycApprove.getCertificateLastName().trim().replaceAll("N/A", ""));
			String dob = ((StringUtils.isBlank(userKycApprove.getCertificateDob()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateDob())) ? ""
					: userKycApprove.getCertificateDob()).trim().replaceAll("N/A", "");
			String certificateCountry = ((StringUtils.isBlank(userKycApprove.getCertificateCountry()) || "NA".equalsIgnoreCase(userKycApprove.getCertificateCountry())) ? ""
					: userKycApprove.getCertificateCountry()).trim().replaceAll("N/A", "");

			birthday = StringUtils.isBlank(dob) ? birthday : dob;
			country = StringUtils.isBlank(certificateCountry) ? country : certificateCountry;
			name = nameBuff.toString().replaceAll("N/A", "").trim();
		}
		UserChannelRiskRatingHandlerParam param = new UserChannelRiskRatingHandlerParam();
		param.buildUserInfo(name, birthday, country);
		return param;
	}

	public UserRiskRatingTierLevel nextTierLevel(UserChannelRiskRating riskRating, KycCertificate kycCertificate) {
		if (!WckChannelStatus.PASSED.name().equals(riskRating.getWorldCheckStatus())
				&& !WckChannelStatus.AUTO_PASS.name().equals(riskRating.getWorldCheckStatus())) {
			log.info("RiskRating变更tier等级，wck状态不为通过.暂不处理 userId:{},WorldCheckStatus:{}", kycCertificate.getUserId(),
					riskRating.getWorldCheckStatus());
			return UserRiskRatingTierLevel.NoTier;
		}

		UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());

		// tier2 || tier3
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())
				&& KycCertificateStatus.PASS.name().equals(kycCertificate.getAddressStatus())) {
			// 乌克兰4Bill和俄罗斯Qiwi渠道，只有Tier1
			if (UserRiskRatingChannelCode.FourBill.equals(channelCode) || UserRiskRatingChannelCode.Qiwi.equals(channelCode)) {
				log.info("4Bill only tier1,userId:{},", riskRating.getUserId());
				return UserRiskRatingTierLevel.Tier1;
			}
			if (riskRating.getDailyLimit() != null
					&& channelCode.getDailyLimit().compareTo(riskRating.getDailyLimit()) < 0) {
				log.info("RiskRating变更tier等级为tier3. userId:{},kyc:{},base:{},address:{}", kycCertificate.getUserId(),
						kycCertificate.getStatus(), kycCertificate.getBaseFillStatus(),
						kycCertificate.getAddressStatus());
				return UserRiskRatingTierLevel.Tier3;
			}
			log.info("RiskRating变更tier等级为tier2. userId:{},kyc:{},base:{},address:{}", kycCertificate.getUserId(),
					kycCertificate.getStatus(), kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
			return UserRiskRatingTierLevel.Tier2;
		}

		// tier1
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			log.info("RiskRating变更tier等级为tier1. userId:{},kyc:{},base:{},address:{}", kycCertificate.getUserId(),
					kycCertificate.getStatus(), kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
			return UserRiskRatingTierLevel.Tier1;
		}

		// tier0
		if (KycCertificateStatus.PASS.name().equals(kycCertificate.getBaseFillStatus())) {
			log.info("RiskRating变更tier等级为tier0. userId:{},kyc:{},base:{},address:{}", kycCertificate.getUserId(),
					kycCertificate.getStatus(), kycCertificate.getBaseFillStatus(), kycCertificate.getAddressStatus());
			return UserRiskRatingTierLevel.Tier0;
		}
		log.info("RiskRating变更tier等级,kyc,base未通过. userId:{},kyc:{},base:{},address:{}", kycCertificate.getUserId(),
				kycCertificate.getStatus(), kycCertificate.getAddressStatus());
		return UserRiskRatingTierLevel.NoTier;
	}

	public void sendWckRefusedEmail(Long userId, String country, String failReason) {
		LanguageEnum language = StringUtils.isEmpty(country) ? LanguageEnum.EN_US
				: LanguageEnum.findByLang(country.toLowerCase());
		sendEmail(AccountConstants.RISK_RATING_WORLDCHECK_REFUSED, failReason, language, userId, "WCH审核拒绝邮件");
	}

	public void sendRiskRatingRefusedEmail(Long userId, String country, String failReason) {
		LanguageEnum language = StringUtils.isEmpty(country) ? LanguageEnum.EN_US
				: LanguageEnum.findByLang(country.toLowerCase());
		sendEmail(AccountConstants.RISK_RATING_REFUSED, failReason, language, userId, "RiskRating禁用邮件");
	}

	private void sendEmail(String tplCode, String failReason, LanguageEnum language, Long userId, String remark) {
		UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
		final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());

		Map<String, Object> data = Maps.newHashMap();
		String reasonMsg;
		if (language == LanguageEnum.ZH_CN) {
			reasonMsg = userCommonBusiness.getJumioFailReason(failReason, true);
		} else {
			reasonMsg = userCommonBusiness.getJumioFailReason(failReason, false);
		}
		if (StringUtils.isNotBlank(reasonMsg)) {
			data.put("reason", reasonMsg);
		}
		log.info("发送riskRating邮件 userId:{} remark:{} language:{} reasonMsg:{}", userId, remark, language, reasonMsg);
		userCommonBusiness.sendEmailWithoutRequest(tplCode, dbUser, data, remark, language);
	}

	public boolean isForbidCountry(String countryCode, UserChannelRiskRating riskRating, String name, String birthday,
								   KycFillInfo kycFillInfo) {
		boolean isForbid = config.isForbidCountry(countryCode, riskRating.getChannelCode());

		if (isForbid) {
			log.info("认证国家为风险国家,关闭RiskRating. userId:{},name:{},birthday:{},country:{}", riskRating.getUserId(), name,
					birthday, countryCode);
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
