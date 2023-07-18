package com.binance.account.service.certificate.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.JumioType;
import com.binance.account.common.exception.InitJumioException;
import com.binance.account.common.query.JumioBizStatusQuery;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.vo.user.response.JumioSdkInitResponse;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioScanSource;
import com.binance.inspector.common.query.JumioCountRequest;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.request.ChangeStatusRequest;
import com.binance.inspector.vo.jumio.request.InitJumioRequest;
import com.binance.inspector.vo.jumio.request.JumioBaseRequest;
import com.binance.inspector.vo.jumio.request.JumioBizIdRequest;
import com.binance.inspector.vo.jumio.request.JumioBizStatusSyncRequest;
import com.binance.inspector.vo.jumio.response.InitJumioResponse;
import com.binance.inspector.vo.jumio.response.InitSdkJumioResponse;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.WebUtils;
import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * JUMIO 的 初始化和JUMIO逻辑公共服务
 * 
 * @author liliang1
 */
@Log4j2
@Service
public class JumioBusiness {

	@Resource
	JumioMapper jumioMapper;
	@Resource
	private JumioApi jumioApi;
	@Resource
	private UserSecurityResetMapper userSecurityResetMapper;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;

	/**
	 * jumio审核通过，直接pass开关 默认false
	 */
	public boolean isDirectPassSwitchOn() {
		Boolean passDirect = apolloCommonConfig.getJumioPassDirectSwitch();
		return passDirect != null && passDirect;
	}

	/**
	 *
	 * @param userId
	 * @param jumioHandlerType
	 * @param bizId
	 * @param isLockOne        是否锁定了一个用户只能又一笔认证
	 * @return
	 */
	public Jumio initWebJumio(Long userId, JumioHandlerType jumioHandlerType, String bizId, boolean isLockOne) {
		InitJumioResponse jumioResponse = initWebJumioWithoutSave(userId, jumioHandlerType, bizId, isLockOne);
		Jumio jumio = new Jumio();
		jumio.setUserId(userId);
		jumio.setType(JumioType.getByName(jumioHandlerType.getCode()));
		jumio.setScanReference(jumioResponse.getTransactionReference());
		jumio.setAuthToken(jumioResponse.getRedirectUrl());
		jumio.setCreateTime(DateUtils.getNewUTCDate());
		jumio.setUpdateTime(DateUtils.getNewUTCDate());
		int row = jumioMapper.insert(jumio);
		if (row > 0) {
			return jumio;
		}
		throw new BusinessException(GeneralCode.SYS_ERROR, "save jumio info fail");
	}

	/**
	 *
	 * @param userId
	 * @param handlerType
	 * @param bizId
	 * @param isLockOne   是否锁定了一个用户只能又一笔认证
	 * @return
	 */
	public JumioSdkInitResponse initSdkJumio(Long userId, JumioHandlerType handlerType, String bizId,
			boolean isLockOne) {
		InitSdkJumioResponse response = initSdkJumioWithoutSave(userId, handlerType, bizId, isLockOne);
		Jumio jumio = new Jumio();
		jumio.setUserId(userId);
		jumio.setType(JumioType.getByName(handlerType.getCode()));
		jumio.setSource(JumioScanSource.SDK.name());
		jumio.setMerchantReference(response.getMerchantReference());
		jumio.setCreateTime(DateUtils.getNewUTCDate());
		jumio.setUpdateTime(DateUtils.getNewUTCDate());
		int row = jumioMapper.insert(jumio);
		if (row > 0) {
			JumioSdkInitResponse result = new JumioSdkInitResponse();
			BeanUtils.copyProperties(response, result);
			result.setJumioId(jumio.getId());
			return result;
		}
		throw new BusinessException(GeneralCode.SYS_ERROR, "save jumio info fail");
	}

