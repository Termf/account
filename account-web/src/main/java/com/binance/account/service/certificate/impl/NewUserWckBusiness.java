package com.binance.account.service.certificate.impl;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserChannelWckAudit;
import com.binance.account.data.entity.certificate.UserChannelWckAuditLog;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserChannelWckAuditLogMapper;
import com.binance.account.data.mapper.certificate.UserChannelWckAuditMapper;
import com.binance.account.domain.bo.NewWckAuditDataHolder;
import com.binance.account.service.certificate.CertificateHelper;
import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.certificate.request.UserChannelWckQuery;
import com.binance.account.vo.certificate.request.WckChannelAuditRequest;
import com.binance.inspector.api.WorldCheckApi;
import com.binance.inspector.vo.worldcheck.WckResultProfileVo;
import com.binance.inspector.vo.worldcheck.request.WckInspectApplyRequest;
import com.binance.inspector.vo.worldcheck.response.WckInspectApplyResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.platform.common.TrackingUtils;
import com.google.common.collect.Lists;
import io.shardingsphere.api.HintManager;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class NewUserWckBusiness {

	@Resource
	ApplicationEventPublisher applicationEventPublisher;
	@Resource
	private UserChannelWckAuditMapper userChannelWckAuditMapper;
	@Resource
	private UserChannelWckAuditLogMapper userChannelWckAuditLogMapper;
	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;
	@Autowired
	private CertificateHelper certificateHelper;
	@Autowired
	private WorldCheckApi worldCheckApi;

	/**
	 * checkout渠道发起world check审核 如果这个用户校验的姓名之前校验过,且状态为终态,则返回之前校验的Vo,否则返回为空
	 */
	public UserChannelWckAuditVo applyWorldCheck(Long userId, String origin, String checkName, String birthDate,
			String nationality) {
		if (StringUtils.isBlank(checkName) || userId == null) {
			log.warn(
					"NewUserWckBusiness applyWorldCheck error, the param is illegal, the userId is:{} the checkName is:{}, the birthDate is:{}, the nationality is:{}, the origin is:{}",
					userId, checkName, birthDate, nationality, origin);
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		String caseId = certificateHelper.getDomainFlag() + userId + origin + System.currentTimeMillis();
		WckInspectApplyRequest request = new WckInspectApplyRequest();
		request.setCaseId(caseId);
		request.setName(checkName);
		request.setBirthDate(birthDate);
		request.setNationality(nationality.toUpperCase());
		// 检测这个用户的姓名之前是否做过worldcheck，如果已经做过则直接返回
		HintManager hintManager = null;
		List<UserChannelWckAudit> userChannelWckAudits = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			userChannelWckAudits = userChannelWckAuditMapper.selectByUserId(userId);
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		for (UserChannelWckAudit userChannelWckAudit : userChannelWckAudits) {
			if (StringUtils.equals(checkName, userChannelWckAudit.getCheckName())) {
				log.info("NewUserWckBusiness applyWorldCheck duplicate, the userId is:{} the checkName is:{}", userId,
						checkName);
				if (WckChannelStatus.isEndStatus(userChannelWckAudit.getStatus())) {
					return convertWckChannelAuditDoToVo(userChannelWckAudit);
				} else {
					return null;
				}
			}

		}

		String trackId = TrackingUtils.getTrace();

		AsyncTaskExecutor.execute(() -> {
			UserChannelWckAudit userChannelWckAudit = new UserChannelWckAudit();
			userChannelWckAudit.setCaseId(caseId);
			userChannelWckAudit.setUserId(userId);
			userChannelWckAudit.setCheckName(checkName);
			userChannelWckAudit.setBirthDate(birthDate);
			userChannelWckAudit.setIssuingCountry(nationality.toUpperCase());
			userChannelWckAudit.setOrigin(origin);
			try {
				TrackingUtils.saveTrace(trackId);
				log.info("异步调用wch进行审核 usdrId:{} caseId:{}", userId, caseId);
				APIResponse<WckInspectApplyResponse> response = worldCheckApi
						.applyWorldCheck(APIRequest.instance(request));
				if (response.getStatus() == APIResponse.Status.OK) {
					WckInspectApplyResponse body = response.getData();
					userChannelWckAudit.setStatus(WckChannelStatus.INITIAL);
					userChannelWckAudit.setCaseSystemId(body.getCaseSystemId());
				} else {
					userChannelWckAudit.setStatus(WckChannelStatus.ERROR);
					log.warn("NewUserWckBusiness applyWorldCheck failed, request:{}, response:{}", request, response);
				}
			} catch (Exception e) {
				log.error("NewUserWckBusiness applyWorldCheck exception, request:{}", request, e);
				userChannelWckAudit.setStatus(WckChannelStatus.ERROR);
			}finally{
				TrackingUtils.clearTrace();
			}
			userChannelWckAuditMapper.insertSelective(userChannelWckAudit);
		});
		return null;
	}

	/**
	 * channel用户world check审核列表分页查询
	 */
	public SearchResult<UserChannelWckAuditVo> listForAdminByPage(UserChannelWckQuery query) {
		if (query.getUserId() == null && StringUtils.isNotBlank(query.getEmail())) {
			query.setUserId(certificateHelper.getUserByEmail(query.getEmail()));
		}

		// 准备翻页
		SearchResult<UserChannelWckAuditVo> result = null;
		List<Map<String, Object>> rawList = userChannelWckAuditMapper.selectChannelWckAuditInfoByPage(query.getUserId(),
				query.getStatus(), query.getFirstAuditorId(), query.getSecondAuditorId(), query.getAuditorSeq(), query.getCountry(), query.getStart(),
				query.getRows());

		if (CollectionUtils.isNotEmpty(rawList)) {
			// 处理原始数据
			List<UserChannelWckAuditVo> auditVos = convertMapToWckChannelAuditVoList(rawList);
			Integer count = userChannelWckAuditMapper.countChannelWckAuditInfo(query.getUserId(), query.getStatus(),
					query.getFirstAuditorId(), query.getSecondAuditorId(), query.getAuditorSeq(), query.getCountry());
			result = new SearchResult<>(auditVos, count);
		}
		return result;
	}

	/**
	 * 查询 channel用户 world check审核结果
	 * 
	 * @param caseId 即user_channel_wck_audit表的case_id
	 */
	public List<WckResultProfileVo> getChannelWckResultProfile(String caseId) {
		APIResponse<List<WckResultProfileVo>> rs = worldCheckApi.getWckResultProfile(caseId);
		if (rs.getStatus() == APIResponse.Status.OK) {
			return rs.getData();
		} else {
			log.warn("getWckResultProfile failed, caseId: {}, msg:{}", caseId, rs.getErrorData());
			throw new BusinessException("getWckResultProfile failed:" + rs.getErrorData());
		}
	}

	/**
	 * channel 用户world check审核进度
	 */
	public List getChannelWcAuditEvents(String caseId) {
		APIResponse<List> rs = worldCheckApi.getWcAuditEvents(caseId);
		if (rs.getStatus() == APIResponse.Status.OK) {
			return rs.getData();
		} else {
			log.warn("getChannelWcAuditEvents failed, caseId: {}, msg:{}", caseId, rs.getErrorData());
			throw new BusinessException("getChannelWcAuditEvents failed:" + rs.getErrorData());
		}
	}

	/**
	 * 根据userId查询channel用户wck审核信息
	 * 
	 * @param userId
	 * @return
	 */
	public List<UserChannelWckAuditVo> getUserChannelWckAuditByUserId(Long userId) {
		List<UserChannelWckAudit> auditList = userChannelWckAuditMapper.selectByUserId(userId);
		List<UserChannelWckAuditVo> result = new LinkedList<>();
		for (UserChannelWckAudit userChannelWckAudit : auditList) {
			result.add(convertWckChannelAuditDoToVo(userChannelWckAudit));
		}
		return result;
	}

	/**
	 * channel worldCheck审核步骤： 1.准备数据 2.基础数据验证 3.执行新审核逻辑 4.结果落库 5.视情况更新用户的riskRating
	 * level
	 */
	public void newAudit(WckChannelAuditRequest request) {
		log.info("start new audit wck, the request is:{}", request);
		UserChannelWckAudit userChannelWckAudit = userChannelWckAuditMapper.selectByCaseId(request.getCaseId());
		List<UserChannelWckAuditLog> auditLogs = userChannelWckAuditLogMapper
				.selectByCaseIds(Lists.newArrayList(request.getCaseId()));

		NewWckAuditDataHolder newHolder = NewWckAuditDataHolder.build(request);

		newHolder.prepareData(userChannelWckAudit, auditLogs);
		newHolder.validate();
		newHolder.doAudit();

		userChannelWckAuditMapper.updateByPrimaryKeySelective(newHolder.getResult());
		userChannelWckAuditLogMapper.insertSelective(newHolder.getAuditLog());

		if (newHolder.isFinished()) {
			// 当整个审核流程结束时,更新用户的riskRating level

			// 判断当前审核是否是最后一条wch，更具创建时间来判断
			List<UserChannelWckAudit> wckAudits = userChannelWckAuditMapper
					.selectByUserId(userChannelWckAudit.getUserId());
			wckAudits.sort((v1, v2) -> v1.getCreateTime().after(v2.getCreateTime()) ? -1 : 0);
			if (wckAudits.get(0).getCaseId().equals(request.getCaseId())) {
				RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
				event.setUserChannelWckAuditVo(convertWckChannelAuditDoToVo(newHolder.getResult()));
				event.setTraceId(TrackingUtils.getTrace());
				event.setUserId(userChannelWckAudit.getUserId());
				log.info("start publish wck audit event, the audit vo is:{}", event.getUserChannelWckAuditVo());
				applicationEventPublisher.publishEvent(event);
			}

		}
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public void resetChannelWck(String caseId) {
		log.info("start reset wck ,the caseId is:{}", caseId);
		if (StringUtils.isBlank(caseId)) {
			log.warn("NewUserWckBusiness resetChannelWck error, the param is illegal, the caseId is:{}", caseId);
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		UserChannelWckAudit userChannelWckAudit = userChannelWckAuditMapper.selectByCaseId(caseId);
		if (userChannelWckAudit == null) {
			log.warn("NewUserWckBusiness resetChannelWck error, userChannelWckAudit can not find, the caseId is:{}",
					caseId);
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		userChannelWckAudit.setStatus(WckChannelStatus.AUDIT_FIRST);
		userChannelWckAuditMapper.updateByPrimaryKeySelective(userChannelWckAudit);
		userChannelWckAuditLogMapper.deleteByCaseId(caseId);
		userChannelRiskRatingMapper.resetWckStatusByUserId(userChannelWckAudit.getUserId());
	}

	private List<UserChannelWckAuditVo> convertMapToWckChannelAuditVoList(List<Map<String, Object>> rawList) {
		List<Long> userIds = rawList.stream().map(rawData -> {
			Long userId = (Long) rawData.get("user_id");
			return userId;
		}).collect(Collectors.toList());
		Map<Long, String> userIdToEmailMap = certificateHelper.getUserIdToEmailMap(userIds);
		List<UserChannelWckAuditVo> auditVos = rawList.stream().map(rawData -> {
			UserChannelWckAuditVo vo = new UserChannelWckAuditVo(rawData);
			HintManager hintManager = null;
			List<UserChannelWckAuditLog> auditLogs = null;
			try {
				hintManager = HintManager.getInstance();
				hintManager.setMasterRouteOnly();
				auditLogs = userChannelWckAuditLogMapper.selectByCaseIds(Lists.newArrayList(vo.getCaseId()));
			} finally {
				if (null != hintManager) {
					hintManager.close();
				}
			}
			if (auditLogs != null) {
				List<UserChannelWckAuditVo.UserChannelWckAuditLogVo> logVoList = new LinkedList<>();
				for (UserChannelWckAuditLog userChannelWckAuditLog : auditLogs) {
					UserChannelWckAuditVo.UserChannelWckAuditLogVo logVo = new UserChannelWckAuditVo.UserChannelWckAuditLogVo();
					BeanUtils.copyProperties(userChannelWckAuditLog, logVo);
					logVoList.add(logVo);
				}
				vo.setAuditLogs(logVoList);
			}
			vo.setEmail(userIdToEmailMap.get(vo.getUserId()));
			return vo;
		}).collect(Collectors.toList());
		return auditVos;
	}

	public UserChannelWckAuditVo convertWckChannelAuditDoToVo(UserChannelWckAudit userChannelWckAudit) {
		UserChannelWckAuditVo result = new UserChannelWckAuditVo();
		BeanUtils.copyProperties(userChannelWckAudit, result);
		result.setNationality(userChannelWckAudit.getIssuingCountry());

		HintManager hintManager = null;
		List<UserChannelWckAuditLog> auditLogs = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			auditLogs = userChannelWckAuditLogMapper
					.selectByCaseIds(Lists.newArrayList(userChannelWckAudit.getCaseId()));
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		if (auditLogs != null) {
			List<UserChannelWckAuditVo.UserChannelWckAuditLogVo> logVoList = new LinkedList<>();
			for (UserChannelWckAuditLog userChannelWckAuditLog : auditLogs) {
				UserChannelWckAuditVo.UserChannelWckAuditLogVo logVo = new UserChannelWckAuditVo.UserChannelWckAuditLogVo();
				BeanUtils.copyProperties(userChannelWckAuditLog, logVo);
				logVoList.add(logVo);
			}
			result.setAuditLogs(logVoList);
		}
		result.setEmail(certificateHelper.getEmailByUserId(result.getUserId()));
		return result;
	}

}
