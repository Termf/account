package com.binance.account.service.question.checker;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.ProtectedStatus;
import com.binance.account.common.enums.UserRiskStatus;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserRiskFeature;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.data.mapper.user.UserRiskFeatureMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.device.IUserDevice;
import com.binance.account.service.question.export.IQuestion;
import com.binance.account.service.reset2fa.impl.Reset2FaService;
import com.binance.account.service.security.impl.UserSecurityResetBusiness;
import com.binance.account.service.security.impl.UserSecurityResetHelper;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.DeviceCacheUtils;
import com.binance.account.vo.device.response.FindMostSimilarUserDeviceResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.WebUtils;
import com.binance.risk.api.RiskUserQuestionApi;
import com.binance.risk.vo.cases.request.RiskUserQuestionFeatureRequest;
import com.binance.risk.vo.cases.response.RiskUserQuestionFeatureResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 问题模块校验工具
 *
 */
@Slf4j
@Service
public class QuestionModuleChecker {

	private final int SESSION_ID = UUID.randomUUID().toString().hashCode();
	private final String _2FA_RESET_SEND_EMAIL = "2FA重置时回答问题失败发邮件";
	private final String FAIL_ANSWER2FA = AccountConstants.RESET_FAIL_ANSWER_2FA_EMAIL_TEMPLATE;

	@Value("${reset.device.cache.timeoutInseconds:2592000}")
	private Long deviceTimeOutInseconds;// 设备pk缓存时间，默认30天，人工审核可能很慢
	
	@Resource
	private UserSecurityMapper userSecurityMapper;
	@Resource
	private UserSecurityResetMapper userSecurityResetMapper;
	@Resource
	private ApolloCommonConfig commonConfig;
	@Resource
	private IUserDevice iUserDevice;
	@Resource
	protected Reset2FaService reset2FaService;
	@Resource
	private UserSecurityResetHelper userSecurityResetHelper;
	@Resource
	protected UserCommonBusiness userCommonBusiness;
	@Resource
	private IQuestion iQuestion;
	@Resource
	private UserRiskFeatureMapper userRiskFeatureMapper;
	@Resource
	private RiskUserQuestionApi riskUserQuestionApi;
	@Resource
    protected UserMapper userMapper;
	
	private FindMostSimilarUserDeviceResponse getDeviceInfo(final Long userId, final Map<String, String> device) {
		String terminal = WebUtils.getAPIRequestHeader().getTerminal().getCode();
		return iUserDevice.findMostSimilarDevice(userId, device, terminal);
	}
	
	/**
	 * 缓存设备pk，返回收否新设备,在流程结束的时候清理此处缓存参见{@link UserSecurityResetBusiness#resetAuditHandler}
	 * 
	 * @param userId
	 * @param resetId
	 * @param device
	 * @return
	 */
	public boolean cacheDevicePK(final Long userId, final String resetId, final Map<String, String> device) {
		FindMostSimilarUserDeviceResponse response = getDeviceInfo(userId, device);
		if (response != null && response.isSame()) {
			String pk = response.getMatched().getId().toString();
			log.info("重置流程->缓存devicePK,userId:{},resetId:{},devicePk:{},deviceTimeOutInseconds:{}", 
					userId,
					resetId, 
					pk,
					deviceTimeOutInseconds);
			DeviceCacheUtils.setDevicePK(resetId, pk, deviceTimeOutInseconds);
			return false;
		}
		return true;
	}
	
	/**
	 * 查询指定reset的DevicePK
	 * 
	 * @param resetId
	 * @return
	 */
	public String getDevicePK(final String resetId) {
		return DeviceCacheUtils.getDevicePK(resetId);
	}
	
	/**
	 * 是否新设备
	 * 
	 * @param userId
	 * @param device
	 * @return
	 */
	public boolean isNewDevice(final Long userId, final Map<String, String> device) {
		FindMostSimilarUserDeviceResponse response = getDeviceInfo(userId, device);
		boolean b = response == null || response.isSame() == false;
		log.info("重置流程->userId:{},isNewDevice:{}", userId, b);
		return b;
	}

