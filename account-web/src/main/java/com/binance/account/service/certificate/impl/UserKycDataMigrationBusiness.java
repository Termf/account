package com.binance.account.service.certificate.impl;

import com.binance.account.common.enums.CertificateType;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.query.CompanyCertificateQuery;
import com.binance.account.common.query.JumioQuery;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.service.certificate.IUserKycDataMigration;
import com.binance.account.service.face.FaceHandlerHelper;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingContext;
import com.binance.account.service.kyc.convert.KycApiTransferAdapterConvertor;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;

import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
public class UserKycDataMigrationBusiness implements IUserKycDataMigration {

	/**
	 * MOVE_SUCCESS,DONT_MOVE,DELTE_CERTIFICATE_USER,DELTE_CERTIFICATE_COMPANY 都算成功
	 */
	// 迁移成功
	private static final String MOVE_SUCCESS = "MOVE_SUCCESS";

	// 迁移成功
	private static final String DONT_MOVE = "DONT_MOVE";

	// delete 属于重复提交，用户个人/企业已经老流程通过，新流程又提交了企业/个人
	private static final String DELTE_CERTIFICATE_USER = "DELTE_CERTIFICATE_USER";

	private static final String DELTE_CERTIFICATE_COMPANY = "DELTE_CERTIFICATE_COMPANY";

	// 未知kyc类型，approve表有数据，但是userKyc，companyCertificate，kycCertificate都没有数据
	private static final String UNKNOW_KYC_TYPE = "UNKNOW_KYC_TYPE";

	// KYC_TYPE需要修改为企业，approve表有数据，认证类型为个人，但是userKyc，kycCertificate都没有数据，companyCertificate存在数据。
	private static final String KYC_TYPE_NEED_CHANGE_COMPANY = "KYC_TYPE_NEED_CHANGE_COMPANY";

	// KYC_TYPE需要修改为个人，approve表有数据，认证类型为企业，但是companyCertificate，kycCertificate都没有数据，userKyc存在数据。
	private static final String KYC_TYPE_NEED_CHANGE_USER = "KYC_TYPE_NEED_CHANGE_USER";

	// 新老流程都通过
	private static final String DOUBLE_PASS = "DOUBLE_PASS";

	// USER_KYC未通过
	private static final String USER_KYC_NOT_PASS = "USER_KYC_NOT_PASS";

	private static final String EXCEPTION = "EXCEPTION";

	private static final String FACE_CHECK_SUCCESS = "SUCCESS";
	private static final String FACE_CHECK_FAIL = "FAIL";

	@Resource
	private UserKycApproveMapper userKycApproveMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private CompanyCertificateMapper companyCertificateMapper;

	@Resource
	private KycCertificateMapper kycCertificateMapper;

	@Resource
	private KycFillInfoMapper kycFillInfoMapper;
	@Resource
	private UserCommonBusiness userCommonBusiness;
	@Resource
	private FaceHandlerHelper faceHandlerHelper;

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Resource
	private UserChannelRiskRatingContext userChannelRiskRatingContext;

	@Override
	public List<UserKycApprove> selectPage(String moveMsg, int start, int rows) {
		return userKycApproveMapper.selectKycDataMigration(moveMsg, start, rows);
	}

	@Override
	public void moveToKycCertificateByUserId(Long userId) {
		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
		moveToKycCertificate(userKycApprove);
	}

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	@Override
	public void moveToKycCertificate(UserKycApprove userKycApprove) {
		CertificateType certificateType = CertificateType.getByCode(userKycApprove.getCertificateType());
		switch (certificateType) {
		case USER:
			this.moveUser(userKycApprove);
			break;
		case COMPANY:
			this.moveCompany(userKycApprove);
			break;
		default:
			moveUnverified(userKycApprove);
			break;
		}

	}

	@Override
	public void addExceptionTag(UserKycApprove userKycApprove) {
		userKycApproveMapper.updateMoveMsg(userKycApprove.getUserId(), EXCEPTION);

	}

	private void moveUnverified(UserKycApprove userKycApprove) {
		userKycApproveMapper.updateMoveMsg(userKycApprove.getUserId(), UNKNOW_KYC_TYPE);
		return;
	}