	/**
	 * 初始化JUMIO但不保存JUMIO记录信息
	 * 
	 * @param userId
	 * @param handlerType
	 * @param bizId
	 * @param isLockOne   是否锁定了一个用户只能又一笔认证
	 * @return
	 * @throws InitJumioException
	 */
	public InitJumioResponse initWebJumioWithoutSave(Long userId, JumioHandlerType handlerType, String bizId,
			boolean isLockOne) {
		APIRequest<InitJumioRequest> request = generateInitJumioRequest(userId, handlerType, bizId, isLockOne);
		log.info("开始初始化 WEB JUMIO信息, userId:{} bizId:{} handlerType:{} baseUrl:{}", userId, bizId, handlerType,request.getBody().getBaseUrl());
		APIResponse<InitJumioResponse> response = jumioApi
				.initJumio(generateInitJumioRequest(userId, handlerType, bizId, isLockOne));
		String logStr = JSON.toJSONString(response);
		logStr = logStr == null ? null : LogMaskUtils.maskJsonString(logStr, "redirectUrl");
		log.info("初始化 WEB JUMIO 信息结果: userId:{} bizId:{} handlerType:{} response:{}", userId, bizId, handlerType,
				logStr);
		if (response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
			log.warn("初始化 WEB JUMIO 信息失败. userId:{} bizId:{}", userId, bizId);
			throw new BusinessException(response.getCode(), response.getErrorData().toString());
		}
		// 如果初始化成功了
		return response.getData();
	}

	/**
	 * 初始化JUMIO SDK
	 * 
	 * @param userId
	 * @param handlerType
	 * @param bizId
	 * @param isLockOne   是否锁定了一个用户只能又一笔认证
	 * @return
	 */
	public InitSdkJumioResponse initSdkJumioWithoutSave(Long userId, JumioHandlerType handlerType, String bizId,
			boolean isLockOne) {
		log.info("开始初始化 SDK JUMIO 信息, userId:{} bizId:{} handlerType:{}", userId, bizId, handlerType);
		APIResponse<InitSdkJumioResponse> response = jumioApi
				.initJumioSdk(generateInitJumioRequest(userId, handlerType, bizId, isLockOne));
		if (response == null || response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
			log.info("初始化 SDK JUMIO 信息失败. userId:{} bizId:{} handlerType:{} response:{}", userId, bizId, handlerType,
					JSON.toJSONString(response));
			throw new BusinessException(response.getCode(), response.getErrorData().toString());
		}
		return response.getData();
	}

	private APIRequest<InitJumioRequest> generateInitJumioRequest(Long userId, JumioHandlerType handlerType,
			String bizId, boolean isLockOne) {
		InitJumioRequest request = new InitJumioRequest();
		request.setUserId(userId);
		request.setTypeCode(handlerType.getCode());
		request.setBizId(bizId);
		request.setIsLockOne(isLockOne);
		APIRequestHeader requestHeader = WebUtils.getAPIRequestHeader();
		if (requestHeader == null) {
			requestHeader = new APIRequestHeader();
			requestHeader.setTerminal(TerminalEnum.WEB);
			requestHeader.setLanguage(LanguageEnum.EN_US);
		}
		String baseUrl = WebUtils.getHeader(Constant.BASE_URL);
		request.setBaseUrl(baseUrl);
		return APIRequest.instance(requestHeader, request);
	}

	/**
	 * 用于把业务状态同步到JUMIO的数据中
	 * 
	 * @param userId
	 * @param scanReference
	 * @param bizStatus
	 * @return
	 */
	public boolean syncJumioBizStatus(Long userId, String scanReference, JumioBizStatus bizStatus) {
		try {
			if (userId == null || StringUtils.isBlank(scanReference) || bizStatus == null) {
				return false;
			}
			JumioBizStatusSyncRequest request = new JumioBizStatusSyncRequest();
			request.setUserId(userId);
			request.setScanReference(scanReference);
			request.setBizStatus(bizStatus);
			APIResponse response = jumioApi.syncBizStatus(APIRequest.instance(request));
			log.info("同步JUMIO 业务状态结果: userId:{} scanRef:{} bizStatus:{} result:{}", userId, scanReference, bizStatus,
					JSON.toJSONString(response));
			return response != null && response.getStatus() == APIResponse.Status.OK;
		} catch (Exception e) {
			log.error("同步JUMIO BIZ STATUS 失败. userId:{} scanRef:{} bizStatus:{}, e", userId, scanReference, bizStatus,
					e);
			return false;
		}
	}

