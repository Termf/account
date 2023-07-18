package com.binance.account.service.face.channel.risk.handler;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.UserChannelRiskRatingRuleLevel;
import com.binance.account.common.enums.UserChannelRiskRatingRuleNo;
import com.binance.account.common.enums.UserChannelRiskRatingRuleParam;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskCountry;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserChannelRiskRatingRule;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.impl.NewUserWckBusiness;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingHandler;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingHandlerParam;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.rule.request.DecisionCommonRequest;
import com.binance.rule.response.DecisionCommonResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@Component
@UserChannelRiskRatingHandler(handlerType = {UserRiskRatingChannelCode.CHECKOUT, UserRiskRatingChannelCode.StandardBank,
		UserRiskRatingChannelCode.ClearJunction, UserRiskRatingChannelCode.FourBill, UserRiskRatingChannelCode.BCB,
		UserRiskRatingChannelCode.Qiwi, UserRiskRatingChannelCode.FlutterwaveNGN, UserRiskRatingChannelCode.FlutterwaveUGX, UserRiskRatingChannelCode.worldpay})
public class DefaultRiskRatingHandler extends AbstractChannelRiskRatingHandler {

	@Resource
	private NewUserWckBusiness newUserWckBusiness;

	@Resource
	private KycFillInfoMapper kycFillInfoMapper;

	@Override
	public void applyThirdPartyRisk(UserChannelRiskRating userChannelRiskRating, KycCertificate kycCertificate,
								   KycFillInfo kycFillInfo, UserKycApprove userKycApprove) {

		Long userId = userChannelRiskRating.getUserId();

		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			log.info("企业认证不支持上报worldcheck. userId:{}", kycCertificate.getUserId());
			return;
		}
		UserChannelRiskRatingHandlerParam info = buildUserInfo(kycCertificate, kycFillInfo, userKycApprove);
		String name = info.getName();
		String birthday = info.getBirthday();
		String country = info.getCountry();

