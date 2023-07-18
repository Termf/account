package com.binance.account.service.kyc.executor.us;

import com.binance.account.common.enums.KycCertificateKycLevel;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateNextStep;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.GetKycStatusResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
@Log4j2
public class CurrentKycStatusUsExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private JumioApi jumioApi;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		Long userId = kycFlowRequest.getUserId();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate == null) {
			// 如果没有做过KYC认证，直接返回Level0的级别，其他不返回
			GetKycStatusResponse result = new GetKycStatusResponse();
			result.setKycLevel(KycCertificateKycLevel.L0.getCode());
			return result;
		}
		// 合规要求，address 如果pass或者refused，在短时间内还是告诉用户review
		if (KycCertificateKycLevel.L1.getCode().equals(kycCertificate.getKycLevel())) {
			if (KycCertificateStatus.PASS.name().equals(kycCertificate.getAddressStatus())
					|| KycCertificateStatus.REFUSED.name().equals(kycCertificate.getAddressStatus())) {
				KycFillInfo address = kycFillInfoMapper.selectByUserIdFillType(kycCertificate.getUserId(),
						KycFillType.ADDRESS.name());
				if (address != null && DateUtils.getNewUTCDate().compareTo(address.getUpdateTime()) < 0) {
					kycCertificate.setAddressStatus(KycCertificateStatus.REVIEW.name());
					kycCertificate.setAddressTips("");
				}
			}
		}

		GetKycStatusResponse response = KycCertificateConvertor.convert2GetKycStatusResponse(kycCertificate,
				WebUtils.getAPIRequestHeader().getLanguage());

		boolean doFace = false;

		if (KycCertificateStatus.SKIP.name().equals(kycCertificate.getFaceStatus())) {
			doFace = false;
		} else {
			doFace = config.isKycJumioFaceSwitch();
		}
		response.setKycFaceSwitch(doFace);

		// L2 不返回nextStep
		if (KycCertificateKycLevel.L2.getCode().equals(response.getKycLevel())) {
			KycFillInfo baseInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
			if(baseInfo != null ) {
				KycFillInfoVo baseVo = new KycFillInfoVo();
				BeanUtils.copyProperties(baseInfo, baseVo);
				response.setBase(baseVo);
			}
			return response;
		}

		// 用户L0 不反悔 nextStep
		if (KycCertificateKycLevel.L0.getCode().equals(response.getKycLevel())) {
			switch (KycCertificateKycType.getByCode(kycCertificate.getKycType())) {
			case USER:
				if (StringUtils.equalsAny(kycCertificate.getBaseFillStatus(), KycCertificateStatus.REVIEW.name(),
						KycCertificateStatus.PASS.name())) {
					return response;
				}
				response.setNextStep(KycCertificateNextStep.BASE);
				return response;
			case COMPANY:
				if (StringUtils.equalsAny(kycCertificate.getBaseFillStatus(), KycCertificateStatus.REFUSED.name(),
						KycCertificateStatus.PROCESS.name())) {
					response.setNextStep(KycCertificateNextStep.BASE);
					return response;
				}
				break;
			default:
				break;
			}
		}

		String jumioStatus = kycCertificate.getJumioStatus();

		// jumio 为PROCESS 判断jumio 是否已经做了
		if (jumioStatus != null && StringUtils.equalsAny(jumioStatus, KycCertificateStatus.PROCESS.name(),
				KycCertificateStatus.REVIEW.name())) {
			APIResponse<JumioInfoVo> resp = jumioApi.getLastJumio(APIRequest.instance(new Long(userId)));
			if (resp.getStatus() != APIResponse.Status.OK || resp.getData() == null) {
				log.error("查询inspector的jumio信息异常 userId:{}", userId);
				throw new BusinessException(GeneralCode.SYS_ERROR);
			}
			JumioInfoVo jumioVo = resp.getData();
			// jumioVo 为空或者 scan_reference为空，代表jumio没做
			if (jumioVo == null || StringUtils.isBlank(jumioVo.getScanReference())) {
				response.setNextStep(KycCertificateNextStep.JUMIO);
				return response;
			}

			// 判断jumio 是否为初始。ps 存在再次做jumio
			if (jumioVo == null || jumioVo.getStatus() == com.binance.inspector.common.enums.JumioStatus.INIT) {
				response.setNextStep(KycCertificateNextStep.JUMIO);
				return response;
			}

			//jumio 状态为review或者upload 重设jumioStatus为review
			if(com.binance.inspector.common.enums.JumioStatus.REVIEW.equals(jumioVo.getStatus())
					|| com.binance.inspector.common.enums.JumioStatus.UPLOADED.equals(jumioVo.getStatus())) {
				response.setJumioStatus(KycCertificateStatus.REVIEW.name());
			}

//			boolean faceSwitch = config.isKycFaceSwitch();
			String faceStatus = kycCertificate.getFaceStatus();

			// 正在进行中的情况，看下是否正在进行人脸识别
			if (doFace && StringUtils.isBlank(faceStatus)) {
				response.setNextStep(KycCertificateNextStep.FACE);
				return response;
			}
			if (doFace && StringUtils.equalsAny(faceStatus, KycCertificateStatus.PROCESS.name(),
					KycCertificateStatus.REFUSED.name())) {
				response.setNextStep(KycCertificateNextStep.FACE);
				return response;
			}
		}

		if (KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())) {
			String addressStatus = kycCertificate.getAddressStatus();

			if (StringUtils.isBlank(addressStatus) || StringUtils.equalsAny(addressStatus,
					KycCertificateStatus.REFUSED.name(), KycCertificateStatus.PROCESS.name())) {
				response.setNextStep(KycCertificateNextStep.ADDRESS);
				return response;

			}

			if (StringUtils.isBlank(kycCertificate.getMobileCode())) {
				response.setNextStep(KycCertificateNextStep.BINDMOBILE);
				return response;
			}
		}

		if (StringUtils.isBlank(jumioStatus)
				|| StringUtils.equalsAny(jumioStatus, KycCertificateStatus.REFUSED.name())) {
			response.setNextStep(KycCertificateNextStep.JUMIO);
			return response;
		}
		return response;
	}

}
