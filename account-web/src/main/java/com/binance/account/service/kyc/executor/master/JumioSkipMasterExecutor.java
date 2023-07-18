package com.binance.account.service.kyc.executor.master;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFlowType;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.security.IFace;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Log4j2
@Service
/**
 * 锁定单条jumio记录
 *
 * @author liufeng
 *
 */
public class JumioSkipMasterExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private JumioBusiness jumioBusiness;
	@Resource
	private IFace iFace;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		if (!kycFlowRequest.isLockJumio()) {
			return null;
		}

		JumioInfoVo jumioInfoVo = jumioBusiness.reuseCurrentJumio(kycFlowRequest.getUserId());

		if (jumioInfoVo == null) {
			return null;
		}

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(jumioInfoVo.getUserId());

		if (kycCertificate == null) {
			return null;
		}

		log.info("当前存在jumio记录,jumio状态处于pending或成功,可以直接跳过jumio做人脸. userId:{},jumioStatus:{}",
				kycFlowRequest.getUserId(), jumioInfoVo.getStatus());
		return validCanDoFace(jumioInfoVo,kycCertificate);

	}

	private JumioInitResponse validCanDoFace(JumioInfoVo jumioInfoVo,KycCertificate kycCertificate) {

		Long userId = jumioInfoVo.getUserId();

		UserFaceReference userFaceReference = iFace.getUserFaceByMasterBD(userId);

		if (userFaceReference == null
				|| StringUtils.isAllBlank(userFaceReference.getRefImage(), userFaceReference.getCheckImage())) {
			log.info("当前存在jumio记录,但jumio人脸照不能用于face，需重做jumio. userId:{}", userId);
			return null;
		}

		KycCertificateKycType kycType = KycCertificateKycType.getByCode(kycCertificate.getKycType());

		if(JumioStatus.PASSED.equals(jumioInfoVo.getStatus())) {
			kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name()); // todo 是否直接显示SKIP状态
			kycCertificate.setJumioTips(jumioInfoVo.getFailReason());
			kycCertificate.setBaseFillStatus(KycCertificateStatus.PASS.name());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificate.setLockOne(true);
			kycCertificateMapper.updateJumioAndBaseStatus(kycCertificate);
		}else {
			kycCertificate.setLockOne(true);
			kycCertificateMapper.updateLockOne(kycCertificate);
		}




		JumioInitResponse response = new JumioInitResponse();
        response.setUserId(userId);
        response.setKycType(kycType);

        KycFlowContext.getContext().setKycFlowType(KycFlowType.SKIP_JUMIO);
        KycFlowContext.getContext().setKycCertificate(kycCertificate);
        return response;
	}

}
