package com.binance.account.service.certificate.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.JumioConst;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.JumioStatus;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.query.CompanyCertificateQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserCertificateListRequest;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.UserCertificate;
import com.binance.account.data.entity.certificate.UserCertificateIndex;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserCertificateIndexMapper;
import com.binance.account.data.mapper.certificate.UserCertificateMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.IWithdrawSecurityFace;
import com.binance.account.service.subuser.ISubUserAdmin;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.certificate.CompanyCertificateVo;
import com.binance.account.vo.certificate.UserCertificateVo;
import com.binance.account.vo.certificate.request.SaveCompanyCertificateRequest;
import com.binance.account.vo.certificate.request.SaveUserCertificateRequest;
import com.binance.account.vo.certificate.request.UserAuditCertificateResponse;
import com.binance.account.vo.certificate.request.UserDetectCertificateRequest;
import com.binance.account.vo.certificate.response.SaveUserCertificateResponse;
import com.binance.account.vo.certificate.response.UserDetectCertificateResponse;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.user.request.CompanyCertificateAuditRequest;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.faceid.response.FaceIdCardOcrResponse;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.data.account.OldUserIdPhotoMapper;
import com.binance.master.old.models.account.OldUserIdPhoto;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.risk.api.RiskFaceIdIndexApi;
import com.binance.risk.vo.cases.request.RiskFaceIdDeleteRequest;
import com.google.common.collect.Maps;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Service
@Log4j2
public class UserCertificateBusiness implements IUserCertificate {

	private static final String MESSAGE_ERROR_STATUS_REFRESH = "操作失败！状态错误，请刷新后重试";
	private static final String MESSAGE_SEND_COMPANY_EMAIL = "发送企业认证邮件";
	private static final int MAX_UPLOAD_TIMES = 3;

	@Resource
	private UserCertificateMapper userCertificateMapper;
	@Resource
	private UserCertificateIndexMapper userCertificateIndexMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	private UserIndexMapper userIndexMapper;
	@Resource
	private UserSecurityMapper userSecurityMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private OldUserIdPhotoMapper oldUserIdPhotoMapper;
	@Resource
	private UserKycApproveMapper userKycApproveMapper;
	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private JumioBusiness jumioBusiness;
	@Resource
	JumioMapper jumioMapper;
	@Resource
	private IMsgNotification iMsgNotification;
	@Resource
	private ICountry iCountry;
	@Resource
	private UserCommonBusiness userCommonBusiness;
	@Autowired
	private ISubUserAdmin subUserAdminBusiness;
	@Autowired
	private IFace iFace;
	@Autowired
	private IWithdrawSecurityFace iWithdrawSecurityFace;
	@Resource
	private IUserFace iUserFace;

	@Resource
	private UserInfoMapper userInfoMapper;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private RiskFaceIdIndexApi riskFaceIdIndexApi;