	/**
	 * 返回用户可以剩余答题次数
	 * 
	 * @return 返回用户剩余答题次数
	 */
	public int remainingTimes(final Long userId, final String resetId) {
		int confTimes = commonConfig.getProtectedTimes();
		if (StringUtils.isBlank(resetId)) {
			return confTimes;
		}
		Integer userCurrentResetAnswerTimes = this.userCurrentAnswerTimes(userId, resetId);
		int resetFails = userCurrentResetAnswerTimes == null ? 0 : userCurrentResetAnswerTimes;
		int count = 0;
		if (resetFails < confTimes) {
			count = confTimes - resetFails;
		}
		log.info("重置流程->userId:{},resetId:{},剩余答题次数:{}.", userId, resetId, count);
		return count > 0 ? count : 0;
	}

	/**
	 * 用户答题失败次数累计加一,达到最大次数时进入保护模式
	 * 
	 * @param userId
	 * @return 当前答题失败次数
	 */
	@Transactional(value = DefaultDB.TRANSACTION, rollbackFor = Exception.class)
	public void doWhenFails(final Long userId, final String resetId, final Integer failTimes) {
		// UserSecurity us = userSecurityExistValidate(userId);
		if (failTimes < commonConfig.getProtectedTimes()) {
			log.info("重置流程->失败计数,userId:{},failTimes:{}", userId, failTimes);
			return;
		}
		// 达到最大答题次数，不直接进入保护模式，由定时job拉风控的决策数据，后置打tag
		saveUserFlowForRisk(userId, resetId);
		log.info("重置流程->答题失败达到阈值,保存数据待风控后置处理 ,userId:{},resetId:{},failTimes:{}", userId, resetId, failTimes);
	}

	/**
	 * 用户流程保存到风控决策表，待定时job后置处理
	 * 
	 * @param userId
	 * @param resetId
	 */
	public void saveUserFlowForRisk(final Long userId, final String resetId) {
		UserRiskFeature feature = new UserRiskFeature();
		feature.setUserId(userId);
		feature.setFlowId(resetId);
		feature.setFeatures("");
		fillIpIfReset(feature);
		feature.setUpdateTime(DateUtils.getNewUTCDate());
		feature.setCreateTime(DateUtils.getNewUTCDate());
		feature.setStatus(UserRiskStatus.UNDO);
		feature.setRiskResult(Boolean.FALSE);
		userRiskFeatureMapper.insert(feature);
	}

	// reset2fa的有IP的记录
	private void fillIpIfReset(UserRiskFeature feature) {
		try {
			UserSecurityReset resetFlow = userResetExistValidate(feature.getFlowId());
			feature.setIp(resetFlow.getApplyIp());
		} catch(Exception e) {
			feature.setIp("127.0.0.1");//给个默认值
		}
	}

	/**
	 * 由风控后置处理用户，判断是否进入保护模式
	 * 
	 * @param clearMinutes 清理之前的处理中的数据，因为超时未处理
	 * @param undoMinutes 查询之前的数据，延迟处理，不然风控结果不准
	 */
	public void postProcessUserByRiskFeatrue(int clearMinutes, int undoMinutes) {
		clearMinutes = Math.max(clearMinutes, 5);
		undoMinutes = Math.max(undoMinutes, 2);
		log.info("调用风控->后置处理保护模式，clearMinutes:{},undoMinutes:{}", clearMinutes, undoMinutes);
		// 清理超时处理中的数据
		clearTimeOutInDoingBefore(clearMinutes);
		// 抢占未处理的数据,有延迟也没问题
		int delayMinute = undoMinutes;
		grabUndoBefore(delayMinute);
        // 查询的包含上一次抢占的数据
		int count = 3;
		List<UserRiskFeature> lst = getGrabedBySessionId(delayMinute);
		// 主备延迟，可能没有查询到，自旋3次
		while (CollectionUtils.isEmpty(lst) && count-- > 0) {
			sleepXSeconds(3);//
			lst = getGrabedBySessionId(delayMinute);
		}
		if (CollectionUtils.isEmpty(lst)) {
			log.info("调用风控->后置处理保护模式,可能主从同步延迟下次job处理,sessionId:{}", SESSION_ID);
			return;
		}
		/**
		 * 调风控接口，此处不用缓存 是因为虽然risk处理的纬度是userid 但是都是根据用户最新的轨迹计算命中策略
		 */
		lst.forEach(this::saveRiskFeaturesWhenInDoning);
	}

	// 抢占延迟了delayMinute分钟的待处理的数据
	private boolean grabUndoBefore(int delayMinute) {
		int grabRows = userRiskFeatureMapper.updateStatusFromTo(delayMinute, 
				UserRiskStatus.UNDO.ordinal(), 
				UserRiskStatus.DOING.ordinal(),
				SESSION_ID);
		log.info("调用风控->后置处理保护模式,抢占待处理数据:{}行,sessionId:{}", grabRows, SESSION_ID);
		return grabRows > 0;
	}

