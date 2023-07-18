package com.binance.account.service.kyc.executor;

import org.apache.commons.lang3.StringUtils;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.enums.LanguageEnum;
import lombok.Data;

@Data
public class KycEndContext {

	/**
	 * kyc 认证信息
	 */
	private KycCertificate kycCertificate;

	/**
	 * 返回结果
	 */
	private KycFlowResponse kycFlowResponse;

	private Long userId;

	/**
	 * kyc 认证类型
	 */
	private KycCertificateKycType kycType;

	/**
	 * jumio 认证信息
	 */
	private JumioInfoVo jumioInfoVo;

	/**
	 * ocr 认证信息
	 */
	private FaceIdCardOcrVo faceIdCardOcrVo;

	/**
	 * 是否 ocr 流程
	 */
	private boolean ocrFlow;

	/**
	 * country code
	 */
	private String country;

	private String idNumber;

	private String documentType;

	/**
	 * 是否不合规国籍
	 */
	private boolean forbidCountry;

	/**
	 * 语言
	 */
	private LanguageEnum language;

	/**
	 * 变更后的主状态
	 */
	private KycCertificateStatus toStatus;
	
	private UserKycApprove userKycApprove;

	private static final ThreadLocal<KycEndContext> LOCAL = new ThreadLocal<KycEndContext>() {
		@Override
		protected KycEndContext initialValue() {
			return new KycEndContext();
		}
	};
	
	public static KycEndContext getContext() {
		return LOCAL.get();
	}
	
	public static void clean() {
		LOCAL.remove();
	}

	/**
	 * 子状态是否存在某一状态
	 * @param subStatus
	 * @return
	 */
	public boolean hasSomeSubStatus(KycCertificateStatus subStatus) {
		if (this.getBasicStatus() == subStatus) {
			return true;
		}
		if (this.getJumioStatus() == subStatus) {
			return true;
		}
		if (this.getFaceOrcStatus() == subStatus) {
			return true;
		}
		if (this.getFaceStatus() == subStatus) {
			return true;
		}
		if (this.getGoogleFormStatus() == subStatus) {
			return true;
		}
		return false;
	}

	// 获取第一个拒绝对错误原因
	public String getFirstSubMessageTips() {
		String messageTips = this.getKycCertificate().getMessageTips(); // 默认
		if (this.getBasicStatus() == KycCertificateStatus.REFUSED && StringUtils.isNotBlank(this.getKycCertificate().getBaseFillTips())) {
			return this.getKycCertificate().getBaseFillTips();
		}
		if (this.isOcrFlow() && this.getFaceOrcStatus() == KycCertificateStatus.REFUSED && StringUtils.isNotBlank(this.getKycCertificate().getFaceOcrTips())) {
			return this.getKycCertificate().getFaceOcrTips();
		}
		if (!this.isOcrFlow() && this.getJumioStatus() == KycCertificateStatus.REFUSED && StringUtils.isNotBlank(this.getKycCertificate().getJumioTips())) {
			return this.getKycCertificate().getJumioTips();
		}
		if (this.getFaceStatus() == KycCertificateStatus.REFUSED && StringUtils.isNotBlank(this.getKycCertificate().getFaceTips())) {
			return this.getKycCertificate().getFaceTips();
		}
		if (KycCertificateKycType.COMPANY == this.getKycType() && this.getGoogleFormStatus() == KycCertificateStatus.REFUSED && StringUtils.isNotBlank(this.getKycCertificate().getGoogleFormTips())) {
			return this.getKycCertificate().getGoogleFormTips();
		}
		return messageTips;
	}

	// ---- 下面这些状态需要根据当前的 kycCertificate 获取，因为在处理过程中可能对这些状态作出变更
	public KycCertificateStatus getBasicStatus() {
		return KycCertificateStatus.getByName(this.kycCertificate.getBaseFillStatus());
	}

	public KycCertificateStatus getJumioStatus() {
		return KycCertificateStatus.getByName(this.kycCertificate.getJumioStatus());
	}

	public KycCertificateStatus getFaceOrcStatus() {
		return KycCertificateStatus.getByName(this.kycCertificate.getFaceOcrStatus());
	}

	public KycCertificateStatus getFaceStatus() {
		return KycCertificateStatus.getByName(this.kycCertificate.getFaceStatus());
	}

	public KycCertificateStatus getGoogleFormStatus() {
		return KycCertificateStatus.getByName(this.kycCertificate.getGoogleFormStatus());
	}
}