	@Resource
	private FaceIdApi faceIdApi;

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
	@Override
	public APIResponse<UserCertificateVo> getUserCertificateByUserId(APIRequest<UserIdRequest> request) {
		final UserIdRequest requestBody = request.getBody();
		UserCertificate userCertificate = this.userCertificateMapper.selectByPrimaryKey(requestBody.getUserId());
		UserCertificateVo userCertificateVo = null;
		if (null != userCertificate) {
			userCertificateVo = new UserCertificateVo();
			BeanUtils.copyProperties(userCertificate, userCertificateVo);
		}
		return APIResponse.getOKJsonResult(userCertificateVo);
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public APIResponse<SaveUserCertificateResponse> saveUserCertificate(
			APIRequest<SaveUserCertificateRequest> request) {
		final SaveUserCertificateRequest requestBody = request.getBody();
		if (null != requestBody) {
			UserCertificate record = new UserCertificate();
			BeanUtils.copyProperties(requestBody, record);
			record.setInsertTime(DateUtils.getNewUTCDate());
			record.setUpdateTime(DateUtils.getNewUTCDate());
			record.setVersion(0);
			int status = this.userCertificateMapper.insertIgnore(record);
			if (status <= 0) {
				record.setInsertTime(null);
				record.setVersion(requestBody.getVersion());
				this.userCertificateMapper.updateByPrimaryKeySelective(record);
			}
		}
		return APIResponse.getOKJsonResult(new SaveUserCertificateResponse());
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public APIResponse<SaveUserCertificateResponse> uploadUserCertificate(
			APIRequest<SaveUserCertificateRequest> request) {
		final SaveUserCertificateRequest requestBody = request.getBody();
		UserCertificateIndex tempIndex = this.userCertificateIndexMapper.selectByPrimaryKey(requestBody.getNumber(),
				requestBody.getCountry(), null);
		if (tempIndex != null && tempIndex.getUserId().longValue() != requestBody.getUserId().longValue()) {
			throw new BusinessException(GeneralCode.USER_CERTIFICATE_USE);// 证件号码已经被使用
		}
		UserCertificate temp = this.userCertificateMapper.selectByPrimaryKey(requestBody.getUserId());
		if (temp != null) {
			switch (temp.getStatus()) {// 0,"审核中" , 1,"通过" 2,"拒绝"
			case 0:
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_AUDIT);// 审核中不需要继续提交
			case 1:
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_PASS);// 审核通过不需要继续提交
			case 2:
				break;
			default:
				break;
			}
		}
		requestBody.setStatus((byte) 0);
		return this.saveUserCertificate(request);
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.NOT_SUPPORTED, readOnly = true)
	@Override
	public APIResponse<UserDetectCertificateResponse> userDetectCertificate(
			APIRequest<UserDetectCertificateRequest> request) {
		final UserDetectCertificateRequest requestBody = request.getBody();
		if (StringUtils.isBlank(requestBody.getCountry())) {
			requestBody.setCountry("");
		}
		UserCertificateIndex tempIndex = this.userCertificateIndexMapper.selectByPrimaryKey(requestBody.getNumber(),
				requestBody.getCountry(), null);
		if (tempIndex != null && tempIndex.getUserId().longValue() != requestBody.getUserId().longValue()) {
			throw new BusinessException(GeneralCode.USER_CERTIFICATE_USE);// 证件号码已经被使用
		}
		UserCertificate temp = this.userCertificateMapper.selectByPrimaryKey(requestBody.getUserId());
		if (temp != null) {
			switch (temp.getStatus()) {// 0,"审核中" , 1,"通过" 2,"拒绝"
			case 0:
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_AUDIT);// 审核中不需要继续提交
			case 1:
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_PASS);// 审核通过不需要继续提交
			case 2:
				break;
			default:
				break;
			}
		}
		return APIResponse.getOKJsonResult(new UserDetectCertificateResponse());
	}

	@Deprecated
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void isDataStaus(final Long id, final Long userId) {
		if (id != null) {
			OldUserIdPhoto oldUserIdPhoto = this.oldUserIdPhotoMapper.selectByPrimaryKey(id);
			if (oldUserIdPhoto != null) {
				UserCertificate userCertificate = this.userCertificateMapper.selectByPrimaryKey(userId);
				if (userCertificate == null) {
					userCertificate = new UserCertificate();
					userCertificate.setUserId(Long.valueOf(oldUserIdPhoto.getUserid())); // 用户id
					userCertificate.setFront(oldUserIdPhoto.getFront()); // 证件正面
					userCertificate.setBack(oldUserIdPhoto.getBack()); // 证件反面
					userCertificate.setHand(oldUserIdPhoto.getHand()); // 手持证件
					userCertificate.setFirstName(oldUserIdPhoto.getFirstname()); //
					userCertificate.setLastName(oldUserIdPhoto.getLastname()); //
					userCertificate.setMessage(oldUserIdPhoto.getMessage()); // 消息
					userCertificate.setLastAuditor(oldUserIdPhoto.getAuditor()); // 最后审核人
					userCertificate.setStatus(oldUserIdPhoto.getStatus()); // 0,"未审核" , 1,"已审核" 2,"拒绝"
					userCertificate.setNumber(oldUserIdPhoto.getNumber()); // 证件号码
					userCertificate.setType(oldUserIdPhoto.getType()); // 类型:
					userCertificate.setSex(oldUserIdPhoto.getSex()); // 性别:
					userCertificate.setCountry(oldUserIdPhoto.getCountry()); // 国家
					userCertificate.setVersion(oldUserIdPhoto.getVersion()); // 版本号
					userCertificate
							.setUpdateTime(null != oldUserIdPhoto.getUpdatetime() ? oldUserIdPhoto.getUpdatetime()
									: DateUtils.getNewUTCDate()); // 更新时间
					userCertificate.setInsertTime(oldUserIdPhoto.getCreatetime()); // 创建时间
					this.userCertificateMapper.insertIgnore(userCertificate);
				} else {
					userCertificate.setUserId(Long.valueOf(oldUserIdPhoto.getUserid())); // 用户id
					userCertificate.setFront(oldUserIdPhoto.getFront()); // 证件正面
					userCertificate.setBack(oldUserIdPhoto.getBack()); // 证件反面
					userCertificate.setHand(oldUserIdPhoto.getHand()); // 手持证件
					userCertificate.setFirstName(oldUserIdPhoto.getFirstname()); //
					userCertificate.setLastName(oldUserIdPhoto.getLastname()); //
					userCertificate.setMessage(oldUserIdPhoto.getMessage()); // 消息
					userCertificate.setLastAuditor(oldUserIdPhoto.getAuditor()); // 最后审核人
					userCertificate.setStatus(oldUserIdPhoto.getStatus()); // 0,"未审核" , 1,"已审核" 2,"拒绝"
					userCertificate.setNumber(oldUserIdPhoto.getNumber()); // 证件号码
					userCertificate.setType(oldUserIdPhoto.getType()); // 类型:
					userCertificate.setSex(oldUserIdPhoto.getSex()); // 性别:
					userCertificate.setCountry(oldUserIdPhoto.getCountry()); // 国家
					userCertificate.setVersion(oldUserIdPhoto.getVersion()); // 版本号
					userCertificate
							.setUpdateTime(null != oldUserIdPhoto.getUpdatetime() ? oldUserIdPhoto.getUpdatetime()
									: DateUtils.getNewUTCDate()); // 更新时间
					this.userCertificateMapper.updateByPrimaryKeySelective(userCertificate);
				}
				if (userCertificate.getStatus().byteValue() == (byte) 1) {
					UserCertificateIndex tempIndex = new UserCertificateIndex();
					tempIndex.setNumber(userCertificate.getNumber());
					tempIndex.setCountry(userCertificate.getCountry());
					tempIndex.setUserId(userCertificate.getUserId());
					int flag = this.userCertificateIndexMapper.insertIgnore(tempIndex);
					if (flag <= 0 && StringUtils.isNotBlank(tempIndex.getCountry())) {
						this.userCertificateIndexMapper.updateByPrimaryKeySelective(tempIndex);// 补全国家字段
					}
					final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
					if (null != userIndex) {
						final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
						final User userTemp = new User();
						userTemp.setEmail(userIndex.getEmail());
						userTemp.setStatus(BitUtils.disable(BitUtils.enable(status, Constant.USER_CERTIFICATION),
								Constant.USER_CERTIFICATION_TYPE));
						this.userMapper.updateUserStatusByEmail(userTemp);
						UserSecurity userSecurity = this.userSecurityMapper.selectByPrimaryKey(userId);
						UserSecurity record = new UserSecurity();
						record.setUserId(userId);
						if (userSecurity.getSecurityLevel() == null) {
							record.setSecurityLevel(2);
						} else {
							if (userSecurity.getSecurityLevel().intValue() < 2) {
								record.setSecurityLevel(2);
							}
						}
						if (record.getSecurityLevel() != null) {
							this.userSecurityMapper.updateByPrimaryKeySelective(record);
						}
					}
				}
			} else {
				log.warn("读写延迟找不到数据:id:{}", id);
			}
		}
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public APIResponse<UserAuditCertificateResponse> userAuditCertificate(
			APIRequest<SaveUserCertificateRequest> request) {
		final SaveUserCertificateRequest requestBody = request.getBody();
		this.isDataStaus(requestBody.getId(), requestBody.getUserId());
		UserCertificate temp = this.userCertificateMapper.selectByPrimaryKey(requestBody.getUserId());
		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(requestBody.getUserId());
		if (temp == null) {
			throw new BusinessException(GeneralCode.USER_AUDIT_NOT_EXIST);
		}
		if (requestBody.getStatus().byteValue() != (byte) 0) {// 非重置操作
			switch (temp.getStatus()) {// 0,"审核中" , 1,"通过" 2,"拒绝"
			case 1:
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_PASS);// 审核通过不需要继续提交
			case 2:
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_REFUSAL);// 审核拒绝不需要继续提交
			default:
				break;
			}
			if (userKycApprove != null) {
				throw new BusinessException(GeneralCode.USER_CERTIFICATE_PASS);
			}
			if (requestBody.getStatus().byteValue() == (byte) 1) {// 通过添加证件索引
				UserCertificateIndex userCertificateIndex = this.userCertificateIndexMapper
						.selectByPrimaryKey(temp.getNumber(), temp.getCountry(), null);
				if (userCertificateIndex != null
						&& userCertificateIndex.getUserId().longValue() != temp.getUserId().longValue()) {
					throw new BusinessException(GeneralCode.USER_CERTIFICATE_USE);// 证件号码已经被使用
				}
				UserCertificateIndex tempIndex = new UserCertificateIndex();
				tempIndex.setNumber(temp.getNumber());
				tempIndex.setCountry(temp.getCountry());
				tempIndex.setUserId(requestBody.getUserId());
				int flag = this.userCertificateIndexMapper.insertIgnore(tempIndex);
				if (flag <= 0 && StringUtils.isNotBlank(tempIndex.getCountry())) {
					this.userCertificateIndexMapper.updateByPrimaryKeySelective(tempIndex);// 补全国家字段
				}
				this.updateCertificateStatus(requestBody.getUserId(), true);
				this.updateSecurityLevel(requestBody.getUserId(), 2);

				// 插入到认证通过表
				Jumio jumio = Jumio.toJumio(temp);
				jumioMapper.insert(jumio);
				jumioMapper.updateByPrimaryKeySelective(jumio);
				UserKycApprove kycApprove = UserKycApprove.toKycApprove(temp);
				kycApprove.setJumioId(jumio.getId());
				userKycApproveMapper.insert(kycApprove);
			} else {
				this.resetUserCertificate(requestBody, temp);
			}
		} else {// 重置为初始状态
			if (userKycApprove != null) {
				userKycApproveMapper.deleteByPrimaryKey(requestBody.getUserId());
			}
			this.resetUserCertificate(requestBody, temp);
		}
		this.saveUserCertificate(request);
		return APIResponse.getOKJsonResult(new UserAuditCertificateResponse());
	}

	@Override
	public int updateCertificateStatus(Long userId, boolean isPassed) {
		UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
		if (null != userIndex) {
			final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
			final User userTemp = new User();
			userTemp.setEmail(userIndex.getEmail());
			if (isPassed) {

				try {
					// 将tradeLevel>=1的普通用户自动设置为母账户
					if (apolloCommonConfig.isAutoEnableSubUserFunctionDueToTradeLevel()) {
						final UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
						Integer tradeLevel = userInfo.getTradeLevel();
						if (null != tradeLevel && tradeLevel.compareTo(1) >= 0) {
							// 普通用户
							if (userCommonBusiness.isNormalUser(status)) {
								APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
								ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
								parentUserIdReq.setParentUserId(userId);
								subUserReq.setBody(parentUserIdReq);
								APIResponse<Boolean> result = subUserAdminBusiness.enableSubUserFunction(subUserReq);
								log.info("autoEnableSubUserFunction for tradeLevel>=1 parentUserId:{}, result:{}",
										userId, result);
							}
						}
					}

				} catch (Exception e) {
					log.error("enableSubUserFunction error for tradeLevel>=1", e);
				}

				userTemp.setStatus(BitUtils.disable(BitUtils.enable(status, Constant.USER_CERTIFICATION),
						Constant.USER_CERTIFICATION_TYPE));
			} else {
				userTemp.setStatus(BitUtils.disable(BitUtils.disable(status, Constant.USER_CERTIFICATION),
						Constant.USER_CERTIFICATION_TYPE));
			}

			return this.userMapper.updateUserStatusByEmail(userTemp);
		}
		return 0;
	}

	@Override
	public int updateSecurityLevel(Long userId, Integer securityLevel) {
		UserSecurity record = new UserSecurity();
		record.setUserId(userId);
		record.setSecurityLevel(securityLevel);
		return this.userSecurityMapper.updateByPrimaryKeySelective(record);
	}

	private void resetUserCertificate(final SaveUserCertificateRequest requestBody, final UserCertificate temp) {
		UserSecurity record = new UserSecurity();
		record.setUserId(temp.getUserId());
		record.setSecurityLevel(1);// 普通认证
		this.userSecurityMapper.updateByPrimaryKeySelective(record);
		final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(requestBody.getUserId());
		if (null != userIndex) {
			final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
			if (BitUtils.isTrue(status, Constant.USER_CERTIFICATION)
					|| BitUtils.isTrue(status, Constant.USER_CERTIFICATION_TYPE)) {
				final User userTemp = new User();
				userTemp.setEmail(userIndex.getEmail());
				userTemp.setStatus(BitUtils.disable(BitUtils.disable(status, Constant.USER_CERTIFICATION),
						Constant.USER_CERTIFICATION_TYPE));
				this.userMapper.updateUserStatusByEmail(userTemp);
			}
		}
	}

	@Override
	public APIResponse<JumioTokenResponse> uploadCompanyCertificate(APIRequest<SaveCompanyCertificateRequest> request) {
		final SaveCompanyCertificateRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		// 验证country输入是否正确，
		Country country = iCountry.getCountryByCode(requestBody.getCompanyCountry());
		if (country == null) {
			log.warn("upload company kyc get country code fail. userId:{} countryCode:{}", userId,
					requestBody.getCompanyCountry());
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		Lock lock = RedisCacheUtils.getLock(AccountConstants.COMPANY_KYC_INIT_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					return processUploadCompanyCertificate(userId, requestBody);
				} finally {
					if (lock != null) {
						lock.unlock();
					}
				}
			} else {
				log.info("获取公司KYC认证锁失败. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			// do nothing
			log.debug("kyc lock InterruptedException. userId:{}", userId);
		}
		throw new BusinessException(GeneralCode.SYS_ERROR);
	}

	private APIResponse<JumioTokenResponse> processUploadCompanyCertificate(Long userId,
			SaveCompanyCertificateRequest requestBody) {
		log.info("请求开始个人KYC认证：userId:{}", userId);
		JumioTokenResponse jumioTokenResponse = null;
		UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
		CompanyCertificate companyCertificate = this.companyCertificateMapper.getLast(userId);
		// 是否设置了手机和google验证
		if (userSecurity == null
				|| (StringUtils.isBlank(userSecurity.getAuthKey()) && StringUtils.isBlank(userSecurity.getMobile()))) {
			log.info("用户没有设置手机或者goole验证, 需要先绑定. userId:{}", userId);
			throw new BusinessException(GeneralCode.USER_SERCURITY_NOT_BIND);
		}
		// 是否有正在审核的KYC数据
		validateHadReviewKyc(userId, companyCertificate);
		// 进入企业验证次数是否达到限制次数
		if (jumioBusiness.getDailyJumioTimes(userId,
				com.binance.inspector.common.enums.JumioHandlerType.USER_KYC) >= MAX_UPLOAD_TIMES) {
			log.info("企业验证此时24小时内达到限制次数. userId:{}", userId);
			throw new BusinessException(GeneralCode.USER_KYC_UPLOAD_EXCEED_LIMIT_TODAY);
		}
		jumioTokenResponse = initCompanyCertificate(userId, requestBody, companyCertificate, userSecurity);
		return APIResponse.getOKJsonResult(jumioTokenResponse);
	}

	public void validateHadReviewKyc(Long userId, CompanyCertificate companyCertificate) {
		// 是否已经通过KYC
		if (userKycApproveMapper.selectByPrimaryKey(userId) != null) {
			log.info("用户已经KYC验证通过，无需再次验证. userId:{}", userId);
			throw new BusinessException(GeneralCode.USER_KYC_PASSED);
		}
		KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
		if (certificateResult.getCertificateStatus() == null) {
			return;
		}
		log.info("INIT_COMPANY_KYC==> 检查到当前用户存在有KYC认证历史信息：userId:{} type:{} status:{} id:{} isForbidPassed:{}", userId,
				certificateResult.getCertificateType(), certificateResult.getCertificateStatus(),
				certificateResult.getCertificateId(), certificateResult.isForbidPassed());
		// 存在有验证KYC的信息，需要进一笔检验
		if (KycCertificateResult.STATUS_PASS == certificateResult.getCertificateStatus()) {
			// 已经通过KYC认证 不能再次验证
			throw new BusinessException(GeneralCode.USER_KYC_PASSED);
		}
		if (KycCertificateResult.STATUS_REFUSED == certificateResult.getCertificateStatus()
				&& certificateResult.isForbidPassed()) {
			log.info("当前用户认证由于不合规国籍认证通过过，不能再次认证：userId:{} type:{} id:{}", userId,
					certificateResult.getCertificateType(), certificateResult.getCertificateId());
			throw new BusinessException(AccountErrorCode.KYC_FORBID_COUNTRY_PASS_STATUS_REFUSED);
		}
		// 如果是正在验证中，需要进一步判断
		if (KycCertificateResult.STATUS_REVIEW == certificateResult.getCertificateStatus()
				&& certificateResult.getCertificateType() != null) {
			if (KycCertificateResult.TYPE_USER == certificateResult.getCertificateType()) {
				// 正在做个人认证的情况下，不能做企业认证
				log.info("当前用户正在做个人认证且正在审核中，不能创建新的个人认证流程. userId:{}", userId);
				throw new BusinessException(GeneralCode.USER_KYC_PENDING);
			} else {
				// 正在做企业认证的情况下，判断最后一笔记录是否与当前验证的记录匹配，如果不匹配，则不能创建
				if (companyCertificate != null
						&& !CompanyCertificateStatus.pending.equals(companyCertificate.getStatus())) {
					// KYC 已经不在最初始的待上传状态下，不能再次提交
					throw new BusinessException(GeneralCode.USER_KYC_PENDING);
				}
			}
		}
	}

	private void setCompanyCertificateWithdrawId(CompanyCertificate certificate, UserSecurity userSecurity) {
		if (userSecurity == null) {
			return;
		}
		if (UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO.equals(userSecurity.getWithdrawSecurityFaceStatus())) {
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userSecurity.getUserId(),
					FaceTransType.WITHDRAW_FACE.name(), null);
			certificate.setTransFaceLogId(faceLog.getTransId());
		}
	}

	private JumioTokenResponse checkJumioRedo(Long userId, Integer redoJumio, String jumioId,
			CompanyCertificate companyCertificate) {
		if (redoJumio != null && redoJumio == 0 && StringUtils.isNotBlank(jumioId)) {
			// 不需要进行jumio认证
			Jumio jumio = jumioMapper.selectByPrimaryKey(userId, jumioId);
			if (jumio != null && jumio.getStatus() != JumioStatus.expired) {
				companyCertificate.setJumioId(jumioId);
				companyCertificate.setScanReference(jumio.getScanReference());
				if (JumioStatus.jumioPassed.equals(jumio.getStatus())) {
					companyCertificate.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.PASSED.name());
					companyCertificate.setStatus(CompanyCertificateStatus.jumioPassed);
				} else {
					companyCertificate.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.REFUED.name());
					companyCertificate.setStatus(CompanyCertificateStatus.jumioRefused);
				}
				// 直接返回固定值，让前端直接跳过这步JUMIO的认证
				JumioTokenResponse response = new JumioTokenResponse();
				response.setAuthorizationToken("success");
				return response;
			}
		}
		return null;
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public JumioTokenResponse initCompanyCertificate(Long userId, SaveCompanyCertificateRequest requestBody,
			CompanyCertificate lastRecord, UserSecurity userSecurity) {
		// 已经存在就更新
		int row;
		CompanyCertificate companyCertificate;
		if (lastRecord != null && lastRecord.getStatus() == CompanyCertificateStatus.pending) {
			companyCertificate = lastRecord;
			companyCertificate.setCompanyName(requestBody.getCompanyName());
			companyCertificate.setCompanyCountry(requestBody.getCompanyCountry());
			companyCertificate.setApplyerName(requestBody.getApplyerName());
			companyCertificate.setApplyerEmail(requestBody.getApplyerEmail());
			companyCertificate.setContactNumber(requestBody.getContactNumber());
			companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			row = companyCertificateMapper.updateByPrimaryKeySelective(companyCertificate);
			if (StringUtils.isNotBlank(companyCertificate.getJumioId())) {
				// 如果已经存在有JUMIO的关联ID，直接进行查询，
				Jumio jumio = jumioMapper.selectByPrimaryKey(userId, companyCertificate.getJumioId());
				if (jumio != null) {
					JumioTokenResponse response = new JumioTokenResponse();
					response.setAuthorizationToken(jumio.getAuthToken());
					return response;
				}
			}
		} else {
			companyCertificate = new CompanyCertificate();
			BeanUtils.copyProperties(requestBody, companyCertificate);
			companyCertificate.setUserId(userId);
			companyCertificate.setStatus(CompanyCertificateStatus.pending);
			companyCertificate.setInsertTime(DateUtils.getNewUTCDate());
			companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			// 设置提币人脸关联的withdrawId
			setCompanyCertificateWithdrawId(companyCertificate, userSecurity);
			// 检查是否不需要重做JUMIO了
			Integer redoJumio = lastRecord == null ? null : lastRecord.getRedoJumio();
			String jumioId = lastRecord == null ? null : lastRecord.getJumioId();
			JumioTokenResponse tokenResponse = checkJumioRedo(userId, redoJumio, jumioId, companyCertificate);
			row = this.companyCertificateMapper.insert(companyCertificate);
			if (tokenResponse != null) {
				// 如果步需在做JUMIO的，直接返回
				return tokenResponse;
			}
		}
		if (row <= 0) {
			log.warn("初始化保存公司验证信息失败. userId: {}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		String bizId = String.valueOf(companyCertificate.getId());
		// 如果还没有jumio 信息，需要初始化JUMIO信息
		Jumio jumio = jumioBusiness.initWebJumio(userId,
				com.binance.inspector.common.enums.JumioHandlerType.COMPANY_KYC, bizId, false);
		log.info("save company kyc base info success, and init jumio. userId:{} bizId:{} success", userId, bizId);
		// jumio 生成后，更新jumio id 到 company certificate 表中
		companyCertificate.setJumioId(jumio.getId());
		companyCertificate.setScanReference(jumio.getScanReference());
		companyCertificate.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
		companyCertificate.setUpdateTime(DateUtils.getNewUTCDate());
		companyCertificateMapper.saveJumioId(companyCertificate);
		JumioTokenResponse response = new JumioTokenResponse();
		response.setAuthorizationToken(jumio.getAuthToken());
		return response;
	}

	@Override
	public APIResponse<?> companyAuditCertificate(APIRequest<CompanyCertificateAuditRequest> request) {
		final CompanyCertificateAuditRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		Long certificateId = requestBody.getId();
		CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, certificateId);
		if (companyCertificate == null) {
			throw new BusinessException(GeneralCode.USER_AUDIT_NOT_EXIST);
		}
		CompanyCertificate last = companyCertificateMapper.getLast(userId);
		if (last.getId().longValue() != companyCertificate.getId().longValue()) {
			log.info("企业认证审核记录不是最后一条记录: userId:{} certificateId:{}", userId, certificateId);
			return APIResponse.getErrorJsonResult("操作失败！请处理最后一条数据");
		}

		Jumio jumio = jumioMapper.selectByPrimaryKey(userId, companyCertificate.getJumioId());
		CompanyCertificateStatus newStatus = requestBody.getStatus();
		if (CompanyCertificateStatus.delete == newStatus
				&& CompanyCertificateStatus.delete == companyCertificate.getStatus()) {
			// 如果是删除操作，并且当前记录已经是删除的状态，则不再次操作。
			log.warn("company audit delete but record had delete status. userId:{} certificateId:{}", userId,
					certificateId);
			return APIResponse.getErrorJsonResult("操作失败！认证已经被删除");
		}
		boolean isForbidCountry = apolloCommonConfig.isKycPassForbidCountry(companyCertificate.getCompanyCountry());
		try {
			switch (newStatus) {
			case jumioPassed:
			case jumioRefused:
			case delete:
				// 重置为待人工确认状态
				auditCompanyCertificateResetHandler(userId, certificateId, jumio, companyCertificate, newStatus);
				break;
			case passed:
				// 审核通过
				auditCompanyCertificatePassHandler(userId, certificateId, companyCertificate, jumio,
						requestBody.getNumber(), isForbidCountry);
				break;
			case refused:
				auditCompanyCertificateRefusedHandler(userId, certificateId, companyCertificate, jumio,
						requestBody.getInfo(), requestBody.getRedoJumio());
				break;
			default:
				log.info("审核企业认证的审核状态错误. userId:{} auditStatus:{}", userId, newStatus);
				return APIResponse.getErrorJsonResult("操作失败！提交的审核状态有误");
			}
		} catch (BusinessException e) {
			return APIResponse.getErrorJsonResult(e.getMessage());
		}
		String reason = companyCertificate.getInfo();
		if (isForbidCountry && CompanyCertificateStatus.passed == newStatus) {
			newStatus = CompanyCertificateStatus.forbidPassed;
			reason = JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE;
		}
		companyCertificate.setStatus(newStatus);
		companyCertificate.setUpdateTime(new Date());
		companyCertificate.setInfo(reason);
		companyCertificateMapper.updateByPrimaryKey(companyCertificate);
		return APIResponse.getOKJsonResult(null);
	}

	/**
	 * 企业认证审核重置
	 *
	 * @param userId
	 * @param certificateId
	 * @param companyCertificate
	 */
	private void auditCompanyCertificateResetHandler(Long userId, Long certificateId, Jumio jumio,
			CompanyCertificate companyCertificate, CompanyCertificateStatus auditStatus) {
		log.info("重置或删除企业认证信息, userId:{} certificateId:{} auditStatus:{}", userId, certificateId, auditStatus);
		if (companyCertificate.getStatus() == CompanyCertificateStatus.passed) {
			// 已通过重置
			final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
			if (null != userIndex) {
				log.info("企业认证重置，变更用户状态。userId:{} certificateId:{}", userId, certificateId);
				final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
				final User userTemp = new User();
				userTemp.setEmail(userIndex.getEmail());
				userTemp.setStatus(BitUtils.disable(BitUtils.disable(status, Constant.USER_CERTIFICATION),
						Constant.USER_CERTIFICATION_TYPE));
				this.userMapper.updateUserStatusByEmail(userTemp);
			}
			// 原本已通过重置
			UserKycApprove kycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
			if (kycApprove != null && kycApprove.getCertificateType() != null
					&& kycApprove.getCertificateType() == KycCertificateResult.TYPE_COMPANY) {
				// 删除通过记录表中的记录
				log.info("重置已经通过企业认证的逻辑，删除原有的KYC认证通过信息. userId:{} certificateId:{}", userId, companyCertificate);
				userKycApproveMapper.deleteByPrimaryKey(userId);
			}
			// 把原来的证件号通过逻辑删除
			removeCertificateIndex(userId, certificateId, jumio.getIssuingCountry(), jumio.getNumber(),
					jumio.getDocumentType());
			log.info("企业认证重置/删除证件号映射关系。userId:{} certificateId:{} jumioId:{}");
			UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
			if (userSecurity.getSecurityLevel() != 1) {
				log.info("企业认证重置，变更用户安全等级为1级. userId:{} certificateId:{}", userId, certificateId);
				UserSecurity security = new UserSecurity();
				security.setUserId(userId);
				security.setSecurityLevel(1);
				userSecurityMapper.updateByPrimaryKeySelective(security);

				// 修改用户等级消息通知 start
				log.info("企业认证重置，通知用户等级变更消息. userId:{} certificateId:{}", userId, certificateId);
				Map<String, Object> dataMsg = Maps.newHashMap();
				dataMsg.put("userId", userId);
				dataMsg.put("level", 1);
				MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL,
						dataMsg);
				log.info("iMsgNotification security level 1:{}", JSON.toJSONString(msg));
				this.iMsgNotification.send(msg);

				// 强制修改用户提币额度
				log.info("企业认证重置，强制修改用户提币额度, userId:{} certificateId:{}", userId, certificateId);
				dataMsg.put("withdrawLimit", null);
				MsgNotification msg2 = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.WITHDRAW_LIMIT,
						dataMsg);
				log.info("iMsgNotification set withdraw limit null:{}", JSON.toJSONString(msg2));
				this.iMsgNotification.send(msg2);
			}
			// 删除人脸识别使用的正式照片
			iFace.removeFaceReferenceRefImage(userId);
		} else if (companyCertificate.getStatus() == CompanyCertificateStatus.forbidPassed) {
			// 把原来的证件号通过逻辑删除
			removeCertificateIndex(userId, certificateId, jumio.getIssuingCountry(), jumio.getNumber(),
					jumio.getDocumentType());
			iFace.removeFaceReferenceRefImage(userId);
		} else if (companyCertificate.getStatus() != CompanyCertificateStatus.refused
				&& CompanyCertificateStatus.delete != auditStatus) {
			log.info("企业认证重置，重置记录的状态不正确, 不能重置. userId:{} certificateId:{}", userId, certificateId);
			throw new BusinessException(GeneralCode.SYS_ERROR, MESSAGE_ERROR_STATUS_REFRESH);
		}

		// 清除下kyc信息的缓存
		UserKycHelper.clearKycCountryCache(userId);

		// 重置用户企业认证,设置为普通账户(关闭子母账户功能)
		try {
			APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
			ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
			parentUserIdReq.setParentUserId(userId);
			subUserReq.setBody(parentUserIdReq);
			APIResponse<Boolean> result = subUserAdminBusiness.disableSubUserFunction(subUserReq);
			log.info("disableSubUserFunction parentUserId:{}, result:{}", userId, result);
		} catch (Exception e) {
			log.error("disableSubUserFunction error, parentUserId:%s, msg:", userId, e);
		}
		if (CompanyCertificateStatus.delete == auditStatus) {
			// 如果是审核删除操作
			String scanRef = jumio == null ? null : jumio.getScanReference();
			deleteKycEndHandler(userId, certificateId, scanRef);
		}
		log.info("重置企业认证: userId:{} certificateId:{}", userId, certificateId);
	}

	@Override
	public void removeCertificateIndex(Long userId, Long certificateId, String country, String number,
			String documentType) {
		if (userId == null) {
			return;
		}
		if (!StringUtils.isAnyBlank(number, country)) {
			// 把通过的身份验证信息删除
			log.info("重置已经通过认证的逻辑，删除原有的证件使用信息: userId:{} certificateId:{} , country:{} type:{}", userId, certificateId,
					country, documentType);
			this.userCertificateIndexMapper.deleteByPrimaryKey(number, country, documentType, userId);
		}
	}

	@Override
	public void deleteKycEndHandler(Long userId, Long certificateId, String jumioScanRef) {
		try {
			// 1. 停止当前用户正在进行的KYC人脸识别流程
			iUserFace.endTransFaceLogStatus(userId, certificateId.toString(), FaceTransType.KYC_USER,
					TransFaceLogStatus.FAIL, "审核删除用户KYC");
			// 2. 同步jumio biz status
			jumioBusiness.syncJumioBizStatus(userId, jumioScanRef, JumioBizStatus.DELETE);
		} catch (Exception e) {
			log.error("人工审核 KYC 删除时同步状态异常. userId:{} certificateId:{}", userId, certificateId, e);
		}
		try {
			List<JumioInfoVo> jumioInfoVos = jumioBusiness.getByUserId(userId);
			List<String> paths = new ArrayList<>();
			for (JumioInfoVo vo : jumioInfoVos) {
				if (StringUtils.isNotBlank(vo.getFront())) {
					paths.add(vo.getFront());
				}
				if (StringUtils.isNotBlank(vo.getBack())) {
					paths.add(vo.getBack());
				}
				if (StringUtils.isNotBlank(vo.getFace())) {
					paths.add(vo.getFace());
				}
			}
			// Begin 获取face ocr
			try {
				APIResponse<FaceIdCardOcrResponse> faceIdResp = faceIdApi
						.getFaceIdCardOcrAll(APIRequest.instance(userId + ""));
				if (faceIdResp == null || faceIdResp.getStatus() != APIResponse.Status.OK) {
					log.warn("get jumio info by userId fail. ", userId, JSON.toJSONString(faceIdResp));
				} else {
					FaceIdCardOcrResponse data = faceIdResp.getData();
					if (data != null && data.getCardOcrVo() != null) {
						if (StringUtils.isNotBlank(data.getCardOcrVo().getFront())) {
							paths.add(data.getCardOcrVo().getFront());
						}
						if (StringUtils.isNotBlank(data.getCardOcrVo().getFaceCheck())) {
							paths.add(data.getCardOcrVo().getFaceCheck());
						}
						if (StringUtils.isNotBlank(data.getCardOcrVo().getFace())) {
							paths.add(data.getCardOcrVo().getFace());
						}
					}

					if (data != null && data.getHistoryVos() != null && !data.getHistoryVos().isEmpty()) {
						for (FaceIdCardOcrVo vo : data.getHistoryVos()) {
							if (StringUtils.isNotBlank(vo.getFront())) {
								paths.add(vo.getFront());
							}
							if (StringUtils.isNotBlank(vo.getFaceCheck())) {
								paths.add(vo.getFaceCheck());
							}
							if (StringUtils.isNotBlank(vo.getFace())) {
								paths.add(vo.getFace());
							}
						}
					}

				}
			} catch (Exception e) {
				log.error("get face id ocr all by userId fail userId:{}", userId, e);
			}
			// End 获取face ocr

			log.info("人工审核 KYC 删除相似脸对比信息. userId:{} paths:{}", userId, JSON.toJSONString(paths));
			if (paths.isEmpty()) {
				return;
			}
			// 3. 删除相似人脸关联的数据
			RiskFaceIdDeleteRequest request = new RiskFaceIdDeleteRequest();
			request.setPathList(paths);
			APIResponse<Boolean> response = riskFaceIdIndexApi.deleteFacesByFacePaths(APIRequest.instance(request));
			if (response == null || response.getStatus() != APIResponse.Status.OK || response.getData() == null
					|| !response.getData()) {
				log.error("KYC 删除相似脸对比库信息失败. userId:{} certificateId:{}", userId, certificateId,
						JSON.toJSONString(response));
			} else {
				log.info("KYC 删除相似脸对比库信息成功. userId:{} certificateId:{}", userId, certificateId);
			}
		} catch (Exception e) {
			log.error("人工审核 USER KYC 删除相似脸对比库异常. userId:{} certificateId:{}", userId, certificateId, e);
		}
	}

	/**
	 * 企业认证审核通过
	 *
	 * @param userId
	 * @param certificateId
	 * @param companyCertificate
	 * @param jumio
	 * @param auditNumber
	 * @param isForbidCountry
	 */
	private void auditCompanyCertificatePassHandler(Long userId, Long certificateId,
			CompanyCertificate companyCertificate, Jumio jumio, String auditNumber, boolean isForbidCountry) {
		if (companyCertificate.getStatus() != CompanyCertificateStatus.jumioPassed
				&& companyCertificate.getStatus() != CompanyCertificateStatus.jumioRefused) {
			log.info("企业认证当前记录状态不能进行审核通过. userId:{} certificateId:{}", userId, certificateId);
			throw new BusinessException(GeneralCode.SYS_ERROR, MESSAGE_ERROR_STATUS_REFRESH);
		}
		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
		if (userKycApprove != null) {
			log.info("企业认证审核，当前用户已经通过了, 不能再次认证. userId:{} certificateId:{}", userId, certificateId);
			throw new BusinessException(GeneralCode.SYS_ERROR, "操作失败！当前用户已经通过个人身份认证，无法再通过企业认证");
		}
		if (StringUtils.isBlank(jumio.getNumber())) {
			if (StringUtils.isBlank(auditNumber)) {
				throw new BusinessException(GeneralCode.SYS_ERROR, "操作失败！请先补全证件号码");
			} else {
				log.info("企业认证审核，补全证件号码. userId:{} certificateId:{}", userId, certificateId);
				jumio.setNumber(auditNumber);
				Jumio record = new Jumio();
				record.setId(jumio.getId());
				record.setUserId(jumio.getUserId());
				record.setNumber(auditNumber);
				jumioMapper.updateByPrimaryKeySelective(record);
			}
		}
		if (isIDNumberOccupied(jumio.getNumber(), jumio.getIssuingCountry(), jumio.getDocumentType(), userId)) {
			log.info("通过用户企业认证失败，证件号已被使用: userId:{}, certificateId:{} number:{}", userId, certificateId,
					jumio.getNumber());
			throw new BusinessException(GeneralCode.SYS_ERROR, "操作失败！当前证件号已被使用");
		}

		log.info("企业认证通过，绑定证件号和对应用户. userId:{} certificateId:{} isForbidCountry:{}", userId, certificateId,
				isForbidCountry);
		Country country = iCountry.getCountryByCode(jumio.getIssuingCountry());
		UserCertificateIndex certificateIndex = new UserCertificateIndex();
		certificateIndex.setNumber(jumio.getNumber());
		certificateIndex.setUserId(userId);
		certificateIndex.setCountry(country.getCode());
		certificateIndex.setType(jumio.getDocumentType());
		certificateIndex.setCreateTime(DateUtils.getNewUTCDate());
		certificateIndex.setCertificateType(KycCertificateResult.TYPE_COMPANY);
		this.userCertificateIndexMapper.insertIgnore(certificateIndex);

		if (!isForbidCountry) {
			log.info("企业认证通过，添加数据到user_kyc_approve表信息. userId:{} certificateId:{}", userId, certificateId);
			userKycApproveMapper.insert(UserKycApprove.toKycApprove(companyCertificate));
			log.info("企业认证通过，修改用户等级和状态, userId:{} certificateId:{}", userId, certificateId);
			final UserIndex userIndex = this.userIndexMapper.selectByPrimaryKey(userId);
			if (null != userIndex) {
				final Long status = this.userMapper.queryUserStatusByEmail(userIndex.getEmail());
				final User userTemp = new User();
				userTemp.setEmail(userIndex.getEmail());
				userTemp.setStatus(BitUtils.enable(BitUtils.enable(status, Constant.USER_CERTIFICATION),
						Constant.USER_CERTIFICATION_TYPE));
				this.userMapper.updateUserStatusByEmail(userTemp);
				UserSecurity record = new UserSecurity();
				record.setUserId(userId);
				record.setSecurityLevel(2);
				this.userSecurityMapper.updateByPrimaryKeySelective(record);
			}
			// 修改用户等级消息通知 start
			log.info("企业认证通过，发送通知变更用户等级. userId:{} certificateId:{}", userId, certificateId);
			Map<String, Object> dataMsg = Maps.newHashMap();
			dataMsg.put("userId", userId);
			dataMsg.put("level", 2);
			MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
			log.info("iMsgNotification security level 2:{}", JSON.toJSONString(msg));
			this.iMsgNotification.send(msg);

			// 通过企业认证,设置为母账户(开启子母账户功能)
			if (apolloCommonConfig.isAutoEnableSubUserFunction()) {
				try {
					APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
					ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
					parentUserIdReq.setParentUserId(userId);
					subUserReq.setBody(parentUserIdReq);
					APIResponse<Boolean> result = subUserAdminBusiness.enableSubUserFunction(subUserReq);
					log.info("autoEnableSubUserFunction parentUserId:{}, result:{}", userId, result);
				} catch (Exception e) {
					log.error("autoEnableSubUserFunction error, parentUserId:{}, msg:", userId, e);
				}
			}
		}
		// 发送邮件
		log.info("企业认证通过，发送通知邮件. userId:{} certificateId:{} isForbidCountry:{}", userId, certificateId,
				isForbidCountry);
		String emailTemplate = isForbidCountry ? AccountConstants.COMPANY_KYC_PASS_FORBID_COUNTRY_EMAIL_TEMPLATE
				: Constant.JUMIO_COMPANY_CHECK_SUCCESS;
		String reason = isForbidCountry ? JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE : null;
		userCommonBusiness.sendJumioCheckEmail(jumio.getUserId(), jumio.getIssuingCountry(), reason, emailTemplate,
				MESSAGE_SEND_COMPANY_EMAIL);

		// 清除下kyc信息的缓存
		UserKycHelper.clearKycCountryCache(userId);

		// 同步业务状态到INSPECTOR 的JUMIO 数据
		jumioBusiness.syncJumioBizStatus(userId, jumio.getScanReference(), JumioBizStatus.PASSED);
		// 把JUMIO 审核时的人脸对比照片同步到业务通过的照片信息中
		boolean saveRefImage = iFace.saveFaceReferenceRefImage(userId);
		log.info("通过用户企业认证: userId:{} certificateId:{} saveRefImage:{} ", userId, certificateId, saveRefImage);
		if (saveRefImage) {
			log.info("企业认证通过后，检查用户当前是否需要做提币人脸. userId:{} certificateId:{}", userId, certificateId);
			kycPassCheckSecurityFaceCheck(userId, certificateId.toString(),
					FaceTransType.KYC_COMPANY, companyCertificate.getTransFaceLogId(),
					isForbidCountry ? KycCertificateStatus.FORBID_PASS.name() : KycCertificateStatus.PASS.name(),
					DateUtils.getNewUTCDate());
		}
		// 同步人脸识别流程通过的状态
		iUserFace.endTransFaceLogStatus(userId, certificateId.toString(), FaceTransType.KYC_COMPANY,
				TransFaceLogStatus.PASSED, "企业认证审核通过");
	}

	@Override
	public void kycPassCheckSecurityFaceCheck(Long userId, String transId, FaceTransType faceTransType, String refTransId, String kycStatus, Date kycPassTime) {
		iWithdrawSecurityFace.kycPassCheckSecurityFaceCheck(userId, transId, faceTransType, refTransId, kycStatus, kycPassTime);
	}

	/**
	 * 企业认证审核拒绝
	 *
	 * @param userId
	 * @param certificateId
	 * @param companyCertificate
	 * @param jumio
	 * @param failReason
	 * @param redoJumio
	 */
	private void auditCompanyCertificateRefusedHandler(Long userId, Long certificateId,
			CompanyCertificate companyCertificate, Jumio jumio, String failReason, Integer redoJumio) {
		if (companyCertificate.getStatus() != CompanyCertificateStatus.jumioPassed
				&& companyCertificate.getStatus() != CompanyCertificateStatus.jumioRefused) {
			log.info("企业认证审核拒绝，当前记录状态错误. userId:{} certificateId:{}", userId, certificateId);
			throw new BusinessException(MESSAGE_ERROR_STATUS_REFRESH);
		}
		if (StringUtils.isNotBlank(failReason)) {
			companyCertificate.setInfo(failReason);
		}
		companyCertificate.setRedoJumio(redoJumio);

		String country = jumio == null ? "US" : jumio.getIssuingCountry();
		// 发送邮件
		log.info("企业认证审核拒绝，发送通知邮件. userId:{} certificateId:{}", userId, certificateId);
		userCommonBusiness.sendJumioCheckEmail(userId, country, failReason, Constant.JUMIO_COMPANY_CHECK_FAIL,
				MESSAGE_SEND_COMPANY_EMAIL);
		// 同步业务状态到INSPECTOR 的JUMIO 数据
		if (jumio != null) {
			jumioBusiness.syncJumioBizStatus(userId, jumio.getScanReference(), JumioBizStatus.REFUSED);
		}
		log.info("拒绝用户企业认证: userId:{} certificateId:{}", userId, certificateId);
		// 同步结束人脸识别的流程
		iUserFace.endTransFaceLogStatus(userId, certificateId.toString(), FaceTransType.KYC_COMPANY,
				TransFaceLogStatus.FAIL, "企业认证审核拒绝");
	}

	@Override
	public APIResponse<CompanyCertificateVo> getCompanyCertificate(APIRequest<UserIdRequest> request) {
		UserIdRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		CompanyCertificate certificate = this.companyCertificateMapper.getLast(userId);
		if (certificate != null && CompanyCertificateStatus.delete != certificate.getStatus()) {
			CompanyCertificateVo certificateVo = new CompanyCertificateVo();
			certificateVo.setCheckInfo(new CompanyCertificateVo.CheckInfo());
			BeanUtils.copyProperties(certificate, certificateVo);

			Jumio jumio = jumioMapper.selectByPrimaryKey(userId, certificate.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, certificateVo.getCheckInfo());
			}

			certificateVo.setEmail(userIndexMapper.selectEmailById(userId));
			return APIResponse.getOKJsonResult(certificateVo);
		}
		return APIResponse.getOKJsonResult(null);
	}

	@Override
	public APIResponse<SearchResult<CompanyCertificateVo>> getCompanyCertificateList(
			APIRequest<CompanyCertificateQuery> request) {
		SearchResult<CompanyCertificateVo> searchResult = new SearchResult<>();
		CompanyCertificateQuery query = request.getBody();
		if (StringUtils.isNotBlank(query.getEmail())) {
			User user = userMapper.queryByEmail(query.getEmail());
			if (user != null) {
				query.setUserId(user.getUserId());
			} else {
				return APIResponse.getOKJsonResult(searchResult);
			}
		}

		List<CompanyCertificateVo> certificateVos = new ArrayList<>();
		List<CompanyCertificate> certificates = companyCertificateMapper.getList(query);

		// 如果jumioList的值是空列表，不需要再次查询和组装信息，直接返回空列表
		if (certificates == null || certificates.isEmpty()) {
			searchResult.setRows(certificateVos);
			searchResult.setTotal(0);
			return APIResponse.getOKJsonResult(searchResult);
		}
		// 获得UserId <-> Email 映射
		Set<Long> userIds = certificates.stream().map(CompanyCertificate::getUserId).collect(Collectors.toSet());
		List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
		Map<Long, String> userEmailMapping = userIndices.stream()
				.collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));

		for (CompanyCertificate certificate : certificates) {

			CompanyCertificateVo certificateVo = new CompanyCertificateVo();
			certificateVo.setEmail(userEmailMapping.get(certificate.getUserId()));
			certificateVo.setCheckInfo(new CompanyCertificateVo.CheckInfo());
			BeanUtils.copyProperties(certificate, certificateVo);

			Jumio jumio = jumioMapper.selectByPrimaryKey(certificate.getUserId(), certificate.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, certificateVo.getCheckInfo());
			}
			certificateVos.add(certificateVo);
		}

		searchResult.setRows(certificateVos);
		searchResult.setTotal(companyCertificateMapper.getListCount(query));
		return APIResponse.getOKJsonResult(searchResult);
	}

	@Override
	public APIResponse<?> modifyCompanyCertificate(APIRequest<CompanyCertificateVo> request) {
		int rows = 0;
		try {
			CompanyCertificateVo requestBody = request.getBody();
			CompanyCertificate certificate = new CompanyCertificate();
			BeanUtils.copyProperties(requestBody, certificate);
			rows = companyCertificateMapper.updateByPrimaryKeySelective(certificate);
		} catch (Exception e) {
			log.error("后台修改企业认证信息失败");
		}
		if (rows < 1) {
			return APIResponse.getErrorJsonResult("修改企业认证信息失败!");
		}
		return APIResponse.getOKJsonResult(null);
	}

	@Override
	public boolean isIDNumberOccupied(String number, String countryCode, String type, Long userId) {
		List<UserCertificateIndex> indexs = null;

		KycCertificateResult result = userCommonBusiness.getKycStatusByUserId(userId);

		if (result.getCertificateType() != null
				&& KycCertificateResult.TYPE_COMPANY == result.getCertificateType().intValue()) {
			return false;
		}

		if (StringUtils.equalsIgnoreCase(countryCode, "CN") && StringUtils.equalsIgnoreCase(type, "DRIVING_LICENSE")) {
			indexs = this.userCertificateIndexMapper.selectCertificate(number, countryCode, "DRIVING_LICENSE");
		} else {
			indexs = this.userCertificateIndexMapper.selectCertificate(number, countryCode, type);
			if (indexs == null || indexs.isEmpty()) {
				indexs = this.userCertificateIndexMapper.selectCertificate(number, countryCode, null);
			}
		}

		boolean existsPerson = false;
		try {
			if (indexs != null && !indexs.isEmpty()) {
				// 补全信息
				for (UserCertificateIndex userCertificateIndex2 : indexs) {
					if (userCertificateIndex2.getCertificateType() != null) {
						continue;
					}
					// 查询 user_kyc_approve
					UserKycApprove userKycApprove = userKycApproveMapper
							.selectByPrimaryKey(userCertificateIndex2.getUserId());
					if (userKycApprove != null) {
						userCertificateIndex2.setCertificateType(userKycApprove.getCertificateType());
						userCertificateIndex2.setCreateTime(userKycApprove.getApproveTime());
						this.userCertificateIndexMapper.updateCertificateType(userCertificateIndex2);
					}

				}

				List<UserCertificateIndex> temp = indexs.stream()
						.filter(userCertificateIndex -> !userId.equals(userCertificateIndex.getUserId())
								&& new Integer(KycCertificateResult.TYPE_USER)
										.equals(userCertificateIndex.getCertificateType()))
						.collect(Collectors.toList());
				existsPerson = temp != null && !temp.isEmpty();

			}
		} catch (Exception e) {
			log.error("判断证件号是否被使用异常", e);
		}
		return existsPerson;
	}

	@Override
	public CompanyCertificate getCompanyKycFromMasterDbByJumioId(Long userId, String jumioId) {
		CompanyCertificate certificate = null;
		if (userId == null || jumioId == null) {
			return null;
		}
		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			certificate = companyCertificateMapper.getByJumioId(userId, jumioId);
		} catch (Exception e) {
			log.error("从主库获取公司认证信息异常. userId:{} jumioId:{} ", userId, jumioId, e);
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		return certificate;
	}

	/**
	 * 根据ID从主库获取数据
	 *
	 * @param userId
	 * @param id
	 * @return
	 */
	@Override
	public CompanyCertificate getCompanyKycFromMasterDbById(Long userId, Long id) {
		CompanyCertificate certificate = null;
		if (userId == null || id == null) {
			return null;
		}
		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			certificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
		} catch (Exception e) {
			log.error("从主库获取公司认证信息异常. userId:{} id:{} ", userId, id, e);
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		return certificate;
	}

	@Override
	public UserKyc getUserKycFromMasterDbByJumioId(Long userId, String jumioId) {
		UserKyc userKyc = null;
		if (userId == null || jumioId == null) {
			return null;
		}
		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			userKyc = userKycMapper.getByJumioId(userId, jumioId);
		} catch (Exception e) {
			log.error("从主库获取个人认证信息异常. userId:{} jumioId:{} ", userId, jumioId, e);
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		return userKyc;
	}

	@Override
	public UserKyc getUserKycFromMasterDbById(Long userId, Long id) {
		UserKyc userKyc = null;
		if (userId == null || id == null) {
			return null;
		}
		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			userKyc = userKycMapper.getById(userId, id);
		} catch (Exception e) {
			log.error("从主库获取个人认证信息异常. userId:{} id:{} ", userId, id, e);
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		return userKyc;
	}

	/**
	 * JUMIO 过期时的处理逻辑
	 */
	@Override
	public void jumioExpireHandler(Long userId, Jumio jumio) {
		switch (jumio.getType()) {
		case user:
			log.info("User Kyc 信息由于JUMIO 过期未上传进行过期处理. userId:{} jumioId:{}", userId, jumio.getId());
			UserKyc userKyc = getUserKycFromMasterDbByJumioId(jumio.getUserId(), jumio.getId());
			if (userKyc != null && !KycStatus.isEndStatus(userKyc.getStatus())) {
				userKycExpired(userKyc);
			}
			break;
		case company:
			log.info("Company Kyc 信息由于JUMIO 过期未上传进行过期处理. userId:{} jumioId:{}", userId, jumio.getId());
			CompanyCertificate certificate = getCompanyKycFromMasterDbByJumioId(jumio.getUserId(), jumio.getId());
			if (certificate != null && !CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
				companyKycExpired(certificate);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public void userKycExpired(UserKyc userKyc) {
		UserKyc record = new UserKyc();
		record.setId(userKyc.getId());
		record.setUserId(userKyc.getUserId());
		record.setStatus(KycStatus.expired);
		record.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.EXPIRED.name());
		record.setUpdateTime(DateUtils.getNewUTCDate());
		record.setFailReason(
				StringUtils.isBlank(userKyc.getFailReason()) ? "long time nothing upload" : userKyc.getFailReason());
		userKycMapper.updateStatus(record);
		kycExpiredExtra(userKyc.getUserId(), userKyc.getId(), FaceTransType.KYC_USER, userKyc.getScanReference(),
				userKyc.getJumioId());
	}

	@Override
	public void companyKycExpired(CompanyCertificate certificate) {
		CompanyCertificate record2 = new CompanyCertificate();
		record2.setId(certificate.getId());
		record2.setUserId(certificate.getUserId());
		record2.setStatus(CompanyCertificateStatus.expired);
		record2.setUpdateTime(DateUtils.getNewUTCDate());
		record2.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.EXPIRED.name());
		record2.setInfo(
				StringUtils.isBlank(certificate.getInfo()) ? "long time nothing upload" : certificate.getInfo());
		companyCertificateMapper.updateByPrimaryKeySelective(record2);
		kycExpiredExtra(certificate.getUserId(), certificate.getId(), FaceTransType.KYC_COMPANY,
				certificate.getScanReference(), certificate.getJumioId());
	}

	private void kycExpiredExtra(Long userId, Long kycId, FaceTransType faceTransType, String scanRef, String jumioId) {
		if (StringUtils.isNotBlank(jumioId)) {
			Jumio temp = new Jumio();
			temp.setId(jumioId);
			temp.setUserId(userId);
			temp.setStatus(JumioStatus.expired);
			temp.setUpdateTime(DateUtils.getNewUTCDate());
			jumioMapper.updateByPrimaryKeySelective(temp);
		}
		log.info("JUMIO过期同步人脸识别和JUMIO业务信息. userId:{} kycId:{}", userId, kycId);
		// 同步业务结果到INSPECTOR JUMIO
		jumioBusiness.syncJumioBizStatus(userId, scanRef, JumioBizStatus.EXPIRED);
		// 同步人脸识别流程的状态信息
		iUserFace.endTransFaceLogStatus(userId, kycId.toString(), faceTransType, TransFaceLogStatus.EXPIRED,
				"KYC认证过期自动结束");
	}

	/**
	 * JUMIO company 验证结果出来后的处理逻辑
	 *
	 * @param jumio
	 * @param jumioPass
	 * @param msg
	 */
	@Override
	public void handleCompanyCertificate(Jumio jumio, boolean jumioPass, String msg) {
		CompanyCertificate certificate = getCompanyKycFromMasterDbByJumioId(jumio.getUserId(), jumio.getId());
		if (certificate == null || CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
			log.info("当前企业认证流程已经进入终态，不能再变更. userId:{} jumioId:{}", jumio.getUserId(), jumio.getId());
			return;
		}
		if (jumioPass) {
			certificate.setStatus(CompanyCertificateStatus.jumioPassed);
		} else {
			certificate.setStatus(CompanyCertificateStatus.refused);
			userCommonBusiness.sendJumioCheckEmail(jumio.getUserId(), jumio.getIssuingCountry(), msg,
					Constant.JUMIO_COMPANY_CHECK_FAIL, MESSAGE_SEND_COMPANY_EMAIL);
			// 同步业务状态到INSPECTOR 的JUMIO 数据
			jumioBusiness.syncJumioBizStatus(jumio.getUserId(), jumio.getScanReference(), JumioBizStatus.REFUSED);
			// 同步人脸识别的认证流程
			iUserFace.endTransFaceLogStatus(certificate.getUserId(), certificate.getId().toString(),
					FaceTransType.KYC_COMPANY, TransFaceLogStatus.FAIL, "JUMIO认证拒绝自动拒绝");
		}
		CompanyCertificate record = new CompanyCertificate();
		record.setId(certificate.getId());
		record.setUserId(certificate.getUserId());
		record.setStatus(certificate.getStatus());
		record.setInfo(msg);
		if (jumio.getStatus() == JumioStatus.jumioPassed) {
			record.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.PASSED.name());
		} else {
			record.setJumioStatus(com.binance.inspector.common.enums.JumioStatus.REFUED.name());
		}
		record.setUpdateTime(DateUtils.getNewUTCDate());
		companyCertificateMapper.updateByPrimaryKeySelective(record);
	}

	@Override
	public boolean isJumioIdNumberUseByOtherUser(Long userId, String idNumber, String countryCode, String idType) {
		// 先验证userCertificateIndex 表中是否符合，如果符合，再验证reset表中的
		boolean haveUse = isIDNumberOccupied(idNumber, countryCode, idType, userId);
		if (haveUse) {
			log.info("KYC认证中, 用户证件号已经被别的用户占用: userId:{} idNumber:{} countCode:{}, idType:{}", userId, idNumber,
					countryCode, idType);
			return true;
		}
		// 检查reset中是否已经被占用
//        long haveResetUse = securityResetMapper.haveResetNumberExistByOtherUser(userId, idNumber, countryCode, idType);
//        if (haveResetUse > 0) {
//            log.info("重置记录中，用户证件号已经被别的用户占用: userId:{} idNumber:{} countCode:{}, idType:{}", userId, idNumber, countryCode, idType);
//            return true;
//        }
		return false;
	}

	@Override
	public SearchResult<UserCertificateVo> listUserCertificate(UserCertificateListRequest request) {
		try {
			SearchResult<UserCertificateVo> result = new SearchResult<>();

			if (StringUtils.isNotBlank(request.getEmail())) {
				User user = userMapper.queryByEmail(request.getEmail());
				if (user != null) {
					request.setUserId(user.getUserId());
				} else {
					return null;
				}
			}

			int count = userCertificateMapper.getListCount(request);

			List<UserCertificateVo> vos = new ArrayList<>();
			List<UserCertificate> datas = count == 0 ? null : userCertificateMapper.selectByPage(request);
			if (datas != null && !datas.isEmpty()) {
				Set<Long> userIds = datas.stream().map(UserCertificate::getUserId).collect(Collectors.toSet());
				List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
				Map<Long, String> userEmailMapping = userIndices.stream()
						.collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));

				for (UserCertificate userCertificate : datas) {
					UserCertificateVo vo = new UserCertificateVo();
					vo.setEmail(userEmailMapping.get(userCertificate.getUserId()));
					BeanUtils.copyProperties(userCertificate, vo);
					vo.setCreateTime(userCertificate.getInsertTime());
					vos.add(vo);
				}
			}

			result.setTotal(count);
			result.setRows(vos);
			return result;
		} catch (Exception e) {
			log.error("查询用户身份信息异常", e);
			return null;
		}

	}

	@Override
	public APIResponse<?> refuseCompanyCertificate(APIRequest<CompanyCertificateAuditRequest> request) {
		CompanyCertificateAuditRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
		if (companyCertificate == null || CompanyCertificateStatus.delete == companyCertificate.getStatus()) {
			throw new BusinessException(GeneralCode.USER_AUDIT_NOT_EXIST);
		}

		Long certificateId = companyCertificate.getId();
		Jumio jumio = jumioMapper.selectByPrimaryKey(userId, companyCertificate.getJumioId());

		if (jumio == null) {
			log.info("企业认证审核记录获取JUMIO认证信息失败: userId:{} certificateId:{}", userId, certificateId);
			return APIResponse.getErrorJsonResult("操作失败！审核数据缺失");
		}

		boolean isForbidCountry = apolloCommonConfig.isKycPassForbidCountry(companyCertificate.getCompanyCountry());
		String reason = requestBody.getInfo();
		try {
			// 重置
			auditCompanyCertificateResetHandler(userId, certificateId, jumio, companyCertificate,
					CompanyCertificateStatus.jumioPassed);

			companyCertificate.setStatus(CompanyCertificateStatus.jumioPassed);
			companyCertificate.setUpdateTime(new Date());
			companyCertificate.setInfo(reason);
			companyCertificateMapper.updateByPrimaryKey(companyCertificate);

			// 拒绝
			auditCompanyCertificateRefusedHandler(userId, certificateId, companyCertificate, jumio,
					requestBody.getInfo(), requestBody.getRedoJumio());
		} catch (BusinessException e) {
			return APIResponse.getErrorJsonResult(e.getMessage());
		}

		CompanyCertificateStatus newStatus = requestBody.getStatus();
		if (isForbidCountry && CompanyCertificateStatus.passed == newStatus) {
			newStatus = CompanyCertificateStatus.forbidPassed;
			reason = JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE;
		}
		companyCertificate.setStatus(newStatus);
		companyCertificate.setUpdateTime(new Date());
		companyCertificate.setInfo(reason);
		companyCertificateMapper.updateByPrimaryKey(companyCertificate);
		return APIResponse.getOKJsonResult(null);
	}

}