	// 释放超时clearMinutes分钟的数据 处理中的数据
	private void clearTimeOutInDoingBefore(int clearMinutes) {
		int rows = userRiskFeatureMapper.updateStatusFromTo(clearMinutes, 
				UserRiskStatus.DOING.ordinal(), 
				UserRiskStatus.UNDO.ordinal(),0);
		log.info("调用风控->后置处理保护模式,释放超时数据:{}行", rows);
	}

	// 当前数据处于处理中状态时，更新数据，避免重复更新
	private void saveRiskFeaturesWhenInDoning(UserRiskFeature c) {
		RiskUserQuestionFeatureResponse res = selectRiskFeatures(c);
		if(res==null) {
			log.info("调用风控->后置处理保护模式,风控接口暂时不可用下次在处理，userId:{}", c.getUserId());
			return;
		}
		String jsonString = JSON.toJSONString(res.getFeatureResults());
		c.setRiskResult(res.getTotalResult());
		c.setFeatures(jsonString);
		c.setStatus(UserRiskStatus.DONE);
		c.setUpdateTime(DateUtils.getNewUTCDate());
		userRiskFeatureMapper.updateSelectiveInDoing(c);
		// 用户进入保护模式
		if (res.getTotalResult()) {
			UserSecurity us = userSecurityExistValidate(c.getUserId());
			ProtectedStatus status = us.getProtectedStatus();
			if (status == null || !status.isInProtectedMode()) {
				make2ProtectedMode(c.getUserId());
				log.info("调用风控->命中风控决策,用户进入保护模式,userId:{},featrues:{}", c.getUserId(),jsonString);
			}else {
				log.info("调用风控->命中风控决策,userId:{},ProtectedStatus:{}", c.getUserId(), status);
			}
		}
	}

	// 查询风控命中策略
	private RiskUserQuestionFeatureResponse selectRiskFeatures(UserRiskFeature c) {
		APIResponse<RiskUserQuestionFeatureResponse> response = null;
		try {
			RiskUserQuestionFeatureRequest request = new RiskUserQuestionFeatureRequest();
			request.setUserId(c.getUserId() + "");
			request.setIpAddress3D(c.getIp());
			APIRequest<RiskUserQuestionFeatureRequest> instance = APIRequest.instance(request);
			log.info("调用风控->查询风控接口,request:{}", instance);
			response = riskUserQuestionApi.getUserFeatures(instance);
			if (response.getStatus() != Status.OK || response.getData() == null) {
				log.warn("调用风控->查询风控接口,request:{}, response:{}", instance, response);
				return null;
			}
			log.info("调用风控->查询风控接口,request:{}, response:{}", instance, response);
			return response.getData();
		} catch (Exception e) {
			log.error("调用风控->查询风控接口异常。", e);
		}
		return null;
	}

	private void sleepXSeconds(int x) {
		try {
			TimeUnit.SECONDS.sleep(x <= 0 ? 3 : x);
		} catch (InterruptedException e) {
		}
	}

	// 查询已经抢占的数据，状态为处理中，由于主备延迟不一定是刚刚抢占的
	private List<UserRiskFeature> getGrabedBySessionId(int undoMinutes) {
		return userRiskFeatureMapper.getBeforeMinutes(undoMinutes, UserRiskStatus.DOING.ordinal(), SESSION_ID);
	}

	/**
	 * 解除用户的保护模式，去掉答题限制
	 * 
	 * @param userId
	 * @return
	 */
	public boolean clearProtectedMode(final Long userId) {
		log.info("解除保护模式,userId:{}.", userId);
		return updateProtectedStatus(userId, ProtectedStatus.NORMAL_MODE);
	}

	/**
	 * 用户进入保护模式，禁止答题
	 * 
	 * @param userId
	 * @return
	 */
	public boolean make2ProtectedMode(final Long userId) {
		log.info("进入保护模式,userId:{}.", userId);
		return updateProtectedStatus(userId, ProtectedStatus.PROTECTED_MODE);
	}

	/**
	 * 用户进入禁止模式
	 * 
	 * @param userId
	 * @return
	 */
	public boolean make2ForbitMode(final Long userId) {
		log.info("进入禁用模式,userId:{}.", userId);
		return updateProtectedStatus(userId, ProtectedStatus.FORBID_MODE);
	}

