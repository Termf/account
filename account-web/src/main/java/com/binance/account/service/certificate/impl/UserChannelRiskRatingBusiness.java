package com.binance.account.service.certificate.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.UserChannelRiskRatingRuleLevel;
import com.binance.account.common.enums.UserChannelRiskRatingRuleNo;
import com.binance.account.common.enums.UserChannelRiskRatingRuleParam;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.common.enums.UserRiskRatingTierLevel;
import com.binance.account.common.query.UserChannelRiskCountryQuery;
import com.binance.account.common.query.UserChannelRiskRatingQuery;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserChannelRiskCountry;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserChannelRiskRatingRule;
import com.binance.account.data.entity.certificate.UserChannelRiskRatingRuleHistory;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskCountryMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingRuleHistoryMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingRuleMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingContext;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingHandlerParam;
import com.binance.account.service.kyc.MessageMapHelper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.UserChannelRiskCountryVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingRuleVo;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.certificate.request.ChannelRiskRatingRuleAuditRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyRequest;
import com.binance.account.vo.certificate.request.RiskRatingApplyResponse;
import com.binance.account.vo.certificate.request.RiskRatingChangeLimitRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeStatusRequest;
import com.binance.account.vo.certificate.request.RiskRatingChangeTierLevelRequest;
import com.binance.account.vo.certificate.request.RiskRatingLimitResponse;
import com.binance.account.vo.certificate.response.UserRiskRatingTierLevelVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.commons.SearchResult;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.rule.api.CommonRiskApi;
import com.binance.rule.request.DecisionCommonRequest;
import com.binance.rule.response.DecisionCommonResponse;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserChannelRiskRatingBusiness implements IUserChannelRiskRating {

	@Resource
	private UserMapper userMapper;
	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;
	@Resource
	private KycCertificateMapper kycCertificateMapper;
	@Resource
	private UserChannelRiskRatingRuleMapper userChannelRiskRatingRuleMapper;
	@Resource
	private UserChannelRiskRatingRuleHistoryMapper userChannelRiskRatingRuleHistoryMapper;
	@Resource
	private UserChannelRiskCountryMapper userChannelRiskCountryMapper;
	@Resource
	private CommonRiskApi commonRiskApi;
	@Resource
	private UserChannelRiskRatingContext userChannelRiskRatingContext;
	@Resource
	private UserIndexMapper userIndexMapper;
	@Resource
	private UserCommonBusiness userCommonBusiness;
	// 各个tier等级对应限额信息，修改可能性比较低，有修改的话最长60分后生效
	private Cache<String, Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo>> tierCache = CacheBuilder.newBuilder()
				.expireAfterWrite(60, TimeUnit.MINUTES).build();

	private User getUserByEmail(String email) {
		final User user = this.userMapper.queryByEmail(email);
		if (user == null) {
			throw new BusinessException(GeneralCode.USER_NOT_EXIST);
		}
		return user;
	}

	private UserRiskRatingTierLevelVo defaultTierLimit() {
		UserRiskRatingTierLevelVo defaultLevel = new UserRiskRatingTierLevelVo();
		defaultLevel.setDailyLimit(BigDecimal.ZERO);
		defaultLevel.setMonthlyLimit(BigDecimal.ZERO);
		defaultLevel.setTotalLimit(BigDecimal.ZERO);
		defaultLevel.setYearlyLimit(BigDecimal.ZERO);
		defaultLevel.setWithdrawDailyLimit(BigDecimal.ZERO);
		defaultLevel.setWithdrawMonthlyLimit(BigDecimal.ZERO);
		defaultLevel.setWithdrawYearlyLimit(BigDecimal.ZERO);
		defaultLevel.setWithdrawTotalLimit(BigDecimal.ZERO);
		return defaultLevel;
	}

	@Override
	public SearchResult<UserChannelRiskRatingVo> getUserChannelRiskRatings(UserChannelRiskRatingQuery riskRatingQuery) {
		if (StringUtils.isNotBlank(riskRatingQuery.getEmail())) {
			riskRatingQuery.setUserId(getUserByEmail(riskRatingQuery.getEmail()).getUserId());
		}
		long count = userChannelRiskRatingMapper.getPageCount(riskRatingQuery);
		if (count <= 0) {
			return new SearchResult<>(Collections.emptyList(), 0);
		}
		UserRiskRatingTierLevelVo defaultLevel = defaultTierLimit();
		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.getPageList(riskRatingQuery);

		// 把这部分用户的地址认证状态显示到这个里面
		Set<Long> userIdSet = ratings.stream().map(item -> item.getUserId()).collect(Collectors.toSet());
		// 查询用户的所有认证的地址信息
		List<Long> userIds =  new ArrayList<>(userIdSet);
		List<KycCertificate> list = kycCertificateMapper.queryByIdList(userIds);
		final Map<Long, String> addressStatusMap = new HashMap<>();
		for (KycCertificate kycCertificate : list) {
			addressStatusMap.put(kycCertificate.getUserId(), kycCertificate.getAddressStatus());
		}
		List<UserChannelRiskRatingVo> riskRatingVos = ratings.stream().map(item -> {
			UserChannelRiskRatingVo vo = new UserChannelRiskRatingVo();
			BeanUtils.copyProperties(item, vo);
			if (vo.getRiskRatingScore() == null) {
				vo.setRiskRatingScore(BigDecimal.ZERO);
			}
			vo.setAddressStatus(addressStatusMap.get(vo.getUserId()));
			String channelCode = vo.getChannelCode();
			Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> tierMap = tierCache.getIfPresent(channelCode);
			if (tierMap == null) {
				tierMap = getRiskTierLevelLimit(channelCode);
				if (tierMap != null) {
					tierCache.put(channelCode, tierMap);
				}
			}
			UserRiskRatingTierLevel tier = UserRiskRatingTierLevel.valueOf(vo.getTierLevel());
			// tier0已被移除，但还是存在历史数据
			UserRiskRatingTierLevelVo levelVo = tierMap == null || UserRiskRatingTierLevel.Tier0.equals(tier) ?
					defaultLevel : tierMap.get(tier);

			if (vo.getDailyLimit() == null) {
				vo.setDailyLimit(levelVo.getDailyLimit());
			}
			if (vo.getMonthlyLimit() == null) {
				vo.setMonthlyLimit(levelVo.getMonthlyLimit());
			}
			if (vo.getTotalLimit() == null) {
				vo.setTotalLimit(levelVo.getTotalLimit());
			}
			if (vo.getYearlyLimit() == null) {
				vo.setYearlyLimit(levelVo.getYearlyLimit());
			}
			if (vo.getWithdrawDailyLimit() == null) {
				vo.setWithdrawDailyLimit(levelVo.getWithdrawDailyLimit());
			}
			if (vo.getWithdrawMonthlyLimit() == null) {
				vo.setWithdrawMonthlyLimit(levelVo.getWithdrawMonthlyLimit());
			}
			if (vo.getWithdrawYearlyLimit() == null) {
				vo.setWithdrawYearlyLimit(levelVo.getWithdrawYearlyLimit());
			}
			if (vo.getWithdrawTotalLimit() == null) {
				vo.setWithdrawTotalLimit(levelVo.getWithdrawTotalLimit());
			}
			return vo;
		}).collect(Collectors.toList());

		return new SearchResult<>(riskRatingVos, count);
	}

	@Override
	public void changeUserRiskRatingStatus(RiskRatingChangeStatusRequest request) {
		log.info("change user channel risk rating status by:{}", JSON.toJSONString(request));
		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByPrimaryKey(request.getId());
		if (riskRating == null || !riskRating.getUserId().equals(request.getUserId())
				|| !StringUtils.equalsIgnoreCase(riskRating.getChannelCode(), request.getChannelCode())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		UserRiskRatingStatus riskRatingStatus = UserRiskRatingStatus.of(request.getStatus());
		if (riskRatingStatus == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		if(UserRiskRatingStatus.DISABLE.equals(riskRatingStatus) && StringUtils.isBlank(request.getFailReason())) {
			log.warn("禁用riskRating缺失failReason userId:{} ratingId:{}",request.getUserId(),request.getId());
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		if(UserRiskRatingStatus.ENABLE.equals(riskRatingStatus)
				&& UserRiskRatingStatus.FORBID.name().equals(riskRating.getStatus())) {
			log.warn("启用riskRating失败，风险国家不允许启用 userId:{},ratingId:{}",request.getUserId(),request.getId());
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}

		riskRating.setFailReason(request.getFailReason());
		riskRating.setStatus(riskRatingStatus.name());
		riskRating.setUpdateTime(DateUtils.getNewUTCDate());
		userChannelRiskRatingMapper.updateByPrimaryKeySelective(riskRating);

		//发送失败邮件
		if(UserRiskRatingStatus.DISABLE.equals(riskRatingStatus)) {
			UserIndex userIndex = userIndexMapper.selectByPrimaryKey(riskRating.getUserId());
			final User dbUser = userMapper.queryByEmail(userIndex.getEmail());
			LanguageEnum language = StringUtils.isEmpty(riskRating.getCitizenshipCountry()) ? LanguageEnum.EN_US
					: LanguageEnum.findByLang(riskRating.getCitizenshipCountry().toLowerCase());
			Map<String, Object> data = Maps.newHashMap();
			String reasonMsg;
			if (language == LanguageEnum.ZH_CN) {
				reasonMsg = userCommonBusiness.getJumioFailReason(riskRating.getFailReason(), true);
			} else {
				reasonMsg = userCommonBusiness.getJumioFailReason(riskRating.getFailReason(), false);
			}
			if (StringUtils.isNotBlank(reasonMsg)) {
				data.put("reason", reasonMsg);
			}
			log.info("发送riskRating审核邮件. userId:{},status:{},reasonMsg:{},language:{}",riskRating.getUserId(),riskRatingStatus,reasonMsg,language);
			userCommonBusiness.sendEmailWithoutRequest(AccountConstants.RISK_RATING_REFUSED, dbUser, data,
					"riskRating审核结果邮件", language);
		}
	}

	@Override
	public UserChannelRiskRatingVo getUserChannelRiskRating(Long userId, String channelCode) {
		if (userId == null || StringUtils.isBlank(channelCode)) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		UserChannelRiskRating userChannelRiskRating = userChannelRiskRatingMapper.selectByUk(userId, channelCode);
		if (userChannelRiskRating == null) {
			return null;
		}
		UserChannelRiskRatingVo vo = new UserChannelRiskRatingVo();
		BeanUtils.copyProperties(userChannelRiskRating, vo);
		return vo;
	}

	@Override
	public UserChannelRiskRatingVo getUserChannelRiskRating(Long userId, Integer riskRatingId) {
		if (userId == null || riskRatingId == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		UserChannelRiskRating userChannelRiskRating = userChannelRiskRatingMapper.selectByPrimaryKey(riskRatingId);
		if (userChannelRiskRating == null) {
			return null;
		}
		if (!userChannelRiskRating.getUserId().equals(userId)) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		UserChannelRiskRatingVo vo = new UserChannelRiskRatingVo();
		BeanUtils.copyProperties(userChannelRiskRating, vo);
		return vo;
	}

	/**
	 * isPep sanctionsHits 为空则从rule里面取
	 *
	 */
	public void pullRiskRating(Long userId, UserChannelWckAuditVo wckAudit, UserChannelRiskRating riskRating,
			Long isPep, Long sanctionsHits) {
		if (riskRating != null) {
			UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());
			UserChannelRiskRatingHandlerParam param = new UserChannelRiskRatingHandlerParam();
			param.buildWckResult(wckAudit, isPep, sanctionsHits);
			userChannelRiskRatingContext.getRatingHandler(channelCode).pullRiskRating(userId, riskRating, param);
			return;
		}

		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(userId);
		for (UserChannelRiskRating userChannelRiskRating : ratings) {
			try {
				UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode
						.valueOf(userChannelRiskRating.getChannelCode());
				UserChannelRiskRatingHandlerParam param = new UserChannelRiskRatingHandlerParam();
				param.buildWckResult(wckAudit, isPep, sanctionsHits);
				userChannelRiskRatingContext.getRatingHandler(channelCode).pullRiskRating(userId, userChannelRiskRating,
						param);
			} catch (Exception e) {
				log.warn("上送风控riskRating打分异常 userId:{},ratingId:{},channelCode:{}", userChannelRiskRating.getUserId(),
						userChannelRiskRating.getId(), userChannelRiskRating.getChannelCode(), e);
			}
		}
	}

	@Override
	public void changeUserRiskRatingTierLevel(RiskRatingChangeTierLevelRequest request) {
		log.info("change user channel risk rating tier level by:{}", JSON.toJSONString(request));
		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByPrimaryKey(request.getId());
		if (riskRating == null || !riskRating.getUserId().equals(request.getUserId())
				|| !StringUtils.equalsIgnoreCase(riskRating.getChannelCode(), request.getChannelCode())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		riskRating.setTierLevel(UserRiskRatingTierLevel.valueOf(request.getTierLevel()).name());
		riskRating.setUpdateTime(DateUtils.getNewUTCDate());
		userChannelRiskRatingMapper.updateByPrimaryKeySelective(riskRating);
	}

	@Override
	public void changeRiskRatingLimit(RiskRatingChangeLimitRequest request) {
		log.info("修改riskRating额度 userId:{},channeCode:{},request:{}", request.getUserId(), request.getChannelCode(),
				request);

		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByPrimaryKey(request.getId());
		if (riskRating == null || !riskRating.getUserId().equals(request.getUserId())
				|| !StringUtils.equalsIgnoreCase(riskRating.getChannelCode(), request.getChannelCode())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		if (!UserRiskRatingStatus.ENABLE.name().equals(riskRating.getStatus())
				&& !UserRiskRatingTierLevel.Tier2.name().equals(riskRating.getStatus())
				&& !UserRiskRatingTierLevel.Tier3.name().equals(riskRating.getStatus())) {
			throw new BusinessException(AccountErrorCode.RISK_RATING_CANT_PROCESS);
		}

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(request.getUserId());

		riskRating.setDailyLimit(request.getDailyLimit());
		riskRating.setMonthlyLimit(request.getMonthlyLimit());
		riskRating.setTotalLimit(request.getTotalLimit());
		riskRating.setYearlyLimit(request.getYearlyLimit());
		riskRating.setWithdrawDailyLimit(request.getWithdrawDailyLimit());
		riskRating.setWithdrawMonthlyLimit(request.getWithdrawMonthlyLimit());
		riskRating.setWithdrawYearlyLimit(request.getWithdrawYearlyLimit());
		riskRating.setWithdrawTotalLimit(request.getWithdrawTotalLimit());
		riskRating.setUpdateTime(DateUtils.getNewUTCDate());
		userChannelRiskRatingMapper.updateLimit(riskRating);

		userChannelRiskRatingContext.getRatingHandler(UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode()))
				.changeRiskRatingLevel(riskRating, kycCertificate, new UserChannelRiskRatingHandlerParam());
	}

	/**
	 * 查询对应的交易规则信息
	 *
	 * @param userId
	 * @param riskRatingId
	 * @return
	 */
	public List<UserChannelRiskRatingRuleVo> getRiskRuleInfoByRiskRatingId(Long userId, Integer riskRatingId) {
		if (userId == null || riskRatingId == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		List<UserChannelRiskRatingRule> histories = userChannelRiskRatingRuleMapper
				.selectByUserIdAndRiskRatingId(userId, riskRatingId);
		if (histories.isEmpty()) {
			return Collections.emptyList();
		}
		List<UserChannelRiskRatingRuleVo> ruleVos = histories.stream().map(item -> {
			UserChannelRiskRatingRuleVo ruleVo = new UserChannelRiskRatingRuleVo();
			BeanUtils.copyProperties(item, ruleVo);
			return ruleVo;
		}).collect(Collectors.toList());
		return ruleVos;
	}

	/**
	 * 重跑用户的评分规则
	 *
	 * @param userId
	 * @param riskRatingId
	 */
	public void redoRiskRatingRule(Long userId, Integer riskRatingId) {
		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByPrimaryKey(riskRatingId);
		if (riskRating == null) {
			log.info("redoRiskRatingRule 当前用户riskRating记录为空. userId:{},riskRatingId:{}", userId, riskRatingId);
			return;
		}
		pullRiskRating(userId, null, riskRating, null, null);
	}

	/**
	 * 审核变更评分规则
	 *
	 * @param request
	 */
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void auditRiskRule(ChannelRiskRatingRuleAuditRequest request) {
		final Long userId = request.getUserId();
		final Integer riskRatingId = request.getRiskRatingId();
		final String auditor = request.getAuditor();
		if (userId == null || riskRatingId == null || request.getRulesValue() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		final Map<String, String> ruleValueMap = request.getRulesValue();
		List<UserChannelRiskRatingRule> rules = userChannelRiskRatingRuleMapper.selectByUserIdAndRiskRatingId(userId,
				riskRatingId);
		if (rules.isEmpty()) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		// 把当前数据全移动到历史表
		List<UserChannelRiskRatingRuleHistory> histories = rules.stream().map(item -> {
			UserChannelRiskRatingRuleHistory history = new UserChannelRiskRatingRuleHistory();
			BeanUtils.copyProperties(item, history);
			history.setId(null);
			return history;
		}).collect(Collectors.toList());
		userChannelRiskRatingRuleHistoryMapper.insertBatch(histories);
		// 循环设置信息
		for (UserChannelRiskRatingRule rule : rules) {
			String ruleValue = ruleValueMap.get(rule.getRuleNo());
			if (ruleValue == null) {
				continue;
			}

			UserChannelRiskRatingRuleNo ruleNo = UserChannelRiskRatingRuleNo.getRuleByRuleNo(rule.getRuleNo());

			switch (ruleNo) {
			case RISK_PEP:
			case RISK_SANCTIONS_HITS:
			case RISK_BEHAVIOUR:
				UserChannelRiskRatingRuleParam param = UserChannelRiskRatingRuleParam.getParam(ruleNo, ruleValue);
				rule.setRuleLevel(param.getLevel().name());
				rule.setRuleScore(param.getScore());
				break;
			case RISK_MANUAL:
				UserChannelRiskRatingRuleLevel level = UserChannelRiskRatingRuleLevel.detailScore(ruleNo, new BigDecimal(ruleValue));
				rule.setRuleLevel(level.name());
				rule.setRuleScore(ruleValue);
				break;
			default:
				break;
			}

			rule.setRuleValue(ruleValue);
			rule.setAuditor(auditor);
			rule.setAuditTime(DateUtils.getNewUTCDate());
			rule.setUpdateTime(DateUtils.getNewUTCDate());
			userChannelRiskRatingRuleMapper.updateByPrimaryKeySelective(rule);
		}
		log.info("update risk rating rule userId:{} riskRatingId:{} auditor:{}", userId, riskRatingId, auditor);
		// 重新触发风控的评分逻辑重跑
		redoRiskRatingRule(userId, riskRatingId);
	}

	/**
	 * 获取风险国籍列表
	 *
	 * @return
	 */
	public SearchResult<UserChannelRiskCountryVo> getChannelRiskCountryList(UserChannelRiskCountryQuery query) {
		long count = userChannelRiskCountryMapper.queryCount(query);
		if (count <= 0) {
			return new SearchResult<>(Collections.emptyList(), count);
		}
		List<UserChannelRiskCountry> countries = userChannelRiskCountryMapper.query(query);
		List<UserChannelRiskCountryVo> countryVos = countries.stream().map(item -> {
			UserChannelRiskCountryVo vo = new UserChannelRiskCountryVo();
			BeanUtils.copyProperties(item, vo);
			return vo;
		}).collect(Collectors.toList());
		return new SearchResult<>(countryVos, count);
	}

	/**
	 * 修改报错
	 *
	 * @param riskCountryVo
	 */
	public int saveChannelRiskCountry(UserChannelRiskCountryVo riskCountryVo) {
		if (riskCountryVo == null
				|| StringUtils.isAnyBlank(riskCountryVo.getChannelCode(), riskCountryVo.getCountryCode())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		String countryCode = riskCountryVo.getCountryCode();
		String channelCode = riskCountryVo.getChannelCode();
		log.info("update channel risk country by: {}", riskCountryVo);
		UserChannelRiskCountry country = userChannelRiskCountryMapper.selectByPrimaryKey(countryCode, channelCode);
		int count = 0;
		if (country != null) {
			// 如果存在，进行修改
			UserChannelRiskCountry update = new UserChannelRiskCountry();
			BeanUtils.copyProperties(riskCountryVo, update);
			update.setUpdateTime(DateUtils.getNewUTCDate());
			count = userChannelRiskCountryMapper.updateByPrimaryKeySelective(update);
		} else {
			// 创建
			UserChannelRiskCountry insert = new UserChannelRiskCountry();
			BeanUtils.copyProperties(riskCountryVo, insert);
			insert.setCreateTime(DateUtils.getNewUTCDate());
			insert.setUpdateTime(DateUtils.getNewUTCDate());
			count = userChannelRiskCountryMapper.insert(insert);
		}
		return count;
	}

	/**
	 * 删除风险国籍
	 *
	 * @param countryCode
	 * @param channelCode
	 * @return
	 */
	public int deleteChannelRiskCountry(String countryCode, String channelCode) {
		log.info("delete channel risk country by {} {}", countryCode, channelCode);
		UserChannelRiskCountry country = userChannelRiskCountryMapper.selectByPrimaryKey(countryCode, channelCode);
		if (country == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		return userChannelRiskCountryMapper.deleteByPrimaryKey(countryCode, channelCode);
	}

	/**
	 * 获取tier等级对应的额度信息
	 *
	 * @param channelCode
	 * @return
	 */
	public Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> getRiskTierLevelLimit(String channelCode) {
		DecisionCommonRequest request = new DecisionCommonRequest();
		request.setEventCode("recharge_limit");
		request.setContext(new HashMap<>());
		request.getContext().put("channel", channelCode.toLowerCase());
		request.getContext().put("tier", "allTier");
		log.info("获取风控riskRating限额请求 channelCode:{},request:{}", channelCode, request);
		UserRiskRatingTierLevelVo defaultLevel = defaultTierLimit();
		APIResponse<DecisionCommonResponse> riskResp = commonRiskApi.commonRule(APIRequest.instance(request));
		log.info("获取风控riskRating限额返回 channelCode:{},resp:{}", channelCode, riskResp);
		Map<String, Object> extendMap = riskResp == null || riskResp.getData() == null ? new HashMap<>()
				: riskResp.getData().getExtend();
		Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> result = new HashMap<>();
		for (UserRiskRatingTierLevel tierLevel : UserRiskRatingTierLevel.values()) {
			UserRiskRatingTierLevelVo tierVo;
			if (extendMap.get(tierLevel.getRiskRespKey()) == null) {
				tierVo = defaultLevel;
			} else {
				tierVo = new UserRiskRatingTierLevelVo();
				UserRiskRatingTierLevelVo tierMap = JSON.parseObject(
						extendMap.get(tierLevel.getRiskRespKey()).toString(),
						UserRiskRatingTierLevelVo.class);
				tierVo.setDailyLimit(tierMap.getDailyLimit() == null ? BigDecimal.ZERO : tierMap.getDailyLimit());
				tierVo.setMonthlyLimit(tierMap.getMonthlyLimit() == null ? BigDecimal.ZERO : tierMap.getMonthlyLimit());
				tierVo.setTotalLimit(tierMap.getTotalLimit() == null ? BigDecimal.ZERO : tierMap.getTotalLimit());
				tierVo.setYearlyLimit(tierMap.getYearlyLimit() == null ? BigDecimal.ZERO : tierMap.getYearlyLimit());
				tierVo.setWithdrawDailyLimit(tierMap.getWithdrawDailyLimit() == null ? BigDecimal.ZERO : tierMap.getWithdrawDailyLimit());
				tierVo.setWithdrawMonthlyLimit(tierMap.getWithdrawMonthlyLimit() == null ? BigDecimal.ZERO : tierMap.getWithdrawMonthlyLimit());
				tierVo.setWithdrawYearlyLimit(tierMap.getWithdrawYearlyLimit() == null ? BigDecimal.ZERO : tierMap.getWithdrawYearlyLimit());
				tierVo.setWithdrawTotalLimit(tierMap.getWithdrawTotalLimit() == null ? BigDecimal.ZERO : tierMap.getWithdrawTotalLimit());
			}
			result.put(tierLevel, tierVo);
		}
		return result;
	}

	@Override
	public RiskRatingLimitResponse riskRatingLimit(Long userId, String channelCode,Boolean autoApply) {
		UserChannelRiskRating riskRating = userChannelRiskRatingMapper.selectByUk(userId, channelCode);
		RiskRatingLimitResponse response = new RiskRatingLimitResponse();

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		if (kycCertificate != null && !KycCertificateStatus.REFUSED.name().equals(kycCertificate.getStatus())) {
			response.setKycType(KycCertificateKycType.getByCode(kycCertificate.getKycType()));
			if(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
				response.setNeedApply(true);
				return response;
			}
		}
		try {
			UserRiskRatingChannelCode channel = UserRiskRatingChannelCode.getByCode(channelCode);
			if (riskRating == null) {
				//自动申报
				if(autoApply != null && autoApply) {
					RiskRatingApplyRequest request = new RiskRatingApplyRequest();
					request.setUserId(userId);
					request.setChannelCode(channel);
					riskRating = userChannelRiskRatingContext.getRatingHandler(channel).applyRiskRating(request);
				}else {
					log.info("用户需要填写riskRating申报. userId:{},channelCode:{}", userId, channelCode);
					response.setNeedApply(true);
					return response;
				}
			}
		}catch(Exception e) {
			log.warn("获取风控riskRating限额异常 userId:{} channelCode:{}",userId,channelCode,e);
			response.setNeedApply(true);
			return response;
		}

		Map<UserRiskRatingTierLevel, UserRiskRatingTierLevelVo> tierMap = tierCache.getIfPresent(riskRating.getChannelCode());
		if (tierMap == null) {
			tierMap = getRiskTierLevelLimit(riskRating.getChannelCode());
			if (tierMap != null) {
				tierCache.put(riskRating.getChannelCode(), tierMap);
			}
		}
		// 去除tier0
		tierMap.remove(UserRiskRatingTierLevel.Tier0);
		response.setTierMap(tierMap);
		response.setStatus(UserRiskRatingStatus.valueOf(riskRating.getStatus()));
		response.setTierLevel(UserRiskRatingTierLevel.valueOf(riskRating.getTierLevel()));
		response.setLimitUnit(riskRating.getLimitUnit());

		if (!UserRiskRatingStatus.ENABLE.name().equals(riskRating.getStatus())
				|| UserRiskRatingTierLevel.NoTier.name().equals(riskRating.getTierLevel())) {
			log.info("riskRating记录为Disable或NoTier. userId:{},channelCode:{}", userId, channelCode);
			response.setDailyLimit(new BigDecimal(0));
			response.setMonthlyLimit(new BigDecimal(0));
			response.setTotalLimit(new BigDecimal(0));
			response.setYearlyLimit(new BigDecimal(0));
			response.setWithdrawDailyLimit(new BigDecimal(0));
			response.setWithdrawMonthlyLimit(new BigDecimal(0));
			response.setWithdrawYearlyLimit(new BigDecimal(0));
			response.setWithdrawTotalLimit(new BigDecimal(0));
			return response;
		}

		UserRiskRatingTierLevel tierLevel = UserRiskRatingTierLevel.valueOf(riskRating.getTierLevel());

		try {

			BigDecimal dailyLimit = riskRating.getDailyLimit();
			BigDecimal monthlyLimit = riskRating.getMonthlyLimit();
			BigDecimal totalLimit = riskRating.getTotalLimit();
			BigDecimal yearlyLimit = riskRating.getYearlyLimit();
			BigDecimal withdrawDailyLimit = riskRating.getWithdrawDailyLimit();
			BigDecimal withdrawMonthlyLimit = riskRating.getWithdrawMonthlyLimit();
			BigDecimal withdrawYearlyLimit = riskRating.getWithdrawYearlyLimit();
			BigDecimal withdrawTotalLimit = riskRating.getWithdrawTotalLimit();

			log.info("获取风控riskRating限额.rating本地记录 userId:{},channelCode:{},dailyLimit:{},monthlyLimit:{}", userId,
					channelCode, dailyLimit, monthlyLimit);

			response.setTierMap(tierMap);
			UserRiskRatingTierLevelVo tierLevelVo = tierMap.get(tierLevel);
			if (dailyLimit == null) {
				dailyLimit = tierLevelVo.getDailyLimit();
			}

			if (monthlyLimit == null) {
				monthlyLimit = tierLevelVo.getMonthlyLimit();
			}

			if (totalLimit == null) {
				totalLimit = tierLevelVo.getTotalLimit();
			}
			if (yearlyLimit == null) {
				yearlyLimit = tierLevelVo.getYearlyLimit();
			}
			if (withdrawDailyLimit == null) {
				withdrawDailyLimit = tierLevelVo.getWithdrawDailyLimit();
			}
			if (withdrawMonthlyLimit == null) {
				withdrawMonthlyLimit = tierLevelVo.getWithdrawMonthlyLimit();
			}
			if (withdrawYearlyLimit == null) {
				withdrawYearlyLimit = tierLevelVo.getWithdrawYearlyLimit();
			}
			if (withdrawTotalLimit == null) {
				withdrawTotalLimit = tierLevelVo.getWithdrawTotalLimit();
			}
			response.setDailyLimit(dailyLimit == null ? new BigDecimal(0) : dailyLimit);
			response.setMonthlyLimit(monthlyLimit == null ? new BigDecimal(0) : monthlyLimit);
			response.setTotalLimit(totalLimit == null ? new BigDecimal(0) : totalLimit);
			response.setYearlyLimit(yearlyLimit == null ? new BigDecimal(0) : yearlyLimit);
			response.setWithdrawDailyLimit(withdrawDailyLimit == null ? new BigDecimal(0) : withdrawDailyLimit);
			response.setWithdrawMonthlyLimit(withdrawMonthlyLimit == null ? new BigDecimal(0) : withdrawMonthlyLimit);
			response.setWithdrawYearlyLimit(withdrawYearlyLimit == null ? new BigDecimal(0) : withdrawYearlyLimit);
			response.setWithdrawTotalLimit(withdrawTotalLimit == null ? new BigDecimal(0) : withdrawTotalLimit);
			return response;
		} catch (Exception e) {
			log.warn("风控获取riskrating额度失败. userId:{},channelCode:{}", userId, channelCode, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	@Override
	public RiskRatingApplyResponse riskRatingApply(RiskRatingApplyRequest request) {
		RiskRatingApplyResponse response = new RiskRatingApplyResponse();
		try {
			userChannelRiskRatingContext.getRatingHandler(request.getChannelCode()).applyRiskRating(request);
			response.setSuccess(true);
			return response;
		} catch (BusinessException be) {
			response.setSuccess(false);
			response.setTips(
					MessageMapHelper.getMessage(be.getBizCode(), WebUtils.getAPIRequestHeader().getLanguage()));
			return response;
		}
	}

	@Override
	public void syncCardCountry(Long userId, String channelCode, String cardCountry) {
		UserChannelRiskRating rating = userChannelRiskRatingMapper.selectByUk(userId, channelCode);
		if (rating == null) {
			return;
		}

		if (userId == null || StringUtils.isAnyBlank(channelCode, cardCountry)) {
			return;
		}
		cardCountry = cardCountry.toUpperCase();
		UserChannelRiskRating record = new UserChannelRiskRating();
		record.setId(rating.getId());
		record.setCardCountry(StringUtils.isBlank(rating.getCardCountry()) ? cardCountry
				: rating.getCardCountry() + "," + cardCountry);
		userChannelRiskRatingMapper.updateByPrimaryKeySelective(record);
	}

}
