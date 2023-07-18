package com.binance.account.service.kyc.executor;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFlowType;
import com.binance.account.service.kyc.validator.FaceInitValidator;
import com.binance.account.service.security.IUserFace;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.kyc.request.FaceInitFlowRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.kyc.response.KycFaceInitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Log4j2
@Service
public class FaceInitExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private KycCertificateMapper kycCertificateMapper;
	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private IUserFace iUserFace;
	@Autowired
	private FaceInitValidator validator;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		// 去除了是否有美国站的错误风险
		if (!config.isKycFaceSwitch()) {
			log.info("kyc.face.switch 目前关闭,暂不支持face 验证 userId:{}", kycFlowRequest.getUserId());

			KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(kycFlowRequest.getUserId());
			if (kycCertificate != null) {
				if (StringUtils.isEmpty(kycCertificate.getFaceStatus())
						|| StringUtils.equalsAny(kycCertificate.getFaceStatus(), KycCertificateStatus.PROCESS.name(),
								KycCertificateStatus.REFUSED.name())) {
					log.info("kyc.face.switch 目前关闭,当前face状态:{}更新为SKIP userId:{}", kycCertificate.getFaceStatus(),
							kycFlowRequest.getUserId());
					KycCertificate record = new KycCertificate();
					record.setUserId(kycFlowRequest.getUserId());
					record.setFaceStatus(KycCertificateStatus.SKIP.name());
					record.setUpdateTime(DateUtils.getNewUTCDate());
					kycCertificateMapper.updateByPrimaryKeySelective(record);
				}
			}
			KycFaceInitResponse kycFaceInitResponse = new KycFaceInitResponse();
			kycFaceInitResponse.setKycFaceSwitch(false);
			return kycFaceInitResponse;
		}

		// 判断是否是 lock jumio 过来的请求.如果是则直接init face
		if (KycFlowType.SKIP_JUMIO.equals(KycFlowContext.getContext().getKycFlowType())) {
			KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
			KycCertificateKycType certificateKycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
			FaceFlowInitResult faceFlowInitResult = initKycFaceFlow(kycCertificate, UUIDUtils.getId(), certificateKycType);
			JumioInitResponse response = (JumioInitResponse) KycFlowContext.getContext().getKycFlowResponse();
	        response.setTransId(faceFlowInitResult.getTransId());
	        response.setType(faceFlowInitResult.getType());
	        return response;
		}

		//如果是init jumio 则直接跳过initFace
		if (KycFlowType.INIT_JUMIO.equals(KycFlowContext.getContext().getKycFlowType())) {
			return KycFlowContext.getContext().getKycFlowResponse();
		}

		//默认从initface入口
		FaceInitFlowRequest request = (FaceInitFlowRequest) kycFlowRequest;

		Long userId = request.getUserId();

		validator.validateApiRequest(request);

		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
		if (kycCertificate == null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		}
		if (kycCertificate == null) {
			log.warn("KYC认证FACE => 获取不到用户KYC认证记录信息. userId:{}", userId);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}

		validator.validateKycCertificateStatus(kycCertificate);
		validator.validateRequestCount(kycCertificate);

		KycCertificateKycType certificateKycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
		if (certificateKycType == null) {
			log.warn("KYC认证FACE => 认证类型获取失败. userId:{}", userId);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

		String transId = request.getTransId();

		// 其他情况，检查当前是否用正在进行到人脸识别流程，如果有，直接返回当前正在做人脸识别到信息,否则，重新创建人脸识别流程

		FaceFlowInitResult faceFlowInitResult = initKycFaceFlow(kycCertificate, transId, certificateKycType);

		KycFaceInitResponse kycFaceInitResponse = new KycFaceInitResponse();
		kycFaceInitResponse.setKycFaceSwitch(true);
		kycFaceInitResponse.setUserId(userId);
		kycFaceInitResponse.setKycType(certificateKycType);
		kycFaceInitResponse.setTransId(faceFlowInitResult.getTransId());
		kycFaceInitResponse.setTransType(faceFlowInitResult.getType());
		return kycFaceInitResponse;
	}

	private FaceFlowInitResult initKycFaceFlow(KycCertificate kycCertificate, String transId,
			KycCertificateKycType certificateKycType) {

		Long userId = kycCertificate.getUserId();

		FaceTransType faceTransType = certificateKycType == KycCertificateKycType.USER ? FaceTransType.KYC_USER
				: FaceTransType.KYC_COMPANY;

		// 其他情况，检查当前是否用正在进行到人脸识别流程，如果有，直接返回当前正在做人脸识别到信息,否则，重新创建人脸识别流程
		TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
		KycFaceInitResponse kycFaceInitResponse = new KycFaceInitResponse();
		kycFaceInitResponse.setKycFaceSwitch(true);
		kycFaceInitResponse.setUserId(userId);
		kycFaceInitResponse.setKycType(certificateKycType);
		if (faceLog == null || TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
			if (StringUtils.isBlank(transId)) {
				log.warn("KYC认证FACE => 认证类型获取失败 请求traceId 为空. userId:{}", userId);
				throw new BusinessException(GeneralCode.COMMON_INCOMPLETE_INFO);
			}

			log.info("KYC认证FACE => 没有正在进行中的人脸识别流程，需要重新创建. userId:{} kycType:{}", userId, certificateKycType);
			FaceFlowInitResult faceFlowInitResult = iUserFace.initFaceFlowByTransId(transId, userId, faceTransType,
					false, true);
			if (faceFlowInitResult == null) {
				log.info("KYC认证FACE => 建立人脸识别流成失败. userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			kycCertificate.setFaceStatus(KycCertificateStatus.PROCESS.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateFaceStatus(kycCertificate);
			return faceFlowInitResult;
		}

		if (TransFaceLogStatus.REVIEW == faceLog.getStatus()) {
			// 当前的人脸识别流程在审核中，修改当前人脸识别状态到审核中，并且通知调用方说正在审核中
			log.info("KYC认证FACE => 人脸识别正在审核中, 不能再建立. userId:{}", userId);
			kycCertificate.setFaceStatus(KycCertificateStatus.REVIEW.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateFaceStatus(kycCertificate);
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_REVIEW);
		}

		FaceFlowInitResult faceFlowInitResult = new FaceFlowInitResult();
		faceFlowInitResult.setType(faceTransType.getCode());
		faceFlowInitResult.setTransId(faceLog.getTransId());
		return faceFlowInitResult;

	}
}
