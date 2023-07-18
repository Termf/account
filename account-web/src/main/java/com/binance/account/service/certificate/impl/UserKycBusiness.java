package com.binance.account.service.certificate.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.JumioConst;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.JumioStatus;
import com.binance.account.common.enums.JumioType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.KycSubStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.query.JumioQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserKycModularQuery;
import com.binance.account.common.validator.ValidateResult;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.AccUserKyc;
import com.binance.account.data.entity.certificate.AccUserKycExample;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.JumioHandlerType;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.account.data.entity.certificate.UserCertificateIndex;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.AccUserKycMapper;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserAddressMapper;
import com.binance.account.data.mapper.certificate.UserCertificateIndexMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.CertificateHelper;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.IWithdrawSecurityFace;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.certificate.KycDetailResponse;
import com.binance.account.vo.certificate.KycFormAddrVo;
import com.binance.account.vo.certificate.request.KycForceToExpiredRequest;
import com.binance.account.vo.certificate.response.KycFormAddrResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.certificate.response.UserSimpleBaseInfoResponse;
import com.binance.account.vo.security.request.UserIdAndIdRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserKycApproveVo;
import com.binance.account.vo.user.UserKycVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.account.vo.user.request.KycBaseInfoRequest;
import com.binance.account.vo.user.request.KycSimpleBaseInfoRequest;
import com.binance.account.vo.user.request.SaveJumioSdkScanRefRequest;
import com.binance.account.vo.user.request.UpdateKycApproveRequest;
import com.binance.account.vo.user.response.InitSdkUserKycResponse;
import com.binance.account.vo.user.response.JumioSdkInitResponse;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioBizStatus;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.common.enums.JumioScanSource;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.request.DirectSavePhotoRequest;
import com.binance.inspector.vo.jumio.request.JumioBaseRequest;
import com.binance.inspector.vo.jumio.request.XfersJumioKycRequest;
import com.binance.inspector.vo.jumio.response.JumioPhotoResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import com.binance.platform.amazon.s3.service.S3ObjectService;
import com.google.common.collect.Maps;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Nullable;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserKycBusiness implements IUserKyc {

	private static final int MAX_UPLOAD_TIMES = 3;
	private static final String MSG_ACTION_STATUS_ERROR = "操作失败！状态错误，请刷新后重试";
	private static final String MSG_ACTION_FAIL_REASO_MISS = "操作失败！缺失拒绝原因";
	private static final String MSG_KYC_EMAIL_SEND = "发送KYC认证邮件";

	@Resource
	private UserKycMapper userKycMapper;
	@Resource
	private AccUserKycMapper accUserKycMapper;
	@Resource
	private UserIndexMapper userIndexMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	private UserSecurityMapper userSecurityMapper;
	@Resource
	private UserCertificateIndexMapper userCertificateIndexMapper;
	@Resource
	private IMsgNotification iMsgNotification;
	@Resource
	private UserKycApproveMapper userKycApproveMapper;
	@Resource
	private CompanyCertificateMapper companyCertificateMapper;
	@Resource
	private JumioMapper jumioMapper;
	@Resource
	private JumioBusiness jumioBusiness;
	@Autowired
	private IUserCertificate iUserCertificate;
	@Autowired
	private UserWckBusiness userWckBusiness;
	@Resource
	protected ISysConfig iSysConfig;
	@Resource
	private UserAddressMapper userAddressMapper;
	@Resource
	private JumioApi jumioApi;
	@Resource
	private UserCommonBusiness userCommonBusiness;
	@Resource
	private UserAddressBusiness userAddressBusiness;
	@Resource(name = "S3ObjectWithSSEService")
	private S3ObjectService s3ObjectService;
	@Resource
	private IFace iFace;
	@Resource
	private IWithdrawSecurityFace iWithdrawSecurityFace;
	@Resource
	private IUserFace iUserFace;
	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	@Resource
	private CountryMapper countryMapper;
	@Resource
	public ICountry iCountry;
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;

	@Resource
	private KycAduitContextBusiness kycAduitContextBusiness;

	@Resource
	private KycFillInfoMapper kycFillInfoMapper;
	@Resource
	private CertificateHelper certificateHelper;

	@Value("${TARGET_EXCHANGE:}")
	private String targetExchange;

	@Override
	public UserKycCountryResponse getKycCountry(Long userId) throws Exception {
		if (userId == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		return UserKycHelper.loadUserKycInfo(userId);
	}

	@Override
	public APIResponse<UserKycApproveVo> getApproveUser(APIRequest<UserIdRequest> request) {
		UserKycApprove userKyc = userKycApproveMapper.selectByPrimaryKey(request.getBody().getUserId());
		if (userKyc != null) {
			UserKycApproveVo userKycVo = new UserKycApproveVo();
			userKycVo.setBaseInfo(new UserKycApproveVo.BaseInfo());
			userKycVo.setCheckInfo(new UserKycApproveVo.CheckInfo());
			BeanUtils.copyProperties(userKyc, userKycVo);
			if (userKyc.getBaseInfo() != null) {
				BeanUtils.copyProperties(userKyc.getBaseInfo(), userKycVo.getBaseInfo());
			}
			Jumio jumio = jumioMapper.selectByPrimaryKey(userKyc.getUserId(), userKyc.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, userKycVo.getCheckInfo());
				generateKycImageUrls(userKycVo.getCheckInfo());
			}
			return APIResponse.getOKJsonResult(userKycVo);
		}
		return APIResponse.getOKJsonResult(null);
	}

	@Override
	public APIResponse<Boolean> checkUserWhetherPassKyc(APIRequest<UserIdRequest> request) throws Exception {
		UserKycApprove userKyc = userKycApproveMapper.selectByPrimaryKey(request.getBody().getUserId());
		if (userKyc != null) {
			return APIResponse.getOKJsonResult(true);
		} else {
			return APIResponse.getOKJsonResult(false);
		}
	}

	@Override
	public APIResponse<?> updateKycApprove(APIRequest<UpdateKycApproveRequest> request) throws Exception {
		UpdateKycApproveRequest requestBody = request.getBody();
		UserKycApprove kycApprove = new UserKycApprove();
		UserKycApprove.BaseInfo baseInfo = new UserKycApprove.BaseInfo();
		baseInfo.setFirstName(requestBody.getFirstName());
		baseInfo.setLastName(requestBody.getLastName());
		baseInfo.setCountry(requestBody.getCountry());
		kycApprove.setUserId(requestBody.getUserId());
		kycApprove.setBaseInfo(baseInfo);
		int rows = this.userKycApproveMapper.updateSelective(kycApprove);
		if (rows > 0) {
			return APIResponse.getOKJsonResult(null);
		} else {
			return APIResponse.getErrorJsonResult("保存失败！");
		}
	}

	@Override
	public void saveApproveResult(UserKycAuditContext auditContext) {
		UserCertificateIndex certificateIndex = new UserCertificateIndex();
		certificateIndex.setUserId(auditContext.getUserId());
		certificateIndex.setNumber(auditContext.getNumber());
		certificateIndex.setCountry(auditContext.getCountry());
		certificateIndex.setType(auditContext.getDocumentType());
		certificateIndex.setCertificateType(KycCertificateResult.TYPE_USER);
		certificateIndex.setCreateTime(DateUtils.getNewUTCDate());
		UserKyc userKyc = auditContext.getUserKyc();
		Long userId = userKyc.getUserId();
		Long kycId = userKyc.getId();
		int certificateIndexRow = this.userCertificateIndexMapper.insertIgnore(certificateIndex);
		log.info("更新保存用户的证件类型信息：userId:{} kycId:{}, certificateIndexRow:{}, isForbidCountry:{}", userId, kycId,
				certificateIndexRow, auditContext.isForbidCountry());

		if (!auditContext.isForbidCountry()) {
			// 先覆盖输入的名字
			replaceKycUserName(auditContext);
			int kycApproveRow = userKycApproveMapper.insert(UserKycApprove.toKycApprove(userKyc));
			log.info("更新保存用户的KYC认证通过信息：userId:{} kycId:{}, kycApproveRow:{}", userId, kycId, kycApproveRow);

			boolean inWhitelist = userAddressBusiness.checkCountryWhitelist(userKyc, auditContext.getCountry());

			userAddressBusiness.updateWaitingToPending(userId);

			int userCertificateStatusRow = iUserCertificate.updateCertificateStatus(userId, true);
			log.info("更新保存用户的KYC认证状态：userId:{} kycId:{}, userCertificateStatusRow:{}", userId, kycId,
					userCertificateStatusRow);
			int securityLevelRow = this.updateUserSecLevel(userId, inWhitelist);
			log.info("更新保存用户的安全等级: userId:{} kycId:{}, securityLevelRow:{}", userId, kycId, securityLevelRow);
		}
		String kycEmailTemplate = auditContext.isForbidCountry()
				? AccountConstants.USER_KYC_PASS_FORBID_COUNTRY_EMAIL_TEMPLATE
				: Constant.JUMIO_KYC_CHECK_SUCCESS;
		String reason = auditContext.isForbidCountry() ? JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE : null;
		userCommonBusiness.sendJumioCheckEmail(userId, auditContext.getCountry(), reason, kycEmailTemplate,
				MSG_KYC_EMAIL_SEND);

		// 清除下kyc信息的缓存
		UserKycHelper.clearKycCountryCache(userId);

		if (!auditContext.isOcrFlow()) {
			// 同步业务状态到INSPECTOR 的JUMIO 数据
			jumioBusiness.syncJumioBizStatus(userId, auditContext.getScanReference(), JumioBizStatus.PASSED);
		}
		// 把人脸识别的检查照片同步到业务正常通过的对比照片信息中
		boolean saveRefImage = iFace.saveFaceReferenceRefImage(userKyc.getUserId());
		if (saveRefImage) {
			log.info("更新人脸识别正常通过的对比照片信息成功, 触发检测是否需要提币人脸流程: userId:{} kycId:{}", userId, kycId);
			iUserCertificate.kycPassCheckSecurityFaceCheck(userId, kycId.toString(), FaceTransType.KYC_USER,
					userKyc.getTransFaceLogId(),
					auditContext.isForbidCountry() ? KycCertificateStatus.FORBID_PASS.name()
							: KycCertificateStatus.PASS.name(),
					DateUtils.getNewUTCDate());
		}
		iUserFace.endTransFaceLogStatus(userId, userKyc.getId().toString(), FaceTransType.KYC_USER,
				TransFaceLogStatus.PASSED, "个人认证审核通过");
		// 如果是中国用户，开通c2c账户信息
		if (apolloCommonConfig.isKycPassCreateFiatAccount()
				// && "CN".equalsIgnoreCase(auditContext.getCountry())
				&& IdCardOcrStatus.PASS.equals(auditContext.getOcrStatus())) {
			UserKycHelper.createFiatAccount(userId, true);
		}
	}

	private void replaceKycUserName(UserKycAuditContext auditContext) {
		// 如果是中国证件号且OCR通过，需要把姓名覆盖掉用户输入掉姓名，用户c2c校验时必须使用正确的姓名
		if ("CN".equalsIgnoreCase(auditContext.getCountry()) && !auditContext.isOcrFlow()) {
			// ocr flow的流程在ocr通过的时候已经覆盖
			FaceIdCardOcrVo vo = iFace.getFaceIdCardOcr(auditContext.getUserId());
			if (vo != null && IdCardOcrStatus.PASS.equals(vo.getStatus()) && StringUtils.isNotBlank(vo.getName())) {
				UserKyc userKyc = auditContext.getUserKyc();
				if (userKyc.getBaseInfo() != null) {
					log.info("用户属于中国用户且ocr已经通过，覆盖用户输入的姓名, userId:{}", auditContext.getUserId());
					userKyc.getBaseInfo().setFirstName(vo.getName());
					userKyc.getBaseInfo().setLastName(null);
					userKyc.getBaseInfo().setMiddleName(null);
					userKycMapper.updateFillName(userKyc);
				}
			}
		}
	}

	@Override
	public APIResponse<SearchResult<UserKycApproveVo>> getApproveList(APIRequest<JumioQuery> request) {
		SearchResult<UserKycApproveVo> searchResult = new SearchResult<>();
		JumioQuery userKycQuery = request.getBody();
		if (StringUtils.isNotBlank(userKycQuery.getEmail())) {
			User user = userMapper.queryByEmail(userKycQuery.getEmail());
			if (user != null) {
				userKycQuery.setUserId(user.getUserId());
			} else {
				return APIResponse.getOKJsonResult(searchResult);
			}
		}

		List<UserKycApproveVo> userKycVos = new ArrayList<>();
		List<UserKycApprove> kycList = userKycApproveMapper.getList(userKycQuery);

		// 如果kycList的值是空列表，不需要再次查询和组装信息，直接返回空列表
		if (kycList == null || kycList.isEmpty()) {
			searchResult.setRows(userKycVos);
			searchResult.setTotal(0);
			return APIResponse.getOKJsonResult(searchResult);
		}

		// 获得UserId <-> Email 映射
		Set<Long> userIds = kycList.stream().map(UserKycApprove::getUserId).collect(Collectors.toSet());
		List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
		Map<Long, String> userEmailMapping = userIndices.stream()
				.collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));

		for (UserKycApprove userKyc : kycList) {

			UserKycApproveVo userKycVo = new UserKycApproveVo();
			userKycVo.setEmail(userEmailMapping.get(userKyc.getUserId()));
			userKycVo.setBaseInfo(new UserKycApproveVo.BaseInfo());
			userKycVo.setCheckInfo(new UserKycApproveVo.CheckInfo());
			BeanUtils.copyProperties(userKyc, userKycVo);
			if (userKyc.getBaseInfo() != null) {
				BeanUtils.copyProperties(userKyc.getBaseInfo(), userKycVo.getBaseInfo());
			}

			Jumio jumio = jumioMapper.selectByPrimaryKey(userKyc.getUserId(), userKyc.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, userKycVo.getCheckInfo());
				generateKycImageUrls(userKycVo.getCheckInfo());
			}
			userKycVos.add(userKycVo);
		}

		searchResult.setRows(userKycVos);
		searchResult.setTotal(userKycApproveMapper.getListCount(userKycQuery));
		return APIResponse.getOKJsonResult(searchResult);
	}

	@Override
	public APIResponse<JumioTokenResponse> submitBaseInfo(@RequestBody() APIRequest<KycBaseInfoRequest> request) {
		KycBaseInfoRequest requestBody = request.getBody();
		UserKyc.BaseInfo baseInfo = null;
		if (null != requestBody.getBaseInfo()) {
			baseInfo = new UserKyc.BaseInfo();
			BeanUtils.copyProperties(requestBody.getBaseInfo(), baseInfo);
		}
		// UG用，姓名不以前端提交为准，以basic提交信息为准
		if ("UG".equalsIgnoreCase(targetExchange)) {
			// 查询basic kyc info
			AccUserKycExample example = new AccUserKycExample();
			example.createCriteria().andUserIdEqualTo(request.getBody().getUserId());
			example.setOrderByClause("create_time desc");
			List<AccUserKyc> accUserKycList = accUserKycMapper.selectByExample(example);
			if (CollectionUtils.isEmpty(accUserKycList)) {
				log.error("用户{}缺失basic kyc信息", request.getBody().getUserId());
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}
			baseInfo.setFirstName(accUserKycList.get(0).getFillFirstName());
			baseInfo.setMiddleName(accUserKycList.get(0).getFillMiddleName());
			baseInfo.setLastName(accUserKycList.get(0).getFillLastName());
			baseInfo.setNationality(accUserKycList.get(0).getNationality());
		}
		// 参数检查
		if (!UserKyc.validateBaseInfo(baseInfo)) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}

		Long userId = requestBody.getUserId();
		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_INIT_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					JumioTokenResponse response = initUserKycHandler(userId, baseInfo, false, new JumioTokenResponse());
					return APIResponse.getOKJsonResult(response);
				} finally {
					lock.unlock();
				}
			} else {
				log.info("init web user kyc get lock fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			// do nothing
			log.info("kyc lock InterruptedException. userId:{}", userId);
		}
		throw new BusinessException(GeneralCode.SYS_ERROR);
	}

	/**
	 * 注意T 必须输入且不为null， 并且只能是两种类JumioTokenResponse/InitSdkUserKycResponse
	 *
	 * @param userId
	 * @param baseInfo
	 * @param isSdk
	 * @param response
	 * @param <T>
	 * @return
	 */
	private <T> T initUserKycHandler(Long userId, UserKyc.BaseInfo baseInfo, boolean isSdk, T response) {
		if (response == null
				|| !(response instanceof InitSdkUserKycResponse || response instanceof JumioTokenResponse)) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 验证country输入是否正确，
		Country country = iCountry.getCountryByCode(baseInfo.getCountry());
		if (country == null) {
			log.warn("init sdk user kyc get country code fail. userId:{} countryCode:{}", userId,
					baseInfo.getCountry());
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		log.info("INIT_USER_KYC==> 开始初始化个人KYC认证：userId:{} isSdk:{}", userId, isSdk);
		// 没有做过Google 2FA或者SMS
		UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
		if (apolloCommonConfig.isKycNeed2faSwitch()) {
			if (userSecurity == null || StringUtils.isAllBlank(userSecurity.getAuthKey(), userSecurity.getMobile())) {
				throw new BusinessException(GeneralCode.USER_SERCURITY_NOT_BIND);
			}
		}
		// 是否已经通过KYC
		UserKyc userKyc = userKycMapper.getLast(userId);
		// 获取用户当前的KYC认证状态,包含正在进行的个人认证和企业认证
		validateHadReviewKyc(userId, userKyc);
		// 检查用户当天操作的KYC次数是否达到限制，如果达到，则不能再次初始化
		if (jumioBusiness.getDailyJumioTimes(userId,
				com.binance.inspector.common.enums.JumioHandlerType.USER_KYC) >= MAX_UPLOAD_TIMES) {
			log.info("INIT_USER_KYC==> 用户KYC 验证24小时内达到限制次数, userId:{}", userId);
			throw new BusinessException(GeneralCode.USER_KYC_UPLOAD_EXCEED_LIMIT_TODAY);
		}
		if (isSdk) {
			builderSdkUserKycResult(userId, baseInfo, userKyc, userSecurity, (InitSdkUserKycResponse) response);
			return response;
		} else {
			builderWebUserKycResult(userId, baseInfo, userKyc, userSecurity, (JumioTokenResponse) response);
			return response;
		}
	}

	public void validateHadReviewKyc(Long userId, UserKyc lastUserKyc) {
		if (userKycApproveMapper.selectByPrimaryKey(userId) != null) {
			throw new BusinessException(GeneralCode.USER_KYC_PASSED);
		}

		if (lastUserKyc != null && KycStatus.pending.equals(lastUserKyc.getStatus()) && StringUtils.equalsAny(
				lastUserKyc.getFaceOcrStatus(), IdCardOcrStatus.PASS.name(), IdCardOcrStatus.REVIEW.name())) {
			throw new BusinessException(GeneralCode.USER_KYC_PENDING);
		}

		KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
		if (certificateResult.getCertificateStatus() == null) {
			return;
		}
		log.info("INIT_USER_KYC==> 检查到当前用户存在有KYC认证历史信息：userId:{} type:{} status:{} id:{} isForbidPassed:{}", userId,
				certificateResult.getCertificateType(), certificateResult.getCertificateStatus(),
				certificateResult.getCertificateId(), certificateResult.isForbidPassed());
		// 存在有验证KYC的信息，需要进一笔检验
		if (KycCertificateResult.STATUS_PASS == certificateResult.getCertificateStatus()) {
			// 已经通过KYC认证
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
			if (KycCertificateResult.TYPE_COMPANY == certificateResult.getCertificateType()) {
				// 正在做企业认证的情况下，不能做个人认证
				log.info("当前用户正在做企业认证且正在审核中，不能创建新的个人认证流程. userId:{}", userId);
				throw new BusinessException(GeneralCode.USER_KYC_PENDING);
			} else {
				// 正在做个人认证的情况下，判断最后一笔记录是否与当前验证的记录匹配，如果不匹配，则不能创建
				if (lastUserKyc != null && !KycStatus.pending.equals(lastUserKyc.getStatus())) {
					// KYC 已经不在最初始的待上传状态下，不能再次提交
					throw new BusinessException(GeneralCode.USER_KYC_PENDING);
				}
			}
		}
	}

	private UserKyc generateUserKycRecord(Long userId, UserKyc.BaseInfo baseInfo, UserKyc oldUserKyc,
			UserSecurity userSecurity) {
		UserKyc userKyc = null;
		if (oldUserKyc != null
				&& (oldUserKyc.getStatus() == KycStatus.pending || oldUserKyc.getStatus() == KycStatus.basic)) {
			// 先保存下base info
			userKyc = new UserKyc();
			userKyc.setBaseInfo(baseInfo);
			userKyc.setUserId(userId);
			userKyc.setId(oldUserKyc.getId());
			userKyc.setUpdateTime(DateUtils.getNewUTCDate());
			int result = userKycMapper.updateBasicByPrimaryKey(userKyc);
			if (result > 0 && oldUserKyc.getStatus() == KycStatus.basic) {
				// 修改pending状态
				AccUserKyc accUserKyc = new AccUserKyc();
				accUserKyc.setId(oldUserKyc.getId());
				accUserKyc.setStatus((byte) KycStatus.pending.ordinal());
				accUserKycMapper.updateByPrimaryKeySelective(accUserKyc);
			}
			return userKyc;
		} else {
			// 创建新记录
			// 如果不存在，或者状态已经不是pending, 生成一条新记录
			userKyc = new UserKyc();
			userKyc.setUserId(userId);
			userKyc.setStatus(KycStatus.pending);
			userKyc.setUpdateTime(DateUtils.getNewUTCDate());
			userKyc.setCreateTime(DateUtils.getNewUTCDate());
			userKyc.setBaseInfo(baseInfo);
			if (userSecurity != null && UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO
					.equals(userSecurity.getWithdrawSecurityFaceStatus())) {
				// 如果当前已经开启了提币人脸信息，进行设置最后一笔提币人脸信息的提币标识：transFaceLogId
				TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId,
						FaceTransType.WITHDRAW_FACE.name(), null);
				if (faceLog != null) {
					userKyc.setTransFaceLogId(faceLog.getTransId());
				}
			}
			int row = userKycMapper.saveBaseInfo(userKyc);
			if (row > 0) {
				return userKyc;
			} else {
				log.warn("save user kyc base info fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
		}
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public JumioTokenResponse builderWebUserKycResult(Long userId, UserKyc.BaseInfo baseInfo, UserKyc oldUserKyc,
			UserSecurity userSecurity, JumioTokenResponse response) {
		UserKyc userKyc = generateUserKycRecord(userId, baseInfo, oldUserKyc, userSecurity);
		String kycId = String.valueOf(userKyc.getId());

		if (oldUserKyc != null && oldUserKyc.getStatus() == KycStatus.pending
				&& StringUtils.isNotBlank(oldUserKyc.getJumioId())) {
			// 如果老数据状态是pending，并且已经存在对应的 jumio id 考虑是否为WEB 数据，如果是，直接使用
			Jumio jumio = jumioMapper.selectByPrimaryKey(userId, oldUserKyc.getJumioId());
			if (jumio != null && !StringUtils.equalsIgnoreCase(jumio.getSource(), JumioScanSource.SDK.name())
					&& StringUtils.isNotBlank(jumio.getAuthToken())) {
				response.setAuthorizationToken(jumio.getAuthToken());
				return response;
			}
		}
		Jumio jumio = jumioBusiness.initWebJumio(userId, com.binance.inspector.common.enums.JumioHandlerType.USER_KYC,
				kycId, false);
		log.info("初始化 JUMIO WEB 端jumio 数据成功, userId:{} kycId:{} jumioId:{}", userId, kycId, jumio.getId());
		// 如果正常没有抛错退出，则在user_kyc中保存对应的jumio 关联信息
		userKyc.setJumioId(jumio.getId());
		userKyc.setScanReference(jumio.getScanReference());
		userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
		userKyc.setUpdateTime(DateUtils.getNewUTCDate());
		userKycMapper.saveJumioId(userKyc);
		response.setAuthorizationToken(jumio.getAuthToken());
		return response;
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public InitSdkUserKycResponse builderSdkUserKycResult(Long userId, UserKyc.BaseInfo baseInfo, UserKyc oldUserKyc,
			UserSecurity userSecurity, InitSdkUserKycResponse response) {
		UserKyc userKyc = generateUserKycRecord(userId, baseInfo, oldUserKyc, userSecurity);
		String kycId = String.valueOf(userKyc.getId());
		JumioSdkInitResponse initResponse = jumioBusiness.initSdkJumio(userId,
				com.binance.inspector.common.enums.JumioHandlerType.USER_KYC, kycId, false);
		log.info("初始化 JUMIO SDK 端jumio 数据成功, userId:{} kycId:{} jumioId:{}", userId, kycId, initResponse.getJumioId());
		// 看下是否存在有老的JUMIO初始化的数据，如果有，且是web端，则不能让他再用SDK申请
		if (oldUserKyc != null && oldUserKyc.getStatus() == KycStatus.pending
				&& StringUtils.isNotBlank(oldUserKyc.getJumioId())) {
			Jumio jumio = jumioMapper.selectByPrimaryKey(userId, oldUserKyc.getJumioId());
			if (jumio != null) {
				log.info("存在有上一笔初始化的JUMIO数据，直接进行删除重建: userId:{} kycId:{} jumioId:{}", userId, kycId, jumio.getId());
				jumioMapper.deleteByPrimaryKey(userId, jumio.getId());
				userKyc.setJumioId(null);
				userKyc.setScanReference(null);
			}
		}
		// 如果没有抛出错误退出, 则在user_kyc中保存对应的jumio 关联信息
		userKyc.setJumioId(initResponse.getJumioId());
		// 先与填写自定义的流水号，在上传完成后需要变更到JUMIO的流水号
		userKyc.setScanReference(initResponse.getMerchantReference());
		userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.INIT.name());
		userKyc.setUpdateTime(DateUtils.getNewUTCDate());
		userKycMapper.saveJumioId(userKyc);
		BeanUtils.copyProperties(initResponse, response);
		return response;
	}

	@Override
	public APIResponse<InitSdkUserKycResponse> initSdkUserKyc(APIRequest<KycBaseInfoRequest> request) {
		// 判断开关，当前是否开启了使用SDK端的JUMIO验证
		if (!apolloCommonConfig.isKycSdkEnableSwitch()) {
			// sdk init kyc 开关未开启
			throw new BusinessException(GeneralCode.SYS_MAINTENANCE);
		}
		if (request.getBody() == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		KycBaseInfoRequest baseInfoRequest = request.getBody();
		Long userId = baseInfoRequest.getUserId();
		UserKyc.BaseInfo baseInfo = new UserKyc.BaseInfo();
		BeanUtils.copyProperties(baseInfoRequest.getBaseInfo(), baseInfo);
		// 参数检查
		if (userId == null || !UserKyc.validateBaseInfo(baseInfo)) {
			throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
		}
		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_INIT_LOCK + userId);
		try {
			if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
				try {
					InitSdkUserKycResponse response = initUserKycHandler(userId, baseInfo, true,
							new InitSdkUserKycResponse());
					return APIResponse.getOKJsonResult(response);
				} finally {
					lock.unlock();
				}
			} else {
				log.info("init sdk user kyc lock fail. userId:{}", userId);
				throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
			}
		} catch (InterruptedException e) {
			// do nothing
			log.info("kyc sdk init lock InterruptedException. userId:{}", userId);
		}
		throw new BusinessException(GeneralCode.SYS_ERROR);
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void saveJumioSdkScanRef(SaveJumioSdkScanRefRequest body) {
		JumioHandlerType handlerType = JumioHandlerType.getByCode(body.getType());
		if (handlerType == null || StringUtils.isAnyBlank(body.getMerchantRef(), body.getScanRef(), body.getBizId())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (!JumioHandlerType.USER_KYC.equals(handlerType)) {
			log.warn("当前只能处理USER_KYC的数据. body:{}", JSON.toJSONString(body));
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		Long kycId = Long.valueOf(body.getBizId());
		Long userId = body.getUserId();
		String merchantRef = body.getMerchantRef();
		String scanRef = body.getScanRef();
		UserKyc userKyc = userKycMapper.getById(userId, kycId);
		if (userKyc == null || !StringUtils.equalsIgnoreCase(merchantRef, userKyc.getScanReference())) {
			log.info("当前 USER KYC 状态不在带同步scanRef的状态, userId:{} kycId:{} userKyc.scanRef:{}", userId, kycId,
					userKyc.getScanReference());
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		// 把jumio 表中的数据也查询出来对比
		Jumio jumio = jumioMapper.selectByPrimaryKey(userId, userKyc.getJumioId());
		if (jumio == null || !StringUtils.equalsIgnoreCase(merchantRef, jumio.getMerchantReference())) {
			log.info("获取Kyc jumio关联数据失败. userId:{} kycId:{} jumio.merchantRef:{}", userId, kycId,
					jumio == null ? null : jumio.getMerchantReference());
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		UserKyc temp = new UserKyc();
		temp.setId(userKyc.getId());
		temp.setUserId(userKyc.getUserId());
		temp.setScanReference(scanRef);
		temp.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.UPLOADED.name());
		temp.setUpdateTime(DateUtils.getNewUTCDate());
		userKycMapper.updateByPrimaryKeySelective(temp);

		Jumio jumioTemp = new Jumio();
		jumioTemp.setId(jumio.getId());
		jumioTemp.setUserId(jumio.getUserId());
		jumioTemp.setScanReference(scanRef);
		jumioTemp.setUpdateTime(DateUtils.getNewUTCDate());
		jumioMapper.updateByPrimaryKeySelective(jumio);
		log.info("同步保存 jumio sdk scanReference 成功: userId:{} kycId:{} scanRef:{}", userId, kycId, scanRef);
	}

	@Override
	public APIResponse<SearchResult<UserKycVo>> getList(@RequestBody() APIRequest<JumioQuery> request) {
		SearchResult<UserKycVo> searchResult = new SearchResult<>();
		JumioQuery userKycQuery = request.getBody();
		if (StringUtils.isNotBlank(userKycQuery.getEmail())) {
			User user = userMapper.queryByEmail(userKycQuery.getEmail());
			if (user != null) {
				userKycQuery.setUserId(user.getUserId());
			} else {
				return APIResponse.getOKJsonResult(searchResult);
			}
		}

		List<UserKycVo> userKycVos = new ArrayList<>();
		List<UserKyc> kycList = userKycMapper.getList(userKycQuery);

		// 如果jumioList的值是空列表，不需要再次查询和组装信息，直接返回空列表
		if (kycList == null || kycList.isEmpty()) {
			searchResult.setRows(userKycVos);
			searchResult.setTotal(0);
			return APIResponse.getOKJsonResult(searchResult);
		}

		// 获得UserId <-> Email 映射
		Set<Long> userIds = kycList.stream().map(UserKyc::getUserId).collect(Collectors.toSet());
		List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
		Map<Long, String> userEmailMapping = userIndices.stream()
				.collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));

		for (UserKyc userKyc : kycList) {

			UserKycVo userKycVo = new UserKycVo();
			userKycVo.setEmail(userEmailMapping.get(userKyc.getUserId()));
			userKycVo.setBaseInfo(new UserKycVo.BaseInfo());
			userKycVo.setCheckInfo(new UserKycVo.CheckInfo());
			BeanUtils.copyProperties(userKyc, userKycVo);
			if (userKyc.getBaseInfo() != null) {
				BeanUtils.copyProperties(userKyc.getBaseInfo(), userKycVo.getBaseInfo());
			}
			Jumio jumio = jumioMapper.selectByPrimaryKey(userKyc.getUserId(), userKyc.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, userKycVo.getCheckInfo());
				generateKycImageUrls(userKycVo.getCheckInfo());
			}
			userKycVos.add(userKycVo);
		}

		searchResult.setRows(userKycVos);
		searchResult.setTotal(userKycMapper.getListCount(userKycQuery));
		return APIResponse.getOKJsonResult(searchResult);
	}

	@Nullable
	private String generateImageUrl(String imagePath) {
		if (StringUtils.isBlank(imagePath)) {
			return null;
		}
		try {
			return s3ObjectService.generatePresignedUrl(imagePath, 3600L);
		} catch (Exception e) {
			log.error("failed to generate presigned url for object: {}", imagePath);
			return null;
		}
	}

	public void generateKycImageUrls(UserKycVo.CheckInfo checkInfo) {
		if (checkInfo != null) {
			checkInfo.setBackUrl(generateImageUrl(checkInfo.getBack()));
			checkInfo.setFrontUrl(generateImageUrl(checkInfo.getFront()));
			checkInfo.setFaceUrl(generateImageUrl(checkInfo.getFace()));
		}
	}

	private void generateKycImageUrls(UserKycApproveVo.CheckInfo checkInfo) {
		if (checkInfo != null) {
			checkInfo.setBackUrl(generateImageUrl(checkInfo.getBack()));
			checkInfo.setFrontUrl(generateImageUrl(checkInfo.getFront()));
			checkInfo.setFaceUrl(generateImageUrl(checkInfo.getFace()));
		}
	}

	/**
	 * 强制从主库读取User Kyc 信息
	 *
	 * @param userId
	 * @param id
	 * @return
	 */
	private UserKyc getUserKycByMasterdb(Long userId, Long id) {
		UserKyc userKyc = null;
		if (userId == null || id == null) {
			return null;
		}
		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			userKyc = userKycMapper.getById(userId, id);
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}
		return userKyc;
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public APIResponse<?> audit(@RequestBody() APIRequest<KycAuditRequest> request) {
		KycAuditRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		// KYC内容强制从主库读取，方式出现修改完状态后从库没法及时更新下次请求修改就过来了
		UserKyc userKyc = getUserKycByMasterdb(userId, requestBody.getId());
		if (userKyc == null) {
			return APIResponse.getErrorJsonResult("record not exist");
		}
		UserKycAuditContext auditContext = kycAduitContextBusiness.builder(userKyc);

		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
		auditContext.setUserKycApprove(userKycApprove);
		auditContext.setAuditStatus(requestBody.getStatus());
		auditContext.setFailReason(requestBody.getFailReason());
		auditContext.setNumber(
				StringUtils.isBlank(auditContext.getNumber()) ? requestBody.getNumber() : auditContext.getNumber());
		auditContext.setMemo(requestBody.getMemo());

		// 1.进行数据验证
		ValidateResult ck = this.preCheckForAudit(auditContext);

		if (!ck.isOk()) {
			log.info("人工审核USER KYC 信息验证失败. userId:{} kycId:{} message:{}", userId, userKyc.getId(), ck.getMessage());
			return APIResponse.getErrorJsonResult(ck.getMessage());
		}
		// 2.审核处理
		KycStatus newStatus = auditContext.getAuditStatus();
		switch (newStatus) {
		case jumioRefused:
		case jumioPassed:
		case delete:
			// 重置为待人工确认状态或者删除
			auditKycResetHandler(auditContext);
			break;
		case passed:
			// 审核通过
			auditKycPassHandler(auditContext);
			break;
		case refused:
			// 审核拒绝
			auditKycRefusedHandler(auditContext);
			break;
		case wckPassed:
			// world-check通过，即审核通过
			if (apolloCommonConfig.isKycFaceSwitch()) {
				// 如果开启了人脸识别则，检查当前人脸识别是否已经通过
				if (StringUtils.equalsIgnoreCase(userKyc.getFaceStatus(), FaceStatus.FACE_PASS.name())) {
					userKyc.setStatus(KycStatus.passed);
					this.saveApproveResult(auditContext);
				} else {
					// 需要等待人脸识别
					log.info("wck pass bug face switch open need face pass. userId:{} kycId:{}", userId,
							userKyc.getId());
				}
			} else {
				// 人脸识别开关未开启的情况下，直接通过
				userKyc.setStatus(KycStatus.passed);
				this.saveApproveResult(auditContext);
			}
			break;
		case wckRefused:
			// world-check拒绝，即审核拒绝
			userKyc.setStatus(KycStatus.refused);

			if (!auditContext.isOcrFlow()) {
				// 同步业务状态到INSPECTOR 的JUMIO 数据
				jumioBusiness.syncJumioBizStatus(auditContext.getUserId(), auditContext.getScanReference(),
						JumioBizStatus.REFUSED);
			}
			iUserFace.endTransFaceLogStatus(userId, userKyc.getId().toString(), FaceTransType.KYC_USER,
					TransFaceLogStatus.FAIL, "个人认证WorldCheck审核拒绝");
			break;
		default:
			log.info("审核USER KYC 审核状态错误. userId:{} kycId:{} auditStatus:{}", userId, userKyc.getId(), newStatus);
			return APIResponse.getErrorJsonResult("操作失败！提交的审核状态有误");
		}
		if (KycStatus.delete == newStatus) {
			userKyc.setFailReason(requestBody.getFailReason());
		}
		userKyc.appendMemo(requestBody.getMemo());
		userKyc.setUpdateTime(new Date());
		userKycMapper.updateStatus(userKyc);

		return APIResponse.getOKJsonResult(null);
	}

	/**
	 * 人工审核 USER KYC 重置
	 *
	 */
	private void auditKycResetHandler(UserKycAuditContext auditContext) {
		UserKyc userKyc = auditContext.getUserKyc();
		Long userId = userKyc.getUserId();
		Long kycId = userKyc.getId();
		UserKycApprove kycApprove = auditContext.getUserKycApprove();
		log.info("人工审核 USER KYC 手动重置, userId:{} kycId:{} currentStatus:{} auditStatus:{}", userId, kycId,
				userKyc.getStatus(), auditContext.getAuditStatus());
		if (userKyc.getStatus() == KycStatus.passed) {
			// 原本已通过重置
			if (kycApprove != null) {
				// 删除通过记录表中的记录
				log.info("重置已经通过个人认证的逻辑，删除原有的KYC认证通过信息. userId:{} kycId:{}", userId, kycId);
				userKycApproveMapper.deleteByPrimaryKey(userId);
			}
			// 把原来的证件号通过逻辑删除
			iUserCertificate.removeCertificateIndex(userId, kycId, auditContext.getCountry(), auditContext.getNumber(),
					auditContext.getDocumentType());
			resetUserSecurityLevel(userId, kycId);
			// 清除用户做人脸识别的正式图片地址
			iFace.removeFaceReferenceRefImage(userId);
		} else if (userKyc.getStatus() == KycStatus.forbidPassed) {
			log.info("人工审核 USER KYC 原来的状态是不合规国籍通过状态. userId:{} kycId:{}", userId, kycId);
			// 把原来的证件号通过逻辑删除
			iUserCertificate.removeCertificateIndex(userId, kycId, auditContext.getCountry(), auditContext.getNumber(),
					auditContext.getDocumentType());
			// 清除用户做人脸识别的正式图片地址
			iFace.removeFaceReferenceRefImage(userId);
		} else if (userKyc.getStatus() != KycStatus.refused && KycStatus.delete != auditContext.getAuditStatus()) {
			// 如果 userKyc 为delete 并且 audit status = delete 则不允许操作
			log.info("人工审核 USER KYC 原本的状态不是处于通过或拒绝状态退出. userId:{} kycId:{} status:{}", userId, kycId,
					userKyc.getStatus());
			throw new BusinessException(GeneralCode.SYS_ERROR, MSG_ACTION_STATUS_ERROR);
		}
		if (KycStatus.delete == auditContext.getAuditStatus()) {
			log.info("人工审核 USER KCY 删除， 需要做后续处理. userId:{} kycId:{}", userId, kycId);
			iUserCertificate.deleteKycEndHandler(userId, kycId, auditContext.getScanReference());
		}
		userKyc.setStatus(auditContext.getAuditStatus());
		log.info("人工审核 重置/删除用户KYC认证: userId{} kycId:{}, auditStatus:{}", userId, kycId, auditContext.getAuditStatus());

		// 清除下kyc信息的缓存
		UserKycHelper.clearKycCountryCache(userId);
	}

	private void resetUserSecurityLevel(Long userId, Long kycId) {
		// 修改身份认证通过的状态信息
		int statusRow = iUserCertificate.updateCertificateStatus(userId, false);
		log.info("人工审核KYC, 降级用户的状态：userId:{} kycId:{} statusRow:{}", userId, kycId, statusRow);
		// 如果用户的登记不是1级, 需要修改用户登记
		UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
		if (userSecurity.getSecurityLevel() != 1) {
			log.info("人工审核KYC, 原来状态为通过重置为审核中变更用户等级: userId:{} kycId:{}", userId, kycId);
			iUserCertificate.updateSecurityLevel(userId, 1);
			// 修改用户等级消息通知 start
			Map<String, Object> dataMsg = Maps.newHashMap();
			dataMsg.put("userId", userId);
			dataMsg.put("level", 1);
			MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
			log.info("user kyc reset iMsgNotification security level 1: userId:{} kycId:{} message:{}", userId, kycId,
					JSON.toJSONString(msg));
			this.iMsgNotification.send(msg);
			// 强制修改用户提币额度
			dataMsg.put("withdrawLimit", null);
			MsgNotification msg2 = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.WITHDRAW_LIMIT,
					dataMsg);
			log.info("user kyc reset iMsgNotification set withdraw limit null: userId:{} kycId:{} message:{}", userId,
					kycId, JSON.toJSONString(msg2));
			this.iMsgNotification.send(msg2);
		}
	}

	/**
	 * 人工审核 USER KYC 通过
	 *
	 */
	private void auditKycPassHandler(UserKycAuditContext auditContext) {
		UserKyc userKyc = auditContext.getUserKyc();
		String reqNumber = auditContext.getNumber();
		Long userId = userKyc.getUserId();
		if (StringUtils.isBlank(reqNumber) && !auditContext.isOcrFlow()) {
			// 如果身份证号缺失，进行补全
			Jumio record = new Jumio();
			record.setId(auditContext.getJumioId());
			record.setUserId(userId);
			record.setNumber(reqNumber);
			jumioMapper.updateByPrimaryKeySelective(record);
		}
		Long kycId = userKyc.getId();
		// 若开启了world-check，则必须先完成world-check验证
		if (certificateHelper.isSwitchOn()) {
			log.info("USER KYC 手动通过，已开启wck审核: userId: {} kycId:{}", userId, kycId);
			userKyc.setStatus(KycStatus.wckWaiting);
			userWckBusiness.applyOrResetWorldCheck(auditContext.buildJumio(), userKyc);
		} else {
			log.info("通过用户KYC认证: userId:{} kycId:{} isForbidCountry:{}", userId, kycId, auditContext.isForbidCountry());
			KycStatus kycStatus = auditContext.getAuditStatus();
			String reason = null;
			if (auditContext.isForbidCountry()) {
				kycStatus = KycStatus.forbidPassed;
				reason = JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE;
			}
			userKyc.setStatus(kycStatus);
			userKyc.setFailReason(reason);
			this.saveApproveResult(auditContext);
		}
		log.info("USER KYC 审核通过变更状态信息. userId:{} kycId:{} newStatus:{}", userId, kycId, userKyc.getStatus());
	}

	/**
	 * 人工审核 User KYC 拒绝
	 *
	 */
	private void auditKycRefusedHandler(UserKycAuditContext auditContext) {
		UserKyc userKyc = auditContext.getUserKyc();
		Long userId = userKyc.getUserId();
		Long kycId = userKyc.getId();
		// 拒绝
		if (StringUtils.isNotBlank(auditContext.getFailReason())) {
			// 如果有输入错误原因，进行替换原来的错误原因
			userKyc.setFailReason(auditContext.getFailReason());
		}
		log.info("USER KYC 审核拒绝. userId:{} kycId:{} failReason:{}", userId, kycId, userKyc.getFailReason());
		userKyc.setStatus(auditContext.getAuditStatus());
		userCommonBusiness.sendJumioCheckEmail(userId, auditContext.getCountry(), userKyc.getFailReason(),
				Constant.JUMIO_KYC_CHECK_FAIL, MSG_KYC_EMAIL_SEND);
		if (!auditContext.isOcrFlow()) {
			// 同步业务状态到INSPECTOR 的JUMIO 数据
			jumioBusiness.syncJumioBizStatus(userId, auditContext.getScanReference(), JumioBizStatus.REFUSED);
		}
		log.info("审核拒绝用户KYC认证: userId{} kycId:{}", userId, kycId);
		iUserFace.endTransFaceLogStatus(userId, userKyc.getId().toString(), FaceTransType.KYC_USER,
				TransFaceLogStatus.FAIL, "个人认证审核拒绝");

		if (auditContext.isOcrFlow()) {
			iFace.resetFaceIdOcr(userId);
		}
	}

	/**
	 * check data for audit
	 */
	private ValidateResult preCheckForAudit(UserKycAuditContext auditContext) {
		UserKyc userKyc = auditContext.getUserKyc();

		Long userId = userKyc.getUserId();
		Long kycId = userKyc.getId();
		UserKyc lastUserKyc = userKycMapper.getLast(userId);
		if (lastUserKyc == null || lastUserKyc.getId().longValue() != userKyc.getId().longValue()) {
			log.info("当前审核的USER KYC 记录不是最后一笔申请记录. userId:{} kycId:{}", userId, kycId);
			return ValidateResult.reject("操作失败！请处理最后一条数据");
		}
		if (auditContext.getAuditStatus() == KycStatus.passed || auditContext.getAuditStatus() == KycStatus.wckPassed) {
			// 审核状态为通过时的检验
			return preCheckForAuditPassed(auditContext);
		} else if (auditContext.getAuditStatus() == KycStatus.refused) {
			// 审核状态是拒绝时，需要验证当前状态是否为jumioPassed 或 jumioRefused
			log.info("审核状态为拒绝, 验证当前kyc状态. userId:{} kycId:{} currentStatus:{}", userKyc.getUserId(), userKyc.getId(),
					userKyc.getStatus());
			if (KycStatus.isEndStatus(userKyc.getStatus())) {
				return ValidateResult.reject(MSG_ACTION_STATUS_ERROR);
			}
			if (StringUtils.isAllBlank(userKyc.getFailReason(), auditContext.getFailReason())) {
				log.warn("审核拒绝时必须要拒绝原因. userId:{}", userId);
				return ValidateResult.reject(MSG_ACTION_FAIL_REASO_MISS);
			}

		} else if (auditContext.getAuditStatus() == KycStatus.delete) {
			// 审核状态是删除时，需要先确认当前用户的记录是end的状态且不能是delete
			if (!KycStatus.isEndStatus(userKyc.getStatus())) {
				log.info("审核状态为拒绝是，当前认证状态必须是结束状态且不为delete.");
				return ValidateResult.reject(MSG_ACTION_STATUS_ERROR);
			}
		}
		return ValidateResult.pass();
	}

	/**
	 * KYC 审核通过时的检验信息
	 *
	 * @return
	 */
	private ValidateResult preCheckForAuditPassed(UserKycAuditContext auditContext) {
		UserKyc userKyc = auditContext.getUserKyc();
		Long userId = userKyc.getUserId();
		Long kycId = userKyc.getId();
		String auditNumber = auditContext.getNumber();

		if (KycStatus.isEndStatus(userKyc.getStatus())) {
			return ValidateResult.reject(MSG_ACTION_STATUS_ERROR);
		}

		UserKycApprove userKycApprove = auditContext.getUserKycApprove();
		if (userKycApprove != null) {
			log.info("当前用户的身份认证已经通过, 不能再次操作通过. userId:{} kycId:{}", userId, kycId);
			return ValidateResult.reject("操作失败！当前用户已经通过身份认证");
		}
		if (StringUtils.isBlank(auditNumber)) {
			log.info("审核的证件号信息缺失，需要补充, userId:{} kycId:{}", userId, kycId);
			return ValidateResult.reject("操作失败！请先补全证件号码");
		}
		if (StringUtils.isAllBlank(auditContext.getFirstName(), auditContext.getLastName())) {
			log.info("审核的记录姓名信息缺失，请直接拒绝，userId:{} kycId:{}", userId, kycId);
			return ValidateResult.reject("操作失败！当前记录缺失审核信息（姓名），请直接拒绝");
		}
		if (StringUtils.isBlank(auditContext.getCountry())) {
			log.info("当前JUMIO信息的国家码缺失. userId:{} kycId:{}", userId, kycId);
			return ValidateResult.reject("操作失败！当前记录缺失审核信息（发行国家），请直接拒绝");
		}
		if (iUserCertificate.isIDNumberOccupied(auditNumber, auditContext.getCountry(), auditContext.getDocumentType(),
				userId)) {
			log.info("通过用户KYC认证失败，证件号已被使用: userId{}, kycId:{} number:{}", userId, kycId, auditNumber);
			return ValidateResult.reject("操作失败！当前证件号已被使用");
		}

		return ValidateResult.pass();
	}

	@Override
	public APIResponse<?> syncPhoto(APIRequest<JumioQuery> request) {
		JumioQuery requestBody = request.getBody();
		Jumio jumio = jumioMapper.selectByPrimaryKey(requestBody.getUserId(), requestBody.getId());
		if (jumio != null) {
			log.info("同步JUMIO照片, userId:{} scanReference:{}", jumio.getUserId(), jumio.getScanReference());
			JumioBaseRequest baseRequest = new JumioBaseRequest();
			baseRequest.setUserId(jumio.getUserId());
			baseRequest.setScanReference(jumio.getScanReference());
			baseRequest.setTypeCode(jumio.getType().name());
			APIResponse<JumioPhotoResponse> response = jumioApi.syncPhoto(APIRequest.instance(baseRequest));
			log.info("同步JUMIO照片结果信息：userId:{} scanReference:{} result:{}", jumio.getUserId(), jumio.getScanReference(),
					JSON.toJSONString(response));
			if (response.getStatus() != APIResponse.Status.OK || response.getData() == null) {
				return APIResponse.getErrorJsonResult("同步失败！");
			}
			// 如果获取成功的话,把数据更新到数据库中
			JumioPhotoResponse photoResponse = response.getData();
			String front = photoResponse.getFront();
			String back = photoResponse.getBack();
			String face = photoResponse.getFace();
			if (!StringUtils.isAllBlank(front, back, face)) {
				log.info("更新保存JUMIO 照片路径信息. userId:{} scanReference:{} ", jumio.getUserId(), jumio.getScanReference());
				Jumio record = new Jumio();
				record.setId(jumio.getId());
				record.setUserId(jumio.getUserId());
				record.setUpdateTime(DateUtils.getNewUTCDate());
				record.setFront(front);
				record.setBack(back);
				record.setFace(face);
				jumioMapper.updateByPrimaryKeySelective(jumio);
			}
			return APIResponse.getOKJsonResult(null);
		} else {
			return APIResponse.getErrorJsonResult("同步失败！");
		}
	}

	private UserKycVo buildByUserKyc(UserKyc userKyc) {
		UserKycVo userKycVo = new UserKycVo();
		userKycVo.setBaseInfo(new UserKycVo.BaseInfo());
		userKycVo.setCheckInfo(new UserKycVo.CheckInfo());
		if (userKyc != null && KycStatus.delete != userKyc.getStatus()) {
			BeanUtils.copyProperties(userKyc, userKycVo);
			if (userKyc.getBaseInfo() != null) {
				BeanUtils.copyProperties(userKyc.getBaseInfo(), userKycVo.getBaseInfo());
			}

			Jumio jumio = jumioMapper.selectByPrimaryKey(userKyc.getUserId(), userKyc.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, userKycVo.getCheckInfo());
				generateKycImageUrls(userKycVo.getCheckInfo());
			}
		}
		return userKycVo;
	}

	@Override
	public APIResponse<UserKycVo> getKycByUserId(APIRequest<UserIdRequest> request) {
		UserIdRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		UserKyc userKyc = userKycMapper.getLast(userId);
		return APIResponse.getOKJsonResult(buildByUserKyc(userKyc));
	}

	@Override
	public APIResponse<UserKycVo> getUserKycById(APIRequest<UserIdAndIdRequest> request) {
		UserIdAndIdRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		UserKyc userKyc = userKycMapper.getById(userId, requestBody.getId());
		return APIResponse.getOKJsonResult(buildByUserKyc(userKyc));
	}

	public int updateUserSecLevel(Long userId, boolean countryInWhitelist) {
		SysConfig addressVerificationConfig = this.iSysConfig.selectByDisplayName("address_verification_switch");
		// 法币这里需要地址认证通过才能提升用户等级，主站不需要
		int row = 0;
		if (addressVerificationConfig != null && "ON".equalsIgnoreCase(addressVerificationConfig.getCode())) {
			// 由于数据库主从延迟的原因，有可能这边查不到刚插入的数据，特意添加countryInWhitelist
			UserAddress userAddressPass = this.userAddressMapper.getLast(userId, UserAddress.Status.PASSED.ordinal());
			if (null != userAddressPass || countryInWhitelist) {
				row = this.updateAndSendMsg(userId);
			}
		} else {
			row = this.updateAndSendMsg(userId);
		}
		return row;
	}

	private int updateAndSendMsg(Long userId) {
		int row = iUserCertificate.updateSecurityLevel(userId, 2);
		// 修改用户等级消息通知 start
		Map<String, Object> dataMsg = new HashMap<>();
		dataMsg.put("userId", userId);
		dataMsg.put("level", 2);
		MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
		log.info("iMsgNotification security level 2:{}", JSON.toJSONString(msg));
		this.iMsgNotification.send(msg);
		return row;
	}

	/**
	 * 验证和自动通过个人认证
	 *
	 * @param kycId
	 * @param userId
	 */
	@Override
	public void syncUserKycCanAutoPass(final Long kycId, final Long userId) {
		// KYC jumio自动通过开关
		boolean jumioSwitch = jumioBusiness.isDirectPassSwitchOn();
		// FACE ocr自动通过开关
		boolean faceOcrSwitch = apolloCommonConfig.isFacePassOcrSwtich();

		// KYC 是否需人脸识别开关
		boolean faceSwitch = apolloCommonConfig.isKycFaceSwitch();
		boolean jumioSdkAutoPassSwitch = apolloCommonConfig.isKycSdkAutoPassSwitch();
		log.info("开始检查和处理个人认证自动通过的审核逻辑. userId:{} kycId:{} jumioSwitch:{} faceSwitch:{} jumioSdkAutoPassSwitch:{}",
				userId, kycId, jumioSwitch, faceSwitch, jumioSdkAutoPassSwitch);

		// 启动一个新的线程来处理
		final String track = TrackingUtils.getTrackingChain();
		AsyncTaskExecutor.execute(() -> {
			TrackingUtils.putTracking(track);
			try {
				// 先等待5秒中再处理 强制从主库查询
				Thread.sleep(5000);
				UserKyc userKyc = getUserKycByMasterdb(userId, kycId);
				if (userKyc == null || KycStatus.isEndStatus(userKyc.getStatus())) {
					log.warn("获取不到个人认证记录信息或者状态已经终态. userId:{} kycId:{}", userId, kycId);
					return;
				}
				if (!jumioSwitch && !userKyc.isOcrFlow()) {
					// 如果jumio 自动通过的开关关闭的，则不能自动通过
					return;
				}
				if (!faceOcrSwitch && userKyc.isOcrFlow()) {
					return;
				}

				UserKycAuditContext auditContext = kycAduitContextBusiness.builder(userKyc);

				if (auditContext == null) {
					log.warn("获取不到{}审核信信息. userId:{} kycId:{}, jumioId:{}", userKyc.isOcrFlow() ? "FACE_OCR" : "JUMIO",
							userId, kycId, userKyc.getJumioId());
					return;
				}
				// 看看JUMIO是否已经通过，
				if (!auditContext.isOcrFlow() && !com.binance.inspector.common.enums.JumioStatus.PASSED
						.equals(auditContext.getJumioStatus())) {
					// jumio 未通过，直接退出
					log.info("当前个人认证记录的状态不处于JUMIO审核通过的状态，userId:{} kycId:{} status:{} jumioStatus:{}", userId, kycId,
							userKyc.getStatus(), auditContext.getJumioStatus());
					return;
				}
				// ocr 逻辑
				if (auditContext.isOcrFlow() && !IdCardOcrStatus.PASS.equals(auditContext.getOcrStatus())) {
					log.info("当前个人认证记录的状态不处于FACE_OCR审核通过的状态，userId:{} kycId:{} status:{} ocrStatus:{}", userId, kycId,
							userKyc.getStatus(), auditContext.getOcrStatus());
					return;
				}

				// 验证下证件号是否被别人占用了，如果占用了，直接拒绝
				if (iUserCertificate.isIDNumberOccupied(auditContext.getNumber(), auditContext.getCountry(),
						auditContext.getDocumentType(), userId)) {
					log.info("证件号已经被别的用户使用了，不能再次使用. userId:{} kycId:{}", userId, kycId);
					auditContext.setAuditStatus(KycStatus.refused);
					auditContext.setFailReason(JumioError.ID_NUMBER_USED.name());
					auditKycRefusedHandler(auditContext);
					userKyc.setUpdateTime(new Date());
					userKycMapper.updateStatus(userKyc);
					return;
				}

				// 如果是SDK的jumio认证，可以直接通过不需要人脸识别
				if (!auditContext.isOcrFlow() && jumioSdkAutoPassSwitch
						&& JumioScanSource.SDK.name().equalsIgnoreCase(auditContext.getJumioSource())) {
					log.info("个人认证的JUMIO是通过SDK端做的认证，不需要经过人脸识别直接通过，userId:{} kycId:{}", userId, kycId);
					this.autoPassUserKyc(auditContext);
					return;
				}

				// 如果不是sdk, 如果JUMIO/FACE_OCR审核通过了，看看是否开启了人脸识别的开关，如果开启了，验证下人脸识别是否已经通过
				if (faceSwitch && StringUtils.equalsIgnoreCase(FaceStatus.FACE_PASS.name(), userKyc.getFaceStatus())) {
					log.info("个人认证的人脸识别已经通过，可以进行自动审核通过: userId:{} kycId:{}", userId, kycId);
					this.autoPassUserKyc(auditContext);
				} else if (!faceSwitch) {
					// 如果当前不需要做人脸识别，则直接通过
					log.info("个人认证未开启人脸识别流程，可以直接通过. userId:{} kycId:{}", userId, kycId);
					this.autoPassUserKyc(auditContext);
				} else {
					log.info("个人认证JUMIO通过但是人脸识别未通过，当前不能自动通过. userId:{} kycId:{}", userId, kycId);
				}
			} catch (Exception e) {
				log.error("处理个人认证自动通过逻辑异常. userId:{} kycId:{}", userId, kycId, e);
			} finally {
				TrackingUtils.removeTracking();
			}
		});
	}

	private void autoPassUserKyc(UserKycAuditContext auditContext) {
		UserKyc userKyc = auditContext.getUserKyc();
		boolean isForbidCountry = apolloCommonConfig.isKycPassForbidCountry(auditContext.getCountry());
		auditContext.setForbidCountry(isForbidCountry);
		log.info("user kyc auto pass. userId:{} kycId:{} isForbidCountry:{}", userKyc.getUserId(), userKyc.getId(),
				isForbidCountry);
		if (isForbidCountry) {
			userKyc.setStatus(KycStatus.forbidPassed);
			userKyc.setFailReason(JumioConst.KYC_PASS_FORBID_COUNTRY_MESSAGE);
		} else {
			userKyc.setStatus(KycStatus.passed);
			userKyc.setFailReason(null);
		}
		UserKyc updateStatus = new UserKyc();
		updateStatus.setId(userKyc.getId());
		updateStatus.setUserId(userKyc.getUserId());
		updateStatus.setStatus(userKyc.getStatus());
		updateStatus.setFailReason(userKyc.getFailReason());
		updateStatus.setUpdateTime(DateUtils.getNewUTCDate());
		userKycMapper.updateStatus(updateStatus);

		this.saveApproveResult(auditContext);
	}

	/**
	 * Jumio 验证结果出来后的处理逻辑
	 *
	 * @param jumio
	 * @param jumioPass
	 * @param msg
	 */
	@Override
	public void handleUserKyc(Jumio jumio, boolean jumioPass, String msg) {
		UserKyc userKyc = iUserCertificate.getUserKycFromMasterDbByJumioId(jumio.getUserId(), jumio.getId());
		if (userKyc == null || KycStatus.isEndStatus(userKyc.getStatus())) {
			log.info("个人认证流程已经进入终态，不能再变更. userId:{} jumioId:{}", jumio.getUserId(), jumio.getId());
			return;
		}
		// world check开关
		boolean wckSwitch = certificateHelper.isSwitchOn();
		log.info("个人认证JUMIO有结果后的处理逻辑, userId:{} id:{} jumioPass:{}", userKyc.getUserId(), userKyc.getId(), jumioPass);
		if (wckSwitch && jumioPass) {
			userKyc.setStatus(KycStatus.wckWaiting);
		} else {
			if (jumioPass) {
				userKyc.setStatus(KycStatus.jumioPassed);
			} else {
				// jumio 拒绝时，业务直接进入拒绝状态
				userKyc.setStatus(KycStatus.refused);
			}
		}
		UserKyc record = new UserKyc();
		record.setId(userKyc.getId());
		record.setUserId(userKyc.getUserId());
		record.setStatus(userKyc.getStatus());
		record.setFailReason(msg);
		if (jumioPass) {
			record.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.PASSED.name());
		} else {
			record.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.REFUED.name());
		}
		record.setUpdateTime(DateUtils.getNewUTCDate());
		int rows = userKycMapper.updateStatus(record);
		if (rows <= 0) {
			log.info("保存修改KYC数据失败. userId:{}", userKyc.getUserId());
			return;
		}
		// 如果已经拒绝的状态，直接发送邮件提示流程结束
		if (userKyc.getStatus() == KycStatus.refused) {
			log.info("jumio返回验证结果，拒绝，发送拒绝邮件和通知变更业务状态完结: {}", jumio.getUserId());
			userCommonBusiness.sendJumioCheckEmail(jumio.getUserId(), jumio.getIssuingCountry(), msg,
					Constant.JUMIO_KYC_CHECK_FAIL, MSG_KYC_EMAIL_SEND);
			// 同步业务状态到 INSPECTOR JUMIO
			jumioBusiness.syncJumioBizStatus(jumio.getUserId(), jumio.getScanReference(), JumioBizStatus.REFUSED);
			// 同步人脸识别状态信息
			iUserFace.endTransFaceLogStatus(userKyc.getUserId(), userKyc.getId().toString(), FaceTransType.KYC_USER,
					TransFaceLogStatus.FAIL, "JUMIO拒绝自动结束流程");
		} else {
			log.info("jumio返回验证结果，通过: userId:{} kycId:{} wckSwitch:{}", userKyc.getUserId(), userKyc.getId(),
					wckSwitch);
			if (wckSwitch) {
				// 发送world check审核申请
				userWckBusiness.applyWorldCheck(jumio, userKyc);
			} else {
				// 如果没开wck，需要检查当前的KYC认证状态是否能自动通过
				this.syncUserKycCanAutoPass(userKyc.getId(), userKyc.getUserId());
			}
		}
	}

	@Override
	public SearchResult<UserKycVo> getModularUserKycList(UserKycModularQuery query) {
		// 个人认证 模块 的列表查询
		SearchResult<UserKycVo> result = new SearchResult<>();
		if (query == null) {
			return new SearchResult<>(Collections.emptyList(), 0);
		}
		// 如果email 不等于null 反查userId
		if (StringUtils.isNotBlank(query.getEmail())) {
			User user = userMapper.queryByEmail(query.getEmail());
			if (user == null) {
				log.info("获取用户信息失败. email:{}", query.getEmail());
				return new SearchResult<>(Collections.emptyList(), 0);
			} else {
				query.setUserId(user.getUserId());
			}
		}
		List<UserKyc> list = userKycMapper.getModularUserKycList(query);
		if (list == null || list.isEmpty()) {
			return new SearchResult<>(Collections.emptyList(), 0);
		}
		// 如果有数据，设置对应的Vo
		List<UserKycVo> vos = list.stream().map(item -> {
			UserKycVo vo = new UserKycVo();
			vo.setBaseInfo(new UserKycVo.BaseInfo());
			BeanUtils.copyProperties(item, vo);
			if (item.getBaseInfo() != null) {
				BeanUtils.copyProperties(item.getBaseInfo(), vo.getBaseInfo());
			}

			vo.setCheckInfo(new UserKycVo.CheckInfo());
			Jumio jumio = jumioMapper.selectByPrimaryKey(item.getUserId(), item.getJumioId());
			if (jumio != null) {
				BeanUtils.copyProperties(jumio, vo.getCheckInfo());
				// 暂时不需要图片链接信息
				// generateKycImageUrls(vo.getCheckInfo());
			}
			if (StringUtils.isNotBlank(item.getFailReason())) {
				JumioError jumioError = JumioError.getByName(item.getFailReason());
				if (jumioError == null) {
					vo.setFailReasonDesc(item.getFailReason());
					vo.setFailReasonCn(item.getFailReason());
					vo.setFailReasonEn(item.getFailReason());
				} else {
					vo.setFailReasonDesc(jumioError.getMessage() + "(" + jumioError.name() + ")");
					vo.setFailReasonCn(jumioError.getCnDesc());
					vo.setFailReasonEn(jumioError.getEnDesc());
				}
			}
			return vo;
		}).collect(Collectors.toList());
		// 查询对应的World Check Status
		List<Long> ids = vos.stream().map(UserKycVo::getId).collect(Collectors.toList());
		Map<Long, UserWckAudit> wckAuditMap = userWckBusiness.getByKycIds(ids);
		if (wckAuditMap != null && !wckAuditMap.isEmpty()) {
			vos.stream().forEach(item -> {
				UserWckAudit audit = wckAuditMap.get(item.getId());
				if (audit != null && audit.getStatus() != null) {
					item.setWckStatus(audit.getStatus());
				}
			});
		}
		// 如果查询中有email, 直接设置，如果没有，查询所有用户的email 进行设置
		if (StringUtils.isNotBlank(query.getEmail())) {
			vos.stream().forEach(item -> item.setEmail(query.getEmail()));
		} else {
			Set<Long> userIds = vos.stream().map(UserKycVo::getUserId).collect(Collectors.toSet());
			List<UserIndex> indexList = userIndexMapper.selectByUserIds(userIds);
			final Map<Long, String> emailMap = indexList.stream()
					.collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));
			vos.stream().forEach(item -> item.setEmail(emailMap.get(item.getUserId())));
		}
		result.setRows(vos);
		result.setTotal(userKycMapper.getModularUserKycListCount(query));
		return result;
	}

	@Override
	public KycDetailResponse getCurrentKycStatus(Long userId) {
		if (userId == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		KycDetailResponse kycDetailResponse = new KycDetailResponse();
		// 先获取当前用户的KYC状态信息
		KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
		if (certificateResult.getCertificateStatus() == null) {
			// 用户KYC为验证
			kycDetailResponse.setKycStatus(-1);
//			if (apolloCommonConfig.isFaceOcrSwitch()) {
//				kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
//			} else {
//				kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
//			}
			kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
			return kycDetailResponse;
		}
		kycDetailResponse.setFlowDefine("master");
		// 如果有验证，看看是否为验证通过或者验证拒绝了
		kycDetailResponse.setKycStatus(certificateResult.getCertificateStatus());
		kycDetailResponse.setKycMessage(certificateResult.getCertificateMessage());
		KycDetailResponse.FillInfo fillInfo = new KycDetailResponse.FillInfo();
		fillInfo.setCompanyName(
				StringUtils.isBlank(certificateResult.getCompanyName()) ? "" : certificateResult.getCompanyName());
		fillInfo.setFirstName(StringUtils.isBlank(certificateResult.getFirstName())
				|| "undefined".equals(certificateResult.getFirstName()) ? "" : certificateResult.getFirstName());
		fillInfo.setMiddleName(StringUtils.isBlank(certificateResult.getMiddleName())
				|| "undefined".equals(certificateResult.getMiddleName()) ? "" : certificateResult.getMiddleName());
		fillInfo.setLastName(StringUtils.isBlank(certificateResult.getLastName())
				|| "undefined".equals(certificateResult.getLastName()) ? "" : certificateResult.getLastName());
		fillInfo.setCountry(StringUtils.isBlank(certificateResult.getCountry()) ? "" : certificateResult.getCountry());
		fillInfo.setCity(StringUtils.isBlank(certificateResult.getCity()) ? "" : certificateResult.getCity());
		fillInfo.setAddress(StringUtils.isBlank(certificateResult.getAddress()) ? "" : certificateResult.getAddress());
		fillInfo.setPostalCode(StringUtils.isBlank(certificateResult.getPostalCode()) ? "" : certificateResult.getPostalCode());
		if(certificateResult.getDob() != null) {
			try {
				fillInfo.setDob(DateUtils.formatter(certificateResult.getDob(), "yyyy-MM-dd"));
			}catch(Exception e) {
				log.warn("currentStatus 获取生日解析异常 userId:{}", userId,e);
			}
		}

		kycDetailResponse.setFillInfo(fillInfo);
		kycDetailResponse.setForbidCountryPassed(certificateResult.isForbidPassed());
		// 设置认证类型
		if (KycCertificateResult.TYPE_USER == certificateResult.getCertificateType()) {
			// 个人认证，type 使用人脸识别类型中的code, 平衡做人脸识别时需要的类型
			kycDetailResponse.setType(FaceTransType.KYC_USER.getCode());
		} else {
			// 企业认证
			kycDetailResponse.setType(FaceTransType.KYC_COMPANY.getCode());
		}

		// 如果事正在审核中的状态，需要展现当前的子状态时什么，方便做下一步流程的跳转
		Long certificateId = certificateResult.getCertificateId();
		// 设置ID值返回方便做人脸识别时使用
		kycDetailResponse.setTransId(certificateId.toString());
		if (KycCertificateResult.STATUS_REVIEW == certificateResult.getCertificateStatus()) {
			// KYC 认证流程正在进行
			if (KycCertificateResult.TYPE_USER == certificateResult.getCertificateType()) {
				setUserKycSubStatus(userId, certificateId, kycDetailResponse);
			} else {
				setCompanyKycSubStatus(userId, certificateId, kycDetailResponse);
			}
		}
		if (KycSubStatus.BASIC.equals(kycDetailResponse.getKycSubStatus())
				|| KycSubStatus.FACE_OCR.equals(kycDetailResponse.getKycSubStatus())) {
//			if (!apolloCommonConfig.isFaceOcrSwitch()) {
//				kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
//			}
			kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
		}
		log.info("用户当前KYC认证状态: userId:{} type:{} status:{} subStatus:{}", userId, kycDetailResponse.getType(),
				kycDetailResponse.getKycStatus(), kycDetailResponse.getKycSubStatus());
		return kycDetailResponse;
	}

	/**
	 * KYC 个人认证的正在审核中的子状态信息
	 *
	 * @param userId
	 * @param kycId
	 * @param kycDetailResponse
	 */
	private void setUserKycSubStatus(Long userId, Long kycId, KycDetailResponse kycDetailResponse) {
		// 个人认证正在处理中的情况
		UserKyc userKyc = userKycMapper.getById(userId, kycId);
		if (userKyc == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		// 已经到终态的话不设置
		if (KycStatus.isEndStatus(userKyc.getStatus())) {
			return;
		}
		if (userKyc.getBaseInfo() == null || StringUtils.isBlank(userKyc.getBaseInfo().getCountry())) {
			// 如果 baseInfo 或者baseInfo.country 为空的，去提交basic
			kycDetailResponse.setKycSubStatus(KycSubStatus.BASIC);
			return;
		}
		boolean faceSwitch = apolloCommonConfig.isKycFaceSwitch();
		// 查看是使用jumio 验证还是 face_ocr 验证方式
		if (userKyc.isOcrFlow()) {
			// face_ocr 状态不为空的情况下，需要走 face_ocr 的方式
			if (IdCardOcrStatus.PROCESS.name().equalsIgnoreCase(userKyc.getFaceOcrStatus())
					|| IdCardOcrStatus.REFUSED.name().equalsIgnoreCase(userKyc.getFaceOcrStatus())) {
				// 1.1 如果face_ocr 状态是待处理或者已经拒绝，直接让用户进入 face_ocr 认证流程

				kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_OCR);
				return;
			}

			if (IdCardOcrStatus.REVIEW.name().equalsIgnoreCase(userKyc.getFaceOcrStatus())) {
				// 1.2 如果face ocr 正在审核中，那就只能等待审核
				kycDetailResponse.setKycSubStatus(KycSubStatus.AUDITING);
				return;
			}
			// 1.3 如果 face ocr 已经通过, 检查是否需要做人脸识别
			if (faceSwitch && !StringUtils.equalsAnyIgnoreCase(userKyc.getFaceStatus(), FaceStatus.FACE_REVIEW.name(),
					FaceStatus.FACE_PASS.name())) {
				// 进入人脸识别
				kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_PENDING);
				kycDetailResponse.setQrCode(AccountConstants.KYC_FL_PREFIX + ":" + FaceTransType.KYC_USER.getCode()
						+ ":" + kycDetailResponse.getTransId());
				return;
			}
			// 只能等待审核
			kycDetailResponse.setKycSubStatus(KycSubStatus.AUDITING);
			return;
		}
		// 看看当前是否在JUMIO需要上传的情况
		if (StringUtils.isBlank(userKyc.getScanReference())) {
			// 需要进行JUMIO上传
			kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
			return;
		}
		JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, userKyc.getScanReference(),
				JumioHandlerType.USER_KYC.getCode());
		if (jumioInfoVo == null || jumioInfoVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.INIT) {
			// 还未上传JUMIO的情况下，可以再次上传JUMIO
			kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
			return;
		}
		// 正在进行中的情况，看下是否正在进行人脸识别
		if (faceSwitch && !StringUtils.equalsAnyIgnoreCase(userKyc.getFaceStatus(), FaceStatus.FACE_REVIEW.name(),
				FaceStatus.FACE_PASS.name())) {
			// 如果用户通过SDK上传，不需要进行人脸识别，直接进入审核状态。
			if (jumioInfoVo != null && JumioScanSource.SDK.name().equalsIgnoreCase(jumioInfoVo.getSource())) {
				kycDetailResponse.setKycSubStatus(KycSubStatus.AUDITING);
			} else {
				// 正在进行人脸识别
				kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_PENDING);
				kycDetailResponse.setQrCode(AccountConstants.KYC_FL_PREFIX + ":" + FaceTransType.KYC_USER.getCode()
						+ ":" + kycDetailResponse.getTransId());
			}
			return;
		}
		// 还未上传JUMIO的情况下，可以再次上传JUMIO
		kycDetailResponse.setKycSubStatus(KycSubStatus.AUDITING);
	}

	/**
	 * KYC 企业认证的正在审核中的子状态信息
	 *
	 * @param userId
	 * @param certificateId
	 * @param kycDetailResponse
	 */
	private void setCompanyKycSubStatus(Long userId, Long certificateId, KycDetailResponse kycDetailResponse) {
		CompanyCertificate certificate = companyCertificateMapper.selectByPrimaryKey(userId, certificateId);
		if (certificate == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		if (StringUtils.isBlank(certificate.getScanReference())) {
			// 需要做JUMIO上传
			kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
			return;
		}
		boolean faceSwitch = apolloCommonConfig.isKycFaceSwitch();
		if (CompanyCertificateStatus.isEndStatus(certificate.getStatus())) {
			return;
		}
		if (faceSwitch && StringUtils.equalsAnyIgnoreCase(certificate.getFaceStatus(), FaceStatus.FACE_FAIL.name(),
				FaceStatus.FACE_PENDING.name())) {
			kycDetailResponse.setKycSubStatus(KycSubStatus.FACE_PENDING);
		} else {
			// 如果是这种状态下，需要检查下是否已经上传了JUMIO，如果上传了，进入等待，如果未上传，则可以进行重传JUMIO
			JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, certificate.getScanReference(),
					JumioHandlerType.COMPANY_KYC.getCode());
			if (jumioInfoVo == null || jumioInfoVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.INIT) {
				// 还未上传JUMIO的情况下，可以再次上传JUMIO
				kycDetailResponse.setKycSubStatus(KycSubStatus.JUMIO);
			} else {
				kycDetailResponse.setKycSubStatus(KycSubStatus.AUDITING);
			}
		}
	}

	@Override
	public void forceKycPassedToExpired(KycForceToExpiredRequest request) {
		if (request == null || request.getUserId() == null || request.getKycId() == null
				|| StringUtils.isBlank(request.getFailReason())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		Long userId = request.getUserId();
		Long kycId = request.getKycId();
		if (!StringUtils.equalsAnyIgnoreCase(request.getType(),
				com.binance.account.data.entity.certificate.JumioHandlerType.USER_KYC.getCode(),
				com.binance.account.data.entity.certificate.JumioHandlerType.COMPANY_KYC.getCode())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		int certificateType = StringUtils.equalsIgnoreCase(request.getType(),
				com.binance.account.data.entity.certificate.JumioHandlerType.USER_KYC.getCode()) ? 1 : 2;
		// 先获取当前用户的KYC认证信息，做对比，如果不一致，则不能进行处理
		KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
		if (certificateResult.getCertificateType() == null || certificateResult.getCertificateStatus() == null) {
			log.info("user kyc status unknown. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_STATUS_NOT_PASSED);
		}
		if (certificateResult.getCertificateStatus() != KycCertificateResult.STATUS_PASS) {
			log.info("user kyc status not passed. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_STATUS_NOT_PASSED);
		}
		if (kycId.longValue() != certificateResult.getCertificateId().longValue()
				|| certificateType != certificateResult.getCertificateType()) {
			log.info("请求的KYC 认证类型和数据不能匹配，不允许修改. userId:{} kycId:{} certificateId:{} certificateType:{}", userId, kycId,
					certificateResult.getCertificateId(), certificateResult.getCertificateType());
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (certificateType == 1) {
			// USER KYC
			forceUserKycPassedToExpired(userId, kycId, request.getFailReason());
		} else {
			// COMPANY KYC 目前不能进行重置企业验证
			throw new BusinessException(GeneralCode.SYS_ERROR, "不能重置过期企业认证");
		}
	}

	/**
	 * 个人认证由通过状态变更到过期状态
	 *
	 * @param userId
	 * @param kycId
	 * @param failReason
	 */
	private void forceUserKycPassedToExpired(Long userId, Long kycId, String failReason) {
		log.info("强制把用户KYC 已通过状态变更为过期状态. userId:{} kycId:{}", userId, kycId);
		KycAuditRequest kycAuditRequest = new KycAuditRequest();
		kycAuditRequest.setId(kycId);
		kycAuditRequest.setUserId(userId);
		kycAuditRequest.setStatus(KycStatus.jumioPassed);
		APIResponse userKycReset = this.audit(APIRequest.instance(kycAuditRequest));
		if (userKycReset.getStatus() != APIResponse.Status.OK) {
			log.warn("重置个人认证失败. userId:{} kycId:{} transId:{}", userId, kycId);
			throw new BusinessException(GeneralCode.SYS_ERROR, "重置用户KYC状态失败");
		}
		log.info("重置用户KYC状态成功，下一步变更状态为过期状态, userId:{} kycId:{}", userId, kycId);
		// 如果能重置成功，则把用户KYC状态变更到过期状态
		UserKyc userKyc = iUserCertificate.getUserKycFromMasterDbById(userId, kycId);
		if (userKyc == null) {
			throw new BusinessException(GeneralCode.SYS_ERROR, "获取KYC记录失败");
		}
		userKyc.setFailReason(failReason);
		iUserCertificate.userKycExpired(userKyc);
	}

	@Override
	public Boolean saveXfersUserKyc(UserKycVo userKycVo) {
		Jumio jumio = new Jumio();
		jumio.setUserId(userKycVo.getUserId());
		jumio.setType(JumioType.user);
		jumio.setLastName(userKycVo.getBaseInfo().getLastName());
		jumio.setFirstName(userKycVo.getBaseInfo().getFirstName());
		Country country = countryMapper.selectByNationality(userKycVo.getBaseInfo().getNationality());
		if (country == null) {
			log.error("saveXfersUserKyc nationality={} have no Country.", userKycVo.getBaseInfo().getNationality());
			return false;
		}
		jumio.setIssuingCountry(country.getCode());
		jumio.setNumber(userKycVo.getCheckInfo().getNumber());
		jumio.setDocumentType(userKycVo.getCheckInfo().getDocumentType());
		jumio.setFront(userKycVo.getCheckInfo().getFront());
		jumio.setBack(userKycVo.getCheckInfo().getBack());
		jumio.setFace(userKycVo.getCheckInfo().getFace());
		jumio.setCreateTime(DateUtils.getNewUTCDate());
		jumio.setUpdateTime(DateUtils.getNewUTCDate());
		jumio.setSource(userKycVo.getCheckInfo().getSource());
		;
		jumio.setStatus(JumioStatus.jumioPassed);
		jumio.setDob(userKycVo.getCheckInfo().getDob());
		jumio.setAddress(userKycVo.getBaseInfo().getAddress());
		jumio.setPostalCode(userKycVo.getBaseInfo().getPostalCode());
		jumio.setCity(userKycVo.getBaseInfo().getCity());
		jumio.setExpiryDate(userKycVo.getCheckInfo().getExpiryDate());
		jumio.setScanReference(UUIDUtils.getId());
		jumioMapper.insert(jumio);
		log.info("Xfers KYC => KYC补充jumio数据: userId:{} jumioId:{}", userKycVo.getUserId(), jumio.getId());

		UserKyc userKyc = new UserKyc();
		userKyc.setUserId(userKycVo.getUserId());
		userKyc.setStatus(KycStatus.passed);
		userKyc.setUpdateTime(DateUtils.getNewUTCDate());
		userKyc.setCreateTime(DateUtils.getNewUTCDate());
		userKyc.setJumioId(jumio.getId());
		userKyc.setScanReference(jumio.getScanReference());
		userKyc.setCheckStatus(com.binance.inspector.common.enums.JumioStatus.PASSED.name());
		UserKyc.BaseInfo baseInfo = new UserKyc.BaseInfo();
		BeanUtils.copyProperties(userKycVo.getBaseInfo(), baseInfo);
		userKyc.setBaseInfo(baseInfo);
		userKycMapper.saveBaseInfo(userKyc);
		log.info("Xfers KYC => KYC补充user_kyc数据：userId:{} kycId:{}", userKycVo.getUserId(), userKyc.getId());
		Long userId = userKyc.getUserId();
		Long kycId = userKyc.getId();

		// inspector 存储
		XfersJumioKycRequest kycRequest = new XfersJumioKycRequest();
		BeanUtils.copyProperties(jumio, kycRequest);
		kycRequest.setIssuingCountry(country.getCode2());
		kycRequest.setBizId(userKyc.getId().toString());
		kycRequest.setTypeCode(JumioHandlerType.USER_KYC.getCode());
		kycRequest.setBaseUrl("https://www.binance.sg/");
		APIResponse<Integer> response = jumioApi.saveXfersUserJumio(APIRequest.instance(kycRequest));
		if (response.getStatus() == Status.ERROR || response.getData() <= 0) {
			log.info("Xfers KYC => save inspector jumio_info failed userId={} kycId:{}", userId, kycId);
			return false;
		}

		// save user_kyc_approve
		int kycApproveRow = userKycApproveMapper.insert(UserKycApprove.toKycApprove(userKyc));
		log.info("Xfers KYC => 保存用户的KYC认证通过信息：userId:{} kycId:{}, kycApproveRow:{}", userId, kycId, kycApproveRow);
		if (kycApproveRow <= 0) {
			// 记录存在，更新数据
			userKycApproveMapper.updateSelective(UserKycApprove.toKycApprove(userKyc));
		}
		// save certificate_index
		UserCertificateIndex certificateIndex = new UserCertificateIndex();
		certificateIndex.setUserId(jumio.getUserId());
		certificateIndex.setNumber(jumio.getNumber());
		certificateIndex.setCountry(jumio.getIssuingCountry());
		certificateIndex.setType(jumio.getDocumentType());
		certificateIndex.setCertificateType(KycCertificateResult.TYPE_USER);
		certificateIndex.setCreateTime(DateUtils.getNewUTCDate());
		int certificateIndexRow = this.userCertificateIndexMapper.insertIgnore(certificateIndex);
		log.info("Xfers KYC => 保存用户的证件类型信息：userId:{} kycId:{}, certificateIndexRow:{}", userId, kycId,
				certificateIndexRow);

		// save user_address
		UserAddress userAddress = new UserAddress();
		userAddress.setUserId(userId);
		userAddress.setStatus(UserAddress.Status.PASSED);
		userAddress.setCreateTime(DateUtils.getNewUTCDate());
		if (userKyc.getBaseInfo() != null) {
			userAddress.setCheckFirstName(userKyc.getBaseInfo().getFirstName());
			userAddress.setCheckLastName(userKyc.getBaseInfo().getLastName());
			userAddress.setCountry(userKyc.getBaseInfo().getCountry());
			userAddress.setCity(userKyc.getBaseInfo().getCity());
			userAddress.setStreetAddress(userKyc.getBaseInfo().getAddress());
			userAddress.setPostalCode(userKyc.getBaseInfo().getPostalCode());
		}
		userAddress.setDaySubmitCount(1);
		userAddress.setAddressFile("");
		userAddress.setApprover("system");
		userAddress.setFailReason("免审核自动通过");
		userAddress.setApproveTime(DateUtils.getNewUTCDate());
		int addressRow = userAddressMapper.insert(userAddress);
		log.info("Xfers KYC => save user_address success. userId:{} kycId:{} addressRow:{}", userId, kycId, addressRow);

		int userCertificateStatusRow = iUserCertificate.updateCertificateStatus(userId, true);
		log.info("Xfers KYC => 更新保存用户的KYC认证状态：userId:{} kycId:{}, userCertificateStatusRow:{}", userId, kycId,
				userCertificateStatusRow);
		int securityLevelRow = this.updateUserSecLevel(userId, true);
		log.info("Xfers KYC => 更新保存用户的安全等级: userId:{} kycId:{}, securityLevelRow:{}", userId, kycId, securityLevelRow);
		return true;
	}

	@Override
	public Boolean updateXfersUserKyc(UserKycVo userKycVo) {
		// TODO Auto-generated method stub
		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userKycVo.getUserId());
		if (userKycApprove == null || StringUtils.isEmpty(userKycApprove.getJumioId())) {
			log.info("updateXfersUserKyc the user {}, kyc approve is null ", userKycVo.getUserId());
			return false;
		}
		Jumio jumio = new Jumio();
		jumio.setScanReference(userKycApprove.getScanReference());
		jumio.setId(userKycApprove.getJumioId());
		jumio.setUserId(userKycVo.getUserId());
		jumio.setFront(userKycVo.getCheckInfo().getFront());
		jumio.setBack(userKycVo.getCheckInfo().getBack());
		jumio.setFace(userKycVo.getCheckInfo().getFace());
		jumio.setUpdateTime(DateUtils.getNewUTCDate());

		jumioMapper.updateByPrimaryKeySelective(jumio);
		log.info("Xfers updateXfersUserKyc jumio数据: userId:{}", userKycVo.getUserId());

		// inspector 存储
		DirectSavePhotoRequest kycRequest = new DirectSavePhotoRequest();
		BeanUtils.copyProperties(jumio, kycRequest);

		APIResponse<Void> response = jumioApi.directSavePhoto(APIRequest.instance(kycRequest));

		if (response.getStatus() == Status.ERROR) {
			log.info("Xfers updateXfersUserKyc update inspector jumio_info failed userId={}", userKycVo.getUserId());
			return false;
		}
		return true;
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public APIResponse<?> refuseApprove(APIRequest<KycAuditRequest> request) {
		KycAuditRequest requestBody = request.getBody();
		Long userId = requestBody.getUserId();
		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
		if (userKycApprove == null) {
			log.info("kyc审核通过重置为失败 USER KYC APPROVE 信息验证失败. userId:{}", userId);
			return APIResponse.getErrorJsonResult("kyc审核通过重置为失败 USER KYC APPROVE 信息验证失败.");
		}
		if (userKycApprove.getCertificateType() == null) {
			log.info("kyc 审核通过中历史数据,暂不处理. userId:{}", userId);
			return APIResponse.getErrorJsonResult("kyc审核通过重置为失败 USER KYC APPROVE 为历史数据,暂无法处理.");
		}

		UserKyc userKyc = userKycMapper.getLast(userId);
		if (userKyc == null || KycStatus.delete == userKyc.getStatus()) {
			log.warn("kyc 查询获取失败或者已经删除. userId:{}", userId);
			return APIResponse.getErrorJsonResult("kyc数据获取失败或已删除,暂无法处理.");
		}
		UserKycAuditContext auditContext = kycAduitContextBusiness.builder(userKyc);
		auditContext.setUserKycApprove(userKycApprove);
		auditContext.setAuditStatus(KycStatus.jumioPassed);
		auditKycResetHandler(auditContext);

		// 审核拒绝
		auditContext.setAuditStatus(KycStatus.refused);
		auditContext.setFailReason(requestBody.getFailReason());
		auditKycRefusedHandler(auditContext);

		userKyc.setUpdateTime(new Date());
		userKyc.appendMemo(requestBody.getMemo());
		userKycMapper.updateStatus(userKyc);
		return APIResponse.getOKJsonResult(null);
	}

	@Override
	public Boolean submitSimpleBaseInfo(KycSimpleBaseInfoRequest request) throws Exception {
		Long userId = request.getUserId();
		// 检查是否已经认证过了
		AccUserKycExample example = new AccUserKycExample();
		example.createCriteria().andUserIdEqualTo(request.getUserId());
		example.setOrderByClause("create_time desc");
		List<AccUserKyc> accUserKycList = accUserKycMapper.selectByExample(example);
		if (CollectionUtils.isNotEmpty(accUserKycList)) {
			AccUserKyc accUserKyc = accUserKycList.get(0);
			if (accUserKyc.getStatus() == 1) {
				throw new BusinessException(GeneralCode.USER_KYC_PASSED);
			} else if (accUserKyc.getFillFirstName() != null && accUserKyc.getFillLastName() != null
					&& accUserKyc.getFillDob() != null && accUserKyc.getNationality() != null) {
				throw new BusinessException(GeneralCode.USER_KYC_PASSED);
			}
		}
		// 加入一个锁, 防止重复提交导致多次初始化的问题
		Lock lock = RedisCacheUtils.getLock(AccountConstants.USER_KYC_INIT_LOCK + userId);
		if (lock != null && lock.tryLock(RedisCacheUtils.DEFAULT_LOCK_TIMEOUT, TimeUnit.SECONDS)) {
			try {
				AccUserKyc accUserKyc = new AccUserKyc();
				accUserKyc.setUserId(request.getUserId());
				accUserKyc.setNationality(request.getNationality());
				accUserKyc.setFillFirstName(request.getFirstName());
				accUserKyc.setFillMiddleName(request.getMiddleName());
				accUserKyc.setFillLastName(request.getLastName());
				accUserKyc.setFillDob(request.getDob());
				accUserKyc.setCheckType(request.getIdType());
				accUserKyc.setCheckNumber(request.getIdNo());
				accUserKyc.setStatus((byte) KycStatus.basic.ordinal());
				accUserKyc.setUpdateTime(DateUtils.getNewUTCDate());
				accUserKyc.setCreateTime(DateUtils.getNewUTCDate());
				accUserKycMapper.insertSelective(accUserKyc);
				return true;
			} catch (Exception e) {
				log.error("{}--submitSimpleBaseInfo exception", request.getUserId(), e);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			} finally {
				lock.unlock();
			}
		} else {
			throw new BusinessException(GeneralCode.GW_TOO_MANY_REQUESTS);
		}
	}

	@Override
	public APIResponse<UserSimpleBaseInfoResponse> getSimpleBaseInfo(APIRequest<UserIdRequest> request)
			throws Exception {
		AccUserKycExample example = new AccUserKycExample();
		example.createCriteria().andUserIdEqualTo(request.getBody().getUserId());
		example.setOrderByClause("create_time desc");
		List<AccUserKyc> accUserKycList = accUserKycMapper.selectByExample(example);
		UserSimpleBaseInfoResponse userSimpleBaseInfoResponse = UserSimpleBaseInfoResponse.builder().build();
		if (CollectionUtils.isNotEmpty(accUserKycList)) {
			userSimpleBaseInfoResponse = UserSimpleBaseInfoResponse.builder().dob(accUserKycList.get(0).getFillDob())
					.firstName(accUserKycList.get(0).getFillFirstName())
					.middleName(accUserKycList.get(0).getFillMiddleName())
					.lastName(accUserKycList.get(0).getFillLastName())
					.nationality(accUserKycList.get(0).getNationality()).build();
		}
		log.info("{}-查询base简单信息结果:{}", request.getBody().getUserId(),
				JSONObject.toJSONString(userSimpleBaseInfoResponse));
		return APIResponse.getOKJsonResult(userSimpleBaseInfoResponse);
	}

	@Override
	public APIResponse<KycFormAddrResponse> getKycFormAddrByUserIds(GetUserListRequest request) {
		KycFormAddrResponse response = new KycFormAddrResponse();
		List<KycFormAddrVo> kycFormAddrVos = new ArrayList<>();
		final Map<String, String> countryCacheMap = new HashMap<>();
		for (Long userId : request.getUserIds()) {
			KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
			KycFormAddrVo vo = new KycFormAddrVo();
			vo.setUserId(userId);

			// 新版本逻辑
			if (certificateResult.isNewVersion()) {
				KycFillInfo base = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
				if (base == null) {
					kycFormAddrVos.add(vo);
					continue;
				}
				vo.setCountry(this.getCountryName(base.getCountry(), countryCacheMap));
				vo.setCity(StringUtils.isBlank(base.getCity()) ? "" : base.getCity());
				vo.setAddr(StringUtils.isNotBlank(base.getAddress()) ? base.getAddress() : base.getCompanyAddress());
				kycFormAddrVos.add(vo);
				continue;
			}
			// 老版本逻辑
			if (certificateResult.getCertificateType() != null) {
				if (KycCertificateResult.TYPE_USER == certificateResult.getCertificateType()) {
					// 个人认证
					UserKyc userKyc = userKycMapper.getLast(userId);
					if (userKyc != null && userKyc.getBaseInfo() != null) {
						vo.setCountry(this.getCountryName(userKyc.getBaseInfo().getCountry(), countryCacheMap));
						vo.setCity(userKyc.getBaseInfo().getCity());
						vo.setAddr(userKyc.getBaseInfo().getAddress());
					}
				} else {
					// 企业认证
					CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
					if (companyCertificate != null) {
						vo.setCountry(this.getCountryName(companyCertificate.getCompanyCountry(), countryCacheMap));
						vo.setCity("");
						vo.setAddr(companyCertificate.getCompanyAddress());
					}
				}
			}
			kycFormAddrVos.add(vo);
		}
		response.setKycFormAddrVos(kycFormAddrVos);
		return APIResponse.getOKJsonResult(response);
	}

	private String getCountryName(String code, final Map<String, String> countryCacheMap) {
		if (countryCacheMap.get(code) == null) {
			Country country = countryMapper.selectByPrimaryKey(code);
			countryCacheMap.put(code, country == null ? code : country.getCn());
		}
		return countryCacheMap.get(code);
	}

}