		try {
			// 当前riskRating状态为forbid风险国家
			if (UserRiskRatingStatus.FORBID.name().equals(userChannelRiskRating.getStatus())) {
				log.warn("RiskRating变更tier等级,当前riskRating的状态为Forbid,不允许更改等级. userId:{},channel:{}",
						kycCertificate.getUserId(), userChannelRiskRating.getChannelCode());
				return;
			}

			if (isForbidCountry(country, userChannelRiskRating, name, birthday, kycFillInfo)) {
				return;
			}

			String origin = userKycApprove == null ? "BASE" : "KYC";
			log.info("开始申报wck. userId:{},name:{},birthday:{},country:{},checkName:{}", userId, name, birthday, country,
					origin);
			UserChannelWckAuditVo auditVo = newUserWckBusiness.applyWorldCheck(userId, origin, name.toString().trim(),
					birthday, country);

			UserChannelRiskRating record = new UserChannelRiskRating();
			record.setId(userChannelRiskRating.getId());
			record.setStatus(UserRiskRatingStatus.REVIEW.name());
			record.setUpdateTime(DateUtils.getNewUTCDate());
			userChannelRiskRatingMapper.updateByPrimaryKeySelective(record);
			userChannelRiskRating.setStatus(UserRiskRatingStatus.REVIEW.name());

			if (auditVo != null) {
				log.info("开始申报wck.已有wck申报记录. userId:{},name:{},birthday:{},country:{},checkName:{}", userId, name,
						birthday, country, origin);
				UserChannelRiskRatingHandlerParam param = new UserChannelRiskRatingHandlerParam();
				param.setUserChannelWckAuditVo(auditVo);
				auditVo.setNationality(country);
				auditVo.setBirthDate(birthday);
				auditVo.setCheckName(name);
				changeRiskRatingLevel(userChannelRiskRating, kycCertificate, param);
			}
		} catch (Exception e) {
			log.warn("上送worldCheck记录执行异常 userId:{}", userId, e);
		}
	}

	@Override
	public void changeRiskRatingLevel(UserChannelRiskRating userChannelRiskRating, KycCertificate kycCertificate,
									  UserChannelRiskRatingHandlerParam param) {
		UserChannelWckAuditVo userChannelWckAuditVo = param.getUserChannelWckAuditVo();

		if (kycCertificate == null) {
			log.warn("RiskRating变更tier等级,当前kyc为空");
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}
		if (userChannelRiskRating == null) {
			log.warn("RiskRating变更tier等级,当前riskRating为空");
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}
		Long userId = userChannelRiskRating.getUserId();
		String channelCode = userChannelRiskRating.getChannelCode();
		KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());

		if(baseInfo == null) {
			log.warn("RiskRating变更tier等级,当前base信息为空 {} {}", userId, channelCode);
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}

		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			log.warn("RiskRating变更tier等级 企业认证暂不支持. userId:{},channel:{}", userId, channelCode);
			throw new BusinessException(AccountErrorCode.RISK_RATING_NOT_SUPPORT_COMPANY);
		}

		if (UserRiskRatingStatus.REVIEW.name().equals(userChannelRiskRating.getStatus())
				&& userChannelWckAuditVo == null) {
			log.warn("RiskRating变更tier等级,当前riskRating的为Review,等待wck审核记录. userId:{},channel:{}", userId, channelCode);
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}
		// 封禁国家无法变更等级
		if (UserRiskRatingStatus.FORBID.name().equals(userChannelRiskRating.getStatus())) {
			log.warn("riskRating is forbid can not change,userId:{},channelCode:{}",userId, channelCode);
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}

		log.info("RiskRating变更tier等级,收到wck审核记录 userId:{},channel:{} wck:{}",userId, channelCode, userChannelWckAuditVo);

		// wck审核记录为空，地址验证结果、kyc降级、人工设置限额后升级tier3
		if (userChannelWckAuditVo == null) {
			log.info("RiskRating变更tier等级,地址验证结果或kyc降级触发. userId:{},channel:{}, tier:{}", userId, channelCode,
					userChannelRiskRating.getTierLevel());
			UserRiskRatingTierLevel level = nextTierLevel(userChannelRiskRating, kycCertificate);
			log.info("RiskRating变更tier等级更新. userId:{},channel:{},curLevel:{},nextLevel:{}", userId, channelCode,
					userChannelRiskRating.getTierLevel(), level);

			// tier等级未发生变更，不触发此逻辑
			if (!level.name().equals(userChannelRiskRating.getTierLevel())) {
				userChannelRiskRating.setId(userChannelRiskRating.getId());
				userChannelRiskRating.setTierLevel(level.name());
				boolean pullRisk = false;
				switch (level) {
					case NoTier:
						// NoTier kyc悲剧了。设置riskRating为不可用
						userChannelRiskRating.setStatus(UserRiskRatingStatus.DISABLE.name());
						userChannelRiskRating.setFailReason("Certificate Refused");
					case Tier0:
					case Tier1:
						userChannelRiskRating.setDailyLimit(null);
						userChannelRiskRating.setMonthlyLimit(null);
						userChannelRiskRating.setTotalLimit(null);
						break;
					case Tier2:
					case Tier3:
						pullRisk = true;
						break;
					default:
						break;
				}
				// 如果计算出tier2 则校验是否是风险国家
				if (UserRiskRatingTierLevel.Tier2.equals(level)) {
					KycFillInfo address = kycFillInfoMapper.selectByUserIdFillType(kycCertificate.getUserId(),
							KycFillType.ADDRESS.name());
					log.info("RiskRating变更tier等级.计算等级Tier2,校验风险国际 userId:{},channel:{}",userId,channelCode);
					if (address != null && isForbidCountry(address.getCountry(), userChannelRiskRating,
							userChannelRiskRating.getName(), userChannelRiskRating.getBirthday(), baseInfo)) {
						return;
					}
				}

				userChannelRiskRating.setUpdateTime(DateUtils.getNewDate());
				userChannelRiskRatingMapper.updateTierLevel(userChannelRiskRating);
				if (pullRisk) {
					pullRiskRating(userChannelRiskRating.getUserId(), userChannelRiskRating,
							new UserChannelRiskRatingHandlerParam());
				}
			}

			return;
		}
		// wck审核拒绝，直接重置tier信息.
		if (WckChannelStatus.REJECTED.equals(userChannelWckAuditVo.getStatus())) {
			log.info("RiskRating变更tier等级.wck审核拒绝. userId:{},channel:{}", userId, channelCode);
			userChannelRiskRating.setWorldCheckStatus(userChannelWckAuditVo.getStatus().name());
			userChannelRiskRating.setAuditTime(userChannelWckAuditVo.getUpdateTime());
			userChannelRiskRating.setTierLevel(UserRiskRatingTierLevel.NoTier.name());
			userChannelRiskRating.setStatus(UserRiskRatingStatus.DISABLE.name());
			userChannelRiskRating.setUpdateTime(DateUtils.getNewUTCDate());
			userChannelRiskRating.setCitizenshipCountry(userChannelWckAuditVo.getNationality());
			userChannelRiskRating.setResidenceCountry(StringUtils.isNotBlank(baseInfo.getResidenceCountry()) ? baseInfo.getResidenceCountry() 
					: StringUtils.isNotBlank(userChannelRiskRating.getResidenceCountry()) ? userChannelRiskRating.getResidenceCountry():baseInfo.getCountry());
			userChannelRiskRating.setName(userChannelWckAuditVo.getCheckName());
			userChannelRiskRating.setBirthday(userChannelWckAuditVo.getBirthDate());
			List<UserChannelWckAuditVo.UserChannelWckAuditLogVo> auditLogs = userChannelWckAuditVo.getAuditLogs();
			if (auditLogs != null && !auditLogs.isEmpty()) {
				for (UserChannelWckAuditVo.UserChannelWckAuditLogVo log : auditLogs) {
					if (log.getIsValid() != null && !log.getIsValid().booleanValue()) {
						userChannelRiskRating
								.setAuditorName(log.getAuditorId() == null ? null : log.getAuditorId().toString());
						userChannelRiskRating.setAuditRemark(log.getMemo());
						userChannelRiskRating.setWorldCheckFailReason(log.getFailReason());
					}
				}
			}
			userChannelRiskRating.setFailReason(userChannelRiskRating.getWorldCheckFailReason());
			userChannelRiskRatingMapper.resetTierLevel(userChannelRiskRating);
			sendWckRefusedEmail(kycCertificate.getUserId(), userChannelWckAuditVo.getNationality(),
					userChannelRiskRating.getWorldCheckFailReason());
			return;
		}

		String auditorName = null;
		String auditRemark = null;
		String wckFailReason = null;
		Date auditDate = userChannelWckAuditVo.getUpdateTime();
		Long isPep = null;
		Long sanctionsHits = null;

		if (WckChannelStatus.PASSED.equals(userChannelWckAuditVo.getStatus())) {
			List<UserChannelWckAuditVo.UserChannelWckAuditLogVo> auditLogs = userChannelWckAuditVo.getAuditLogs();
			for (UserChannelWckAuditVo.UserChannelWckAuditLogVo log : auditLogs) {
				if (new Integer(2).equals(log.getAuditorSeq())) {
					auditorName = log.getAuditorId() == null ? null : log.getAuditorId().toString();
					auditRemark = log.getMemo();
					wckFailReason = log.getFailReason();
					auditDate = log.getCreateTime();
					isPep = log.getIsPep();
					sanctionsHits = log.getSanctionsHits();
				}
			}
		} else {
			isPep = new Long(0);
			sanctionsHits = new Long(0);
		}
		userChannelRiskRating.setCitizenshipCountry(userChannelWckAuditVo.getNationality());
		userChannelRiskRating.setResidenceCountry(StringUtils.isNotBlank(baseInfo.getResidenceCountry()) ? baseInfo.getResidenceCountry() 
				: StringUtils.isNotBlank(userChannelRiskRating.getResidenceCountry()) ? userChannelRiskRating.getResidenceCountry():baseInfo.getCountry());
		userChannelRiskRating.setName(userChannelWckAuditVo.getCheckName());
		userChannelRiskRating.setBirthday(userChannelWckAuditVo.getBirthDate());
		userChannelRiskRating.setAuditorName(auditorName);
		userChannelRiskRating.setAuditRemark(auditRemark);
		userChannelRiskRating.setAuditTime(auditDate);
		userChannelRiskRating.setWorldCheckStatus(userChannelWckAuditVo.getStatus().name());
		userChannelRiskRating.setWorldCheckFailReason(wckFailReason);
		userChannelRiskRating.setUpdateTime(DateUtils.getNewUTCDate());
		UserRiskRatingTierLevel level = nextTierLevel(userChannelRiskRating, kycCertificate);
		String oldLevel = userChannelRiskRating.getTierLevel();
		// level不可能为空 到这一步 wck状态肯定成功
		userChannelRiskRating.setTierLevel(level.name());

		if (StringUtils.equalsAny(level.name(), UserRiskRatingTierLevel.NoTier.name(),
				UserRiskRatingTierLevel.Tier0.name(), UserRiskRatingTierLevel.Tier1.name())) {
			userChannelRiskRating.setDailyLimit(null);
			userChannelRiskRating.setMonthlyLimit(null);
			userChannelRiskRating.setTotalLimit(null);
		}

		userChannelRiskRating.setStatus(UserRiskRatingStatus.DISABLE.name());
		userChannelRiskRating.setFailReason(wckFailReason);

		// 如果计算出tier2 则校验是否是风险国家
		if (UserRiskRatingTierLevel.Tier2.equals(level)) {
			KycFillInfo address = kycFillInfoMapper.selectByUserIdFillType(kycCertificate.getUserId(),
					KycFillType.ADDRESS.name());
			log.info("RiskRating变更tier等级.计算等级Tier2,校验风险国际 userId:{},channel:{}", userId, channelCode);
			if (address != null && isForbidCountry(address.getCountry(), userChannelRiskRating,
					userChannelRiskRating.getName(), userChannelRiskRating.getBirthday(), baseInfo)) {
				return;
			}
		}


		log.info("RiskRating变更tier等级更新. userId:{},channel:{},curLevel:{},nextLevel:{}", kycCertificate.getUserId(),
				userChannelRiskRating.getChannelCode(),oldLevel, level);
		userChannelRiskRatingMapper.auditWckPass(userChannelRiskRating);

		if (!UserRiskRatingTierLevel.NoTier.name().equals(userChannelRiskRating.getTierLevel())) {
			param.setIsPep(isPep);
			param.setSanctionsHits(sanctionsHits);
			pullRiskRating(userChannelRiskRating.getUserId(), userChannelRiskRating, param);
		}

	}

	@Override
	public void pullRiskRating(Long userId, UserChannelRiskRating riskRating, UserChannelRiskRatingHandlerParam param) {
		Long isPep = param.getIsPep();
		Long sanctionsHits = param.getSanctionsHits();
		String channelCode = riskRating.getChannelCode();
		if (UserRiskRatingTierLevel.NoTier.name().equals(riskRating.getTierLevel())) {
			log.warn("RiskRating提交风控评分,当前用户risk_rating为NoTier，不允许提交风控. userId:{},channel:{},tierLevel:{}", userId,
					channelCode, riskRating.getTierLevel());
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PUSH_RISK);
		}

		if (!WckChannelStatus.PASSED.name().equals(riskRating.getWorldCheckStatus())
				&& !WckChannelStatus.AUTO_PASS.name().equals(riskRating.getWorldCheckStatus())) {
			log.warn("RiskRating提交风控评分,当前用户wck未通过，不允许提交风控. userId:{},channel:{},tierLevel:{}", userId,
					channelCode, riskRating.getTierLevel());
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PUSH_RISK);
		}

		List<UserChannelRiskRatingRule> rules = userChannelRiskRatingRuleMapper.selectByUserIdAndChannelCode(userId, riskRating.getChannelCode());

		if (rules == null || rules.isEmpty()) {
			log.warn("RiskRating提交风控评分,当前用户riskRule为空，不允许提交风控. userId:{},ratingId:{},channel:{}", userId, riskRating.getId(), channelCode);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		JSONArray data = buildRiskRequestBody(rules, riskRating, isPep, sanctionsHits);

		// 上送风控评级
		BigDecimal totalScore = riskCommonRule(userId, data, riskRating, rules);

		riskRating.setRiskRatingScore(totalScore);
		riskRating
				.setRiskRatingLevel(UserChannelRiskRatingRuleLevel.totalScore(riskRating.getRiskRatingScore()).name());

		log.info("RiskRating提交风控评分结果: userId:{},channel:{},riskLevel:{},riskScore:{}", userId, channelCode,
				riskRating.getRiskRatingLevel(), riskRating.getRiskRatingScore());
		// 91分以上 不可用
		if (UserChannelRiskRatingRuleLevel.Extreme.name().equals(riskRating.getRiskRatingLevel())) {
			riskRating.setStatus(UserRiskRatingStatus.DISABLE.name());
			riskRating.setFailReason("High Risk Level");
		} else {
			riskRating.setStatus(UserRiskRatingStatus.ENABLE.name());
		}
		riskRating.setUpdateTime(DateUtils.getNewUTCDate());
		userChannelRiskRatingMapper.updateRiskLevelScore(riskRating);
		if (UserRiskRatingStatus.DISABLE.name().equals(riskRating.getStatus())) {
			sendRiskRatingRefusedEmail(userId, riskRating.getCitizenshipCountry(), riskRating.getFailReason());
		}
	}

	private JSONArray buildRiskRequestBody(List<UserChannelRiskRatingRule> rules, UserChannelRiskRating riskRating,
										   Long isPep, Long sanctionsHits) {
		JSONArray data = new JSONArray();

		for (UserChannelRiskRatingRule rule : rules) {
			UserChannelRiskRatingRuleNo ruleNo = UserChannelRiskRatingRuleNo.getRuleByRuleNo(rule.getRuleNo());
			switch (ruleNo) {
				case RISK_PEP:// wck 审核返回PEP 允许操作员修改
					if (isPep != null) {
						UserChannelRiskRatingRuleParam param1 = UserChannelRiskRatingRuleParam.getParam(ruleNo,
								isPep.longValue() + "");
						rule.setRuleValue(isPep.longValue() + "");
						rule.setRuleLevel(param1.getLevel().name());
						rule.setRuleScore(param1.getScore());
						rule.setUpdateTime(DateUtils.getNewUTCDate());
						userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					}
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), rule.getRuleScore()));
					userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					break;
				case RISK_AGE:// 年龄kyc年龄
					rule.setRuleValue(riskRating.getBirthday());
					rule.setUpdateTime(DateUtils.getNewUTCDate());
					userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), null));
					break;
				case RISK_SANCTIONS_HITS:// wck 审核返回，允许操作员修改
					if (sanctionsHits != null) {
						UserChannelRiskRatingRuleParam param2 = UserChannelRiskRatingRuleParam.getParam(ruleNo,
								sanctionsHits.longValue() + "");
						rule.setRuleValue(isPep.longValue() + "");
						rule.setRuleLevel(param2.getLevel().name());
						rule.setRuleScore(param2.getScore());
						rule.setUpdateTime(DateUtils.getNewUTCDate());
						userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					}
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), rule.getRuleScore()));
					break;
				case RISK_BEHAVIOUR:// Good Fake 允许操作员修改，初始话rule就会塞good
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), rule.getRuleScore()));
					break;
				case RISK_MANUAL:// ongoingMonitoring 人工手提安，初始化rule就会有默认值
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), rule.getRuleValue()));
					break;
				case RISK_COUNTRY:// 居住国
					UserChannelRiskCountry riskCountry0 = userChannelRiskCountryMapper.selectByPrimaryKey(
							riskRating.getResidenceCountry(), riskRating.getChannelCode());
					rule.setRuleValue(riskRating.getResidenceCountry());
					rule.setRuleScore(riskCountry0 == null ? "0" : riskCountry0.getRiskScore());
					rule.setRuleLevel(riskCountry0 == null ? UserChannelRiskRatingRuleLevel.Lower.name()
							: riskCountry0.getRiskLevel());
					rule.setUpdateTime(DateUtils.getNewUTCDate());
					userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					data.add(createRiskEntry(ruleNo, riskRating.getResidenceCountry(),
							riskCountry0 == null ? "0" : riskCountry0.getRiskScore()));
					break;
				case RISK_NATIONALITY:// wck审核国籍信息
					UserChannelRiskCountry riskCountry1 = userChannelRiskCountryMapper.selectByPrimaryKey(
							riskRating.getCitizenshipCountry(), riskRating.getChannelCode());
					rule.setRuleValue(riskRating.getCitizenshipCountry());
					rule.setRuleScore(riskCountry1 == null ? "0" : riskCountry1.getRiskScore());
					rule.setRuleLevel(riskCountry1 == null ? UserChannelRiskRatingRuleLevel.Lower.name()
							: riskCountry1.getRiskLevel());
					rule.setUpdateTime(DateUtils.getNewUTCDate());
					userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					data.add(createRiskEntry(ruleNo, riskRating.getCitizenshipCountry(),
							riskCountry1 == null ? "0" : riskCountry1.getRiskScore()));
					break;
				case RISK_PRODUCT:// tierLevel
					rule.setRuleValue(riskRating.getTierLevel());
					rule.setUpdateTime(DateUtils.getNewUTCDate());
					userChannelRiskRatingRuleMapper.updateByPrimaryKey(rule);
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), null));
					break;
				case RISK_AVG_DAILY:// 申报金额，初始rule就会创建
					data.add(createRiskEntry(ruleNo, rule.getRuleValue(), null));
					break;
				default:
					break;
			}
		}
		return data;
	}

	private JSONObject createRiskEntry(UserChannelRiskRatingRuleNo ruleNo, String value, String score) {
		JSONObject json = new JSONObject();
		json.put("ruleNo", ruleNo.getRuleNo());
		json.put("value", value);
		json.put("score", score);
		return json;
	}

	private BigDecimal riskCommonRule(Long userId, JSONArray contextJson, UserChannelRiskRating riskRating,
									  List<UserChannelRiskRatingRule> rules) {
		try {
			DecisionCommonRequest request = new DecisionCommonRequest();
			Map<String, Object> context = new HashMap<>();
			context.put("rating_params", contextJson.toString());
			context.put("uid", userId);
			context.put("channel", riskRating.getChannelCode());
			context.put("baseAsset", riskRating.getLimitUnit());
			request.setEventCode("risk_rating");
			request.setContext(context);
			log.info("调用风控riskRating请求 userId:{},channelCode:{}, context:{}", userId, riskRating.getChannelCode(),contextJson);
			APIResponse<DecisionCommonResponse> response = commonRiskApi.commonRule(APIRequest.instance(request));
			if (response == null || response.getData() == null) {
				log.info("调用风控riskRating响应为空 userId:{},context:{}", userId, contextJson);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			log.info("commonRule userId:{}, channelCode:{},resp:{}", userId, riskRating.getChannelCode(), response);
			DecisionCommonResponse data = response.getData();

			Map<String, Object> extend = data.getExtend();

			if (extend == null || extend.isEmpty()) {
				log.warn("调用风控riskRating响应extend为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}

			if (extend.get("totalScore") == null) {
				log.warn("调用风控riskRating响应总分值为空 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			BigDecimal totalScore = new BigDecimal(extend.get("totalScore").toString());

			for (UserChannelRiskRatingRule rule : rules) {
				try {
					UserChannelRiskRatingRuleNo ruleNo = UserChannelRiskRatingRuleNo.getRuleByRuleNo(rule.getRuleNo());
					switch (ruleNo) {
						case RISK_AGE:// 年龄kyc年龄
						case RISK_PRODUCT:// tierLevel
						case RISK_AVG_DAILY:// 申报金额，初始rule就会创建
						case RISK_MANUAL:
							Object object = extend.get(ruleNo.getRuleNo());
							BigDecimal scope = new BigDecimal(object.toString());
							rule.setRuleLevel(UserChannelRiskRatingRuleLevel.detailScore(ruleNo, scope).name());
							rule.setRuleScore(scope.toString());
							rule.setUpdateTime(DateUtils.getNewUTCDate());
							userChannelRiskRatingRuleMapper.updateRiskLevelScore(rule);
							break;
						default:
							break;
					}
				} catch (Exception e) {
					log.warn("调用风控riskRating处理异常.userId:{},rule:{},riskReturn:{}", userId, rule.getRuleNo(),
							extend.get(rule.getRuleNo()));
					rule.setRuleLevel(UserChannelRiskRatingRuleLevel.Unknow.name());
					rule.setRuleScore(
							extend.get(rule.getRuleNo()) == null ? null : extend.get(rule.getRuleNo()).toString());
					rule.setUpdateTime(DateUtils.getNewUTCDate());
					userChannelRiskRatingRuleMapper.updateRiskLevelScore(rule);
				}
			}
			return totalScore;
		} catch (Exception e) {
			log.warn("调用风控riskRating异常. userId:{},channel:{}", userId,riskRating.getChannelCode(), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	@Override
	public void initRiskRatingRules(UserChannelRiskRating rating) {
		List<UserChannelRiskRatingRuleNo> ruleNos = UserChannelRiskRatingRuleNo
				.getRuleByChannelCode(rating.getChannelCode());
		for (UserChannelRiskRatingRuleNo userChannelRiskRatingRuleNo : ruleNos) {
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
					UserChannelRiskRatingRuleLevel level = UserChannelRiskRatingRuleLevel.detailScore(
							UserChannelRiskRatingRuleNo.RISK_MANUAL,
							new BigDecimal(UserChannelRiskRatingRuleParam.OngoingMonitoring.getParamValue()));
					userChannelRiskRatingRule
							.setRuleValue(UserChannelRiskRatingRuleParam.OngoingMonitoring.getParamValue());
					userChannelRiskRatingRule.setRuleLevel(level.name());
					userChannelRiskRatingRule
							.setRuleScore(UserChannelRiskRatingRuleParam.OngoingMonitoring.getParamValue());
					break;
				case RISK_AVG_DAILY:
					userChannelRiskRatingRule.setRuleValue(rating.getApplyAmount());
					break;
				case RISK_COUNTRY:
					UserChannelRiskCountry riskCountry2 = userChannelRiskCountryMapper
							.selectByPrimaryKey(rating.getResidenceCountry(), rating.getChannelCode());
					userChannelRiskRatingRule.setRuleValue(rating.getResidenceCountry());
					userChannelRiskRatingRule.setRuleScore(riskCountry2 == null ? "0" : riskCountry2.getRiskScore());
					userChannelRiskRatingRule
							.setRuleLevel(riskCountry2 == null ? UserChannelRiskRatingRuleLevel.Lower.name()
									: riskCountry2.getRiskLevel());
					break;
				default:
					break;
			}
			userChannelRiskRatingRule.setCreateTime(DateUtils.getNewDate());
			userChannelRiskRatingRule.setUpdateTime(DateUtils.getNewDate());
			userChannelRiskRatingRuleMapper.insert(userChannelRiskRatingRule);
		}
	}

}