	/**
	 * 直接更新用户保护状态和失败次数
	 * 
	 * @param userId
	 * @param status
	 * @return
	 */
	public boolean updateProtectedStatus(final Long userId, ProtectedStatus status) {
		return userSecurityMapper.updateProtectedMode(userId, status == null ? null : status.ordinal()) > 0;
	}

	/**
	 * 用户安全数据校验，不存在，抛出异常
	 * 
	 * @param userId
	 * @return 用户安全数据
	 */
	public UserSecurity userSecurityExistValidate(Long userId) {
		UserSecurity us = userSecurityMapper.selectByPrimaryKey(userId);
		if (us == null) {
			log.error("重置流程->进入保护模式,查询不到用户设备失败次数.userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_VALID);
		}
		return us;
	}

	/**
	 * 用户重置流程记录校验，不存在或者不再答题状态抛出异常
	 * 
	 * @param resetId
	 * @return 重置记录
	 */
	public UserSecurityReset userResetExistValidate(final String resetId) {
		UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(resetId);
		if (reset == null) {
			log.warn("重置答题->获取重置流程信息失败. flowiId:{}", resetId);
			throw new BusinessException(GeneralCode.SYS_VALID);
		}
		return reset;
	}

	/**
	 * 用户是否处于答题过程中校验，不是抛出异常
	 * 
	 * @param resetId
	 * @return 重置记录
	 */
	public UserSecurityReset userAnsweringValidate(final String resetId) {
		UserSecurityReset reset = userResetExistValidate(resetId);
		if (UserSecurityResetStatus.unverified != reset.getStatus()) {
			log.error("重置答题->当前状态已经不在答题环节. resetId:{}", resetId);
			throw new BusinessException(AccountErrorCode.RESET_FLOW_CAN_NOT_DO_QUESTION);
		}
		return reset;
	}

	public User userExistValidate(final Long userId) {
		User user = userSecurityResetHelper.getUserByUserId(userId);
		if (user == null) {
			log.error("重置答题->当前用户不存. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_VALID);
		}
		return user;
	}

	/**
	 * 发失败邮件
	 * 
	 * @param userId
	 */
	public void sendEmailWhenFail(Long userId) {
		User user = userExistValidate(userId);
		userCommonBusiness.sendDisableTokenEmail(FAIL_ANSWER2FA, user, null, _2FA_RESET_SEND_EMAIL, null);
		log.info("重置答题->答题失败,发邮件.userId:{}", userId);
	}

	/**
	 * 答题完毕,通过
	 * 
	 * @param userId
	 * @param resetId
	 */
	public void sendEmailWhenSuccess(final Long userId, final String resetId) {
		User user = userExistValidate(userId);
		UserSecurityReset reset = userResetExistValidate(resetId);
		log.info("重置答题->答题通过,发邮件.userId:{},resetId:{}", userId, resetId);
		reset2FaService.toUploadStatus(user, reset);
	}

	/**
	 * 指定的用户再指定的流程中申请宁问题的次数
	 * 
	 * @param userId
	 * @param resetId
	 * @return
	 */
	public Integer userCurrentAnswerTimes(Long userId, String resetId) {
		return iQuestion.getFlowCurrentAnswerTimes(userId, resetId);
	}

	/**
	 * 控制流，取消本次重置流程
	 * 
	 * @param reset
	 */
	public void cancelReset(final UserSecurityReset reset) {
		UserSecurityReset updateVo = new UserSecurityReset();
		updateVo.setUserId(reset.getUserId());
		updateVo.setId(reset.getId());
		updateVo.setStatus(UserSecurityResetStatus.cancelled);
		updateVo.setUpdateTime(DateUtils.getNewUTCDate());
		updateVo.setFailReason(reset.getFailReason());
		userSecurityResetMapper.updateByPrimaryKeySelective(updateVo);
	}
	
	/**
	 * 查询用户的风控后置处理记录
	 * 
	 * @param userId
	 * @param flowId
	 * @return
	 */
	public List<UserRiskFeature> geRiskPostLog(Long userId,String flowId) {
		return userRiskFeatureMapper.getByUserId(userId, flowId);
	}
	
	public User getUserByEmail(String email) {
		User user = userMapper.queryByEmail(email);
		if (user == null) {
			log.error("问答模块->查询用户不存在. email:{}", email);
			throw new BusinessException(GeneralCode.SYS_VALID);
		}
		return user;
	}
}
