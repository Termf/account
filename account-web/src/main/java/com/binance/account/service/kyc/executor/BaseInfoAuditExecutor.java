package com.binance.account.service.kyc.executor;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.KycFLowExecutorHelper;
import com.binance.account.service.kyc.validator.BaseInfoAuditValidator;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.request.ChangeStatusRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;

@Service
public class BaseInfoAuditExecutor extends AbstractKycFlowCommonExecutor {

	@Autowired
	private BaseInfoAuditValidator validator;

	@Resource
	private KycFLowExecutorHelper kycFLowExecutorHelper;

	@Resource
	private JumioBusiness jumioBusiness;

	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		KycAuditRequest req = (KycAuditRequest) kycFlowRequest;

		validator.validateApiRequest(req);

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(req.getUserId());

		validator.validateKycCertificateStatus(kycCertificate, req.getKycCertificateStatus());

		KycCertificate record = new KycCertificate();
		record.setUserId(kycCertificate.getUserId());
		record.setBaseFillStatus(req.getKycCertificateStatus().name());
		record.setBaseFillTips(StringUtils.isBlank(req.getTips()) ? "" : req.getTips());
		record.setUpdateTime(DateUtils.getNewUTCDate());

		
		//非拒绝流程 直接更新base状态
		if(!KycCertificateStatus.REFUSED.equals(req.getKycCertificateStatus())) {
			kycCertificateMapper.updateByPrimaryKeySelective(record);	
			new KycFlowResponse(); 
		}
		
		// address状态：如果地址状态不为空，并且不等于REFUSED。直接更新address状态
		if (StringUtils.isNotBlank(kycCertificate.getAddressStatus())
				&& !KycCertificateStatus.REFUSED.name().equals(kycCertificate.getAddressStatus())) {
			record.setAddressStatus(req.getKycCertificateStatus().name());
			record.setAddressTips(req.getTips());
		}
		// 绑定手机
		if (StringUtils.isNotBlank(kycCertificate.getBindMobile())) {
			record.setMobileCode("");
			record.setBindMobile("");
		}

		// JUMIO状态 & face 状态
		if (StringUtils.isNotBlank(kycCertificate.getJumioStatus())
				&& !KycCertificateStatus.REFUSED.name().equals(kycCertificate.getJumioStatus())) {
			record.setJumioStatus(req.getKycCertificateStatus().name());
			record.setJumioTips(req.getTips());
			record.setFaceStatus(kycCertificate.getFaceStatus());
			// 修改account的 transaction_face_log user_face_reference_view 信息
			kycFLowExecutorHelper.jumioChangeAfterHandler(record,
					KycCertificateStatus.valueOf(kycCertificate.getJumioStatus()));

			JumioInfoVo jumioInfoVo = jumioBusiness.getLastByUserId(kycCertificate.getUserId());

			if (jumioInfoVo == null) {
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			ChangeStatusRequest request = new ChangeStatusRequest();
			request.setJumioId(jumioInfoVo.getId());
			request.setUserId(jumioInfoVo.getUserId());
			request.setStatus(JumioStatus.REFUED);
			request.setFailReason(JumioError.ID_NUMBER_USED.name());
			request.setNotifyChange(false);
			jumioBusiness.changeJumioStatus(request);
		}

		// google 表单
		if (StringUtils.isNotBlank(kycCertificate.getGoogleFormStatus())
				&& !KycCertificateStatus.REFUSED.name().equals(kycCertificate.getGoogleFormStatus())) {
			record.setGoogleFormStatus(req.getKycCertificateStatus().name());
			record.setGoogleFormTips(req.getTips());
		}

		kycCertificateMapper.updateByPrimaryKeySelective(record);
		return new KycFlowResponse();

	}

}