	private void moveUser(UserKycApprove userKycApprove) {
		Long userId = userKycApprove.getUserId();
		UserKyc userKyc = userKycMapper.getLast(userKycApprove.getUserId());
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		// 同时为空判断是否存在CompanyCertificate
		if (kycCertificate == null && userKyc == null) {
			CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
			if (companyCertificate == null) {
				userKycApproveMapper.updateMoveMsg(userId, UNKNOW_KYC_TYPE);
				return;
			}
			userKycApproveMapper.updateMoveMsg(userId, KYC_TYPE_NEED_CHANGE_COMPANY);
			return;
		}

		/**
		 * case情况: 1.kycCertificate 不存在记录，直接迁移至kycCertificate 2.kycCertificate
		 * 存在个人认证记录，则直接跳过不做 3.kycCertificate
		 * 存在企业认证记录，则判断企业认证记录是否通过，如果通过，则打标，如果没通过则直接删除这条kycCertificate记录。
		 */
		if (kycCertificate == null) {
			if (!KycStatus.passed.equals(userKyc.getStatus())) {
				userKycApproveMapper.updateMoveMsg(userId, USER_KYC_NOT_PASS);
				return;
			}
			kycCertificate = KycApiTransferAdapterConvertor.convert2KycCertificate(userKyc);
			KycFillInfo kycFillInfo = KycApiTransferAdapterConvertor.convert2KycFillInfo(userKyc, userKycApprove);
			kycCertificateMapper.insert(kycCertificate);
			kycFillInfoMapper.insert(kycFillInfo);
			userKycApproveMapper.updateMoveMsg(userId, MOVE_SUCCESS);
			applyWck(kycCertificate, kycFillInfo, userKycApprove);
			return;
		}

		if (KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			userKycApproveMapper.updateMoveMsg(userId, DONT_MOVE);
			return;
		}
		// 企业认证未通过则直接删除当前记录，补充一条个人信息
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			kycCertificateMapper.deleteByPk(userId);
			kycFillInfoMapper.deleteByUk(userId, KycFillType.BASE.name());
			kycCertificate = KycApiTransferAdapterConvertor.convert2KycCertificate(userKyc);
			KycFillInfo kycFillInfo = KycApiTransferAdapterConvertor.convert2KycFillInfo(userKyc, userKycApprove);
			kycCertificateMapper.insert(kycCertificate);
			kycFillInfoMapper.insert(kycFillInfo);
			userKycApproveMapper.updateMoveMsg(userId, DELTE_CERTIFICATE_COMPANY);
			return;

		}
		userKycApproveMapper.updateMoveMsg(userId, DOUBLE_PASS);
		return;

	}

	private void moveCompany(UserKycApprove userKycApprove) {
		Long userId = userKycApprove.getUserId();
		CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (companyCertificate == null && kycCertificate == null) {
			UserKyc userKyc = userKycMapper.getLast(userId);
			if (userKyc == null) {
				userKycApproveMapper.updateMoveMsg(userId, UNKNOW_KYC_TYPE);
				return;
			}
			userKycApproveMapper.updateMoveMsg(userId, KYC_TYPE_NEED_CHANGE_USER);
			return;
		}

		if (kycCertificate == null) {
			if (!CompanyCertificateStatus.passed.equals(companyCertificate.getStatus())) {
				userKycApproveMapper.updateMoveMsg(userId, USER_KYC_NOT_PASS);
				return;
			}
			kycCertificate = KycApiTransferAdapterConvertor.convert2KycCertificate(companyCertificate);
			KycFillInfo kycFillInfo = KycApiTransferAdapterConvertor.convert2KycFillInfo(companyCertificate);
			kycFillInfo.setCountry(userKycApprove.getBaseInfo().getCountry());
			kycCertificateMapper.insert(kycCertificate);
			kycFillInfoMapper.insert(kycFillInfo);
			userKycApproveMapper.updateMoveMsg(userId, MOVE_SUCCESS);
			return;
		}

		if (KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
			userKycApproveMapper.updateMoveMsg(userId, DONT_MOVE);
			return;
		}
		// 企业认证未通过则直接删除当前记录，补充一条个人信息
		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			kycCertificateMapper.deleteByPk(userId);
			kycFillInfoMapper.deleteByUk(userId, KycFillType.BASE.name());
			kycCertificate = KycApiTransferAdapterConvertor.convert2KycCertificate(companyCertificate);
			KycFillInfo kycFillInfo = KycApiTransferAdapterConvertor.convert2KycFillInfo(companyCertificate);
			kycCertificateMapper.insert(kycCertificate);
			kycFillInfoMapper.insert(kycFillInfo);
			userKycApproveMapper.updateMoveMsg(userId, DELTE_CERTIFICATE_USER);
			return;

		}

		userKycApproveMapper.updateMoveMsg(userId, DOUBLE_PASS);
		return;

	}

	@Override
	public String checkKycFaceCheck(UserKycApprove userKycApprove) {
		if (userKycApprove == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (FACE_CHECK_SUCCESS.equalsIgnoreCase(userKycApprove.getFaceCheck())) {
			return FACE_CHECK_SUCCESS;
		}
		Long userId = userKycApprove.getUserId();
		KycCertificateResult result = userCommonBusiness.getKycStatusByUserId(userId);
		if (result.getCertificateStatus() == null
				|| result.getCertificateStatus() != KycCertificateResult.STATUS_PASS) {
			log.warn("userKycApporve 存在但获取不到kyc通过状态, userId:{}", userId);
			updateFaceCheck(EXCEPTION, userId);
			return EXCEPTION;
		}
		String transId = "KYC_FACE_CHECK:" + UUID.randomUUID().toString().replaceAll("-", "");
		boolean check = faceHandlerHelper.checkCurrentKycCanDoFace(userId, result, transId, 2);
		log.info("检查用户kyc图片是否能做人脸识别结果: userId:{} result:{}", userId, check);
		String checkResult = check ? FACE_CHECK_SUCCESS : FACE_CHECK_FAIL;
		updateFaceCheck(checkResult, userId);
		return checkResult;
	}

	@Override
	public List<UserKycApprove> selectFaceCheckPage(String faceCheck, int start, int rows, Long userId) {
		return userKycApproveMapper.selectFaceCheckList(faceCheck, start, rows, userId);
	}

	private int updateFaceCheck(String faceCheck, Long userId) {
		return userKycApproveMapper.updateFaceCheck(userId, faceCheck);
	}

	private void applyWck(KycCertificate kycCertificate, KycFillInfo kycFillInfo, UserKycApprove userKycApprove) {
		if(userKycApprove == null) {
			return;
		}
		
		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(kycCertificate.getUserId());

		if (ratings == null || ratings.size() < 0) {
			return;
		}

		for (UserChannelRiskRating riskRating : ratings) {
			try {
				UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());
				userChannelRiskRatingContext.getRatingHandler(channelCode).applyThirdPartyRisk(riskRating,
						kycCertificate, kycFillInfo, userKycApprove);
			} catch (Exception e) {
				log.warn("数据迁移上送三方风险评估异常. userId:{},riskRatingId:{},channelCode:{}", kycCertificate.getUserId(),
						riskRating.getId(), riskRating.getChannelCode(), e);
			}

		}

	}

	@Override
	public List<UserKyc> selectUserPage(int start, int rows) {
		JumioQuery query = new JumioQuery();
		query.setStatus(String.valueOf(KycStatus.jumioPassed.ordinal()));
		query.setPage(start);
		query.setRows(rows);
		return userKycMapper.getList(query);
	}

	@Override
	public List<CompanyCertificate> selectCompanyPage(int start, int rows) {
		CompanyCertificateQuery query = new CompanyCertificateQuery();
		query.setStatus(CompanyCertificateStatus.jumioPassed.ordinal() + "");
		query.setPage(start);
		query.setRows(rows);
		return companyCertificateMapper.getList(query);
	}

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void moveUserKyc(UserKyc userKyc) {
		Long userId = userKyc.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate != null) {
			log.info("执行UserKycDataMigrationJobTwoHandler 用户已存在kycCertificate.重置userKyc失效. userId:{}", userId);
			expiredUser(userKyc, "kycCertificate exists");
			return;
		}

		kycCertificate = KycApiTransferAdapterConvertor.convert2KycCertificate(userKyc);
		kycCertificate.setStatus(KycCertificateStatus.PROCESS.name());
		kycCertificate.setFaceStatus(KycCertificateStatus.PROCESS.name());
		kycCertificate.setKycLevel(KycCertificateKycLevel.L0.getCode());
		
		KycFillInfo kycFillInfo = KycApiTransferAdapterConvertor.convert2KycFillInfo(userKyc, null);
		kycCertificateMapper.insert(kycCertificate);
		kycFillInfoMapper.insert(kycFillInfo);
		expiredUser(userKyc, "move success");
		return;

	}
	
	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public void moveCompany(CompanyCertificate companyCertificate) {
		Long userId = companyCertificate.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate != null) {
			log.info("执行UserKycDataMigrationJobTwoHandler 用户已存在kycCertificate.重置userKyc失效. userId:{}", userId);
			expiredCompany(companyCertificate, "kycCertificate exists");
			return;
		}

		kycCertificate = KycApiTransferAdapterConvertor.convert2KycCertificate(companyCertificate);
		KycFillInfo kycFillInfo = KycApiTransferAdapterConvertor.convert2KycFillInfo(companyCertificate);
		kycCertificateMapper.insert(kycCertificate);
		kycFillInfoMapper.insert(kycFillInfo);
		expiredCompany(companyCertificate, "move success");
		return;

	}

	public void expiredUser(UserKyc userKyc, String memo) {
		UserKyc record = new UserKyc();
		record.setId(userKyc.getId());
		record.setUserId(userKyc.getUserId());
		record.setUpdateTime(DateUtils.getNewUTCDate());
		record.setFailReason(userKyc.getFailReason());
		record.setMemo("system check "+memo);
		record.setStatus(KycStatus.expired);
		userKycMapper.updateStatus(record);
	}
	
	public void expiredCompany(CompanyCertificate companyCertificate, String memo) {
		CompanyCertificate record = new CompanyCertificate();
		record.setId(companyCertificate.getId());
		record.setUserId(companyCertificate.getUserId());
		record.setUpdateTime(DateUtils.getNewUTCDate());
		record.setInfo("system check "+memo);
		record.setStatus(CompanyCertificateStatus.expired);
		companyCertificateMapper.updateByPrimaryKeySelective(record);
	}
}
