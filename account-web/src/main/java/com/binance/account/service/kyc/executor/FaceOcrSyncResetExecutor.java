package com.binance.account.service.kyc.executor;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.mq.ResetJumioInfoMsgExecutor;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.reset2fa.IReset2Fa;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;

import groovy.util.logging.Log4j2;

@Service
@Log4j2
public class FaceOcrSyncResetExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	IReset2Fa iReset2Fa;
	
	@Resource
	ResetJumioInfoMsgExecutor resetJumioInfoMsgExecutor;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		KycFlowResponse response = KycFlowContext.getContext().getKycFlowResponse();

		boolean needSync = false;

		UserKyc userKyc = KycFlowContext.getContext().getUserKyc();

		FaceIdCardOcrVo faceIdCardOcrVo = new FaceIdCardOcrVo();
		String status = null;

		Long userId = 0l;
		if (userKyc != null) {
			needSync = IdCardOcrStatus.PASS.name().equals(userKyc.getFaceOcrStatus())
					|| IdCardOcrStatus.REFUSED.name().equals(userKyc.getFaceOcrStatus());
			status = userKyc.getFaceOcrStatus();
			userId = userKyc.getUserId();
		}

		KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();

		if (kycCertificate != null) {
			needSync = KycCertificateStatus.PASS.name().equals(kycCertificate.getFaceOcrStatus())
					|| KycCertificateStatus.REFUSED.name().equals(kycCertificate.getFaceOcrStatus());
			status = kycCertificate.getFaceOcrStatus();
			userId = kycCertificate.getUserId();
		}

		if (!needSync) {
			return response;
		}

		
		List<UserSecurityReset> resets = iReset2Fa.findJumioPendingResets(userId);
		
		if(resets == null || resets.isEmpty()) {
			return response;
		}
		
		faceIdCardOcrVo.setStatus(IdCardOcrStatus.valueOf(status));
		faceIdCardOcrVo.setFront(KycFlowContext.getContext().getFront());
		faceIdCardOcrVo.setBack(KycFlowContext.getContext().getBack());
		faceIdCardOcrVo.setFace(KycFlowContext.getContext().getFace());
		faceIdCardOcrVo.setIdcardNumber(KycFlowContext.getContext().getIdCardNumber());

		for (UserSecurityReset reset : resets) {
			AsyncTaskExecutor.execute(() -> {
				resetJumioInfoMsgExecutor.executorWithLockFromFaceOcr(reset, faceIdCardOcrVo);
			});
		}

		return response;
	}

}
