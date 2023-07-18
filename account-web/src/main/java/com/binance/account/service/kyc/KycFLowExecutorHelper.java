package com.binance.account.service.kyc;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.security.IFace;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Log4j2
public class KycFLowExecutorHelper {

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;
	@Resource
	private KycCertificateMapper kycCertificateMapper;
	@Resource
	protected IFace iFace;
	@Resource
	protected ApolloCommonConfig config;

	public void jumioChangeAfterHandler(KycCertificate kycCertificate, KycCertificateStatus oldJumoStatus) {
		/*
		 * 如果oldJumoStatus 是通过的状态，当前已经不是通过状态，原来在审核中，后来变成了拒绝, 需要把人脸识别的状态也重置掉，需要用户重新做人脸识别
		 */
		Long userId = kycCertificate.getUserId();
		KycCertificateStatus currJumioStatus = KycCertificateStatus.getByName(kycCertificate.getJumioStatus());

		if (config.isKycFaceSwitch() && KycCertificateStatus.REFUSED == currJumioStatus) {
			// 如果当前人脸识别状态不为空，先修改到拒绝状态
			kycCertificate.setFaceStatus(KycCertificateStatus.REFUSED.name());
			kycCertificate.setFaceTips(kycCertificate.getJumioTips());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificateMapper.updateFaceStatus(kycCertificate);
		}

		if ((KycCertificateStatus.PASS == oldJumoStatus && KycCertificateStatus.PASS != currJumioStatus)
				|| KycCertificateStatus.REFUSED == currJumioStatus) {

			if (StringUtils.isEmpty(kycCertificate.getFaceStatus())) {
				return;
			}

			// 只要JUMIO拒绝，都需要考虑把对应的人脸识别对比认证照片删除
//			if (!config.isKycJumioFaceSwitch()) {
//				return;
//			}
			log.info("KYC JUMIO AUTH => 原来JUMIO状态已经通过，但是审核后状态 {} 不为通过, userId:{}", currJumioStatus, userId);

			// 把人脸识别的正式照片设置到没有的情况
			int refImageRow = iFace.removeFaceReferenceRefImage(userId);
			log.info("KYC JUMIO AUTH => 删除人脸识别对比照片. userId:{} refImageRow:{}", userId, refImageRow);

			// 把正在进行中的人脸识别流程变更到拒绝状态
			KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
			FaceTransType faceTransType = kycType == KycCertificateKycType.USER ? FaceTransType.KYC_USER
					: FaceTransType.KYC_COMPANY;
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
			if (faceLog != null && !TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
				log.info("KYC JUMIO AUTH => 结束正在进行中的人脸识别流程. userId:{} faceLogId:{}", userId, faceLog.getId());
				TransactionFaceLog transactionFaceLog = new TransactionFaceLog();
				transactionFaceLog.setId(faceLog.getId());
				transactionFaceLog.setUserId(faceLog.getUserId());
				transactionFaceLog.setStatus(TransFaceLogStatus.FAIL);
				transactionFaceLog.setFailReason(kycCertificate.getJumioTips());
				transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
				transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
			}
		}
	}

	/**
	 * 当OCR 审核变化后，确认是否需要触发
	 * @param kycCertificate
	 * @param oldJumoStatus
	 */
	public void faceOcrChangeAfterHandler(KycCertificate kycCertificate, KycCertificateStatus oldJumoStatus) {
		Long userId = kycCertificate.getUserId();
		KycCertificateStatus currJumioStatus = KycCertificateStatus.getByName(kycCertificate.getFaceOcrStatus());

		if ((KycCertificateStatus.PASS == oldJumoStatus && KycCertificateStatus.PASS != currJumioStatus)
				|| KycCertificateStatus.REFUSED == currJumioStatus) {

			if (StringUtils.isEmpty(kycCertificate.getFaceStatus())) {
				return;
			}
//			if (!config.isKycOrcFaceSwitch()) {
//				return;
//			}

			log.info("KYC FACE OCR => 原来OCR状态已经通过，但是审核后状态 {} 不为通过, userId:{}", currJumioStatus, userId);
			if (!KycCertificateStatus.SKIP.name().equalsIgnoreCase(kycCertificate.getFaceStatus())) {
				kycCertificate.setFaceStatus(KycCertificateStatus.REFUSED.name());
				kycCertificate.setFaceTips(kycCertificate.getJumioTips());
				kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
				kycCertificateMapper.updateFaceStatus(kycCertificate);
			}

			// 把人脸识别的正式照片设置到没有的情况
			int refImageRow = iFace.removeFaceReferenceRefImage(userId);
			log.info("KYC FACE OCR => 删除人脸识别对比照片. userId:{} refImageRow:{}", userId, refImageRow);

			// 把正在进行中的人脸识别流程变更到拒绝状态
			KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());
			FaceTransType faceTransType = kycType == KycCertificateKycType.USER ? FaceTransType.KYC_USER
					: FaceTransType.KYC_COMPANY;
			TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, faceTransType.name(), null);
			if (faceLog != null && !TransFaceLogStatus.isEndStatus(faceLog.getStatus())) {
				log.info("KYC FACE OCR => 结束正在进行中的人脸识别流程. userId:{} faceLogId:{}", userId, faceLog.getId());
				TransactionFaceLog transactionFaceLog = new TransactionFaceLog();
				transactionFaceLog.setId(faceLog.getId());
				transactionFaceLog.setUserId(faceLog.getUserId());
				transactionFaceLog.setStatus(TransFaceLogStatus.FAIL);
				transactionFaceLog.setFailReason(kycCertificate.getJumioTips());
				transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
				transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
			}
		}
	}

}