	/**
	 * 查询当前业务的最终业务状态，当返回null时代表正在审核处理中
	 * 
	 * @param query
	 * @return
	 */
	public JumioBizStatus queryJumioBizStatus(JumioBizStatusQuery query) {
		if (query == null || query.getUserId() == null
				|| StringUtils.isAnyBlank(query.getBizId(), query.getHandlerTypeCode())) {
			log.info("查询参数错误");
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		JumioHandlerType handlerType = JumioHandlerType.getByCode(query.getHandlerTypeCode());
		if (handlerType == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		Long userId = query.getUserId();
		String bizId = query.getBizId();
		JumioBizStatus result;
		switch (handlerType) {
		case RESET_GOOGLE:
		case RESET_MOBILE:
		case RESET_ENABLE:
			result = getResetBizStatus(userId, bizId, handlerType);
			break;
		case USER_KYC:
			result = getUserKycBizStatus(userId, bizId);
			break;
		case COMPANY_KYC:
			result = getCompanyKycBizStatus(userId, bizId);
			break;
		default:
			log.info("参数类型错误. userId:{} bizId:{} handlerType:{}", userId, bizId, handlerType);
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		log.info("当前业务状态：userId:{} bizId:{} bizStatus:{}", userId, bizId, result);
		return result;
	}

	private JumioBizStatus getResetBizStatus(Long userId, String bizId, JumioHandlerType handlerType) {
		UserSecurityReset reset = userSecurityResetMapper.selectByPrimaryKey(bizId);
		if (reset == null || !userId.equals(reset.getUserId())
				|| !StringUtils.equalsIgnoreCase(handlerType.getCode(), reset.getType().name())) {
			log.info("业务BIZ_ID: 查询到错误的业务 userId:{} bizId:{}", userId, bizId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		if (reset.getStatus() == null) {
			return null;
		}
		switch (reset.getStatus()) {
		case passed:
			return JumioBizStatus.PASSED;
		case refused:
			return JumioBizStatus.REFUSED;
		case cancelled:
			return JumioBizStatus.EXPIRED;
		default:
			log.info("重置流程业务状态还在审核中: userId:{} bizId:{}", userId, bizId);
			return null;
		}
	}

	private JumioBizStatus getUserKycBizStatus(Long userId, String bizId) {
		UserKyc userKyc = userKycMapper.getById(userId, Long.valueOf(bizId));
		if (userKyc == null || userKyc.getStatus() == null) {
			log.info("业务数据查询失败. userId:{} bizId:{}", userId, bizId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		switch (userKyc.getStatus()) {
		case passed:
			return JumioBizStatus.PASSED;
		case refused:
			return JumioBizStatus.REFUSED;
		case expired:
			return JumioBizStatus.EXPIRED;
		case delete:
			return JumioBizStatus.DELETE;
		default:
			log.info("个人KYC业务状态还在审核中. userId:{} bizId:{}", userId, bizId);
			return null;
		}
	}

	private JumioBizStatus getCompanyKycBizStatus(Long userId, String bizId) {
		CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId,
				Long.valueOf(bizId));
		if (companyCertificate == null || companyCertificate.getStatus() == null) {
			log.info("业务数据查询失败. userId:{} bizId:{}", userId, bizId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		switch (companyCertificate.getStatus()) {
		case passed:
			return JumioBizStatus.PASSED;
		case refused:
			return JumioBizStatus.REFUSED;
		case expired:
			return JumioBizStatus.EXPIRED;
		case delete:
			return JumioBizStatus.DELETE;
		default:
			log.info("企业KYC业务状态还在审核中. userId:{} bizId:{}", userId, bizId);
			return null;
		}
	}

	/**
	 * 获取JUMIO信息
	 * 
	 * @param userId
	 * @param scanRef
	 * @param typeCode
	 * @return
	 */
	public JumioInfoVo getByUserAndScanRef(Long userId, String scanRef, String typeCode) {
		try {
			JumioBaseRequest baseRequest = new JumioBaseRequest(userId, scanRef, typeCode, null);
			APIResponse<JumioInfoVo> response = jumioApi.getByUserAndScanReference(APIRequest.instance(baseRequest));
			if (response.getData() != null) {
				return response.getData();
			} else {
				return null;
			}
		} catch (Exception e) {
			log.warn("获取JUMIO信息失败异常. userId:{} scanRef:{}", userId, scanRef);
			return null;
		}
	}

	/**
	 * 获取某一类型的JUMIO 24小时内的请求次数
	 * 
	 * @param userId
	 * @param handlerType
	 * @return
	 */
	public long getDailyJumioTimes(Long userId, JumioHandlerType handlerType) {
		Date endTime = DateUtils.getNewUTCDate();
		Date startTime = DateUtils.add(endTime, Calendar.DAY_OF_MONTH, -1);
		JumioCountRequest countRequest = new JumioCountRequest();
		countRequest.setUserId(userId);
		countRequest.setHandlerType(handlerType);
		countRequest.setStartTime(startTime);
		countRequest.setEndTime(endTime);
		return getJumioCount(countRequest);
	}

	/**
	 * 获取JUMIO 的操作次数
	 * 
	 * @param countRequest
	 * @return
	 */
	public long getJumioCount(JumioCountRequest countRequest) {
		try {
			APIResponse<Long> response = jumioApi.getJumioCount(APIRequest.instance(countRequest));
			if (response == null || response.getStatus() != APIResponse.Status.OK) {
				log.warn("获取JUMIO的操作次数失败, userId:{}", countRequest.getUserId());
				return 0;
			}
			return response.getData();
		} catch (Exception e) {
			log.error("获取JUMIO的操作次数异常, userId:{}", countRequest.getUserId(), e);
			return 0L;
		}
	}

	/**
	 * 根据业务编号信息获取该笔业务最后一次的JUMIO信息
	 * 
	 * @param userId
	 * @param bizId
	 * @param typeCode
	 * @return
	 */
	public JumioInfoVo getLastByUserAndBizId(Long userId, String bizId, String typeCode) {
		try {
			if (userId == null || StringUtils.isBlank(bizId)) {
				log.warn("根据业务编号获取jumio信息时业务编号不能为空. userId: {} bizId:{}", userId, bizId);
				return null;
			}
			JumioBizIdRequest request = new JumioBizIdRequest(userId, bizId, typeCode);
			APIResponse<JumioInfoVo> response = jumioApi.getByBizId(APIRequest.instance(request));
			if (response == null || response.getStatus() != APIResponse.Status.OK) {
				return null;
			} else {
				return response.getData();
			}
		} catch (Exception e) {
			log.warn("获取JUMIO信息失败. userId:{} bizId:{} typeCode:{}", userId, bizId, typeCode, e);
			return null;
		}
	}

	/**
	 * 查询一个用户的全部JUMOIO
	 * 
	 * @param userId
	 * @return
	 */
	public List<JumioInfoVo> getByUserId(Long userId) {
		if (userId == null) {
			return Collections.emptyList();
		}
		try {
			APIResponse<List<JumioInfoVo>> response = jumioApi.getByUserId(APIRequest.instance(userId));
			if (response == null || response.getStatus() != APIResponse.Status.OK) {
				log.warn("get jumio info by userId fail. ", userId, JSON.toJSONString(response));
				return Collections.emptyList();
			}
			return response.getData();
		} catch (Exception e) {
			log.warn("get Jumio info by userId:{} fail.", userId, e);
			return Collections.emptyList();
		}
	}

	/**
	 * 获取用户最后一笔记录的申请信息
	 * 
	 * @param userId
	 * @return
	 */
	public JumioInfoVo getLastByUserId(@NonNull Long userId) {
		APIResponse<JumioInfoVo> response = jumioApi.getLastJumio(APIRequest.instance(userId));
		if (response == null || response.getStatus() != APIResponse.Status.OK) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		} else {
			return response.getData();
		}
	}

	public void changeJumioStatus(ChangeStatusRequest request) {
		APIResponse response = jumioApi.changeJumioStatus(APIRequest.instance(request));
		if (response == null || response.getStatus() != APIResponse.Status.OK) {
			log.warn("修改jumio状态信息失败. userId:{} jumioId:{} ", request.getUserId(), request.getJumioId());
		}
	}

	//判断当前jumio 是否可以复用
	public JumioInfoVo reuseCurrentJumio(Long userId) {
		JumioInfoVo jumioInfoVo = this.getLastByUserId(userId);

		if (jumioInfoVo == null) {
			return null;
		}

		log.info("当前存在jumio记录,判断是否复用该条记录. userId:{},jumioStatus:{}", userId, jumioInfoVo.getStatus());

		if (jumioInfoVo.getStatus() == null) {
			return null;
		}
		switch (jumioInfoVo.getStatus()) {
		case INIT:
		case REFUED:
		case ERROR:
		case EXPIRED:
			log.info("当前存在jumio记录,但不能使用,需要重新init. userId:{},jumioStatus:{}", userId, jumioInfoVo.getStatus());
			return null;
		case PASSED:
		case REVIEW:
		case UPLOADED:
			return jumioInfoVo;
		default:
			return null;
		}
	}
}
