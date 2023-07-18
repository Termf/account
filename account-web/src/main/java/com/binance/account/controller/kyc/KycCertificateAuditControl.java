package com.binance.account.controller.kyc;

import com.binance.account.api.kyc.KycCertificateAuditApi;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.query.TaxIdBlacklistQuery;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.service.certificate.ITaxId;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingContext;
import com.binance.account.service.kyc.KycCertificateService;
import com.binance.account.service.kyc.KycFlowProcessFactory;
import com.binance.account.service.kyc.KycFlowProcessor;
import com.binance.account.vo.certificate.TaxIdBlacklistVo;
import com.binance.account.vo.kyc.request.FaceAuthRequest;
import com.binance.account.vo.kyc.request.FaceOcrAuthRequest;
import com.binance.account.vo.kyc.request.KycAuditRequest;
import com.binance.account.vo.kyc.request.TaxIdBlacklistPushRequest;
import com.binance.account.vo.kyc.request.UpdateFaceOcrNameRequest;
import com.binance.account.vo.kyc.request.UpdateFillNameRequest;
import com.binance.master.commons.SearchResult;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@Log4j2
public class KycCertificateAuditControl implements KycCertificateAuditApi {

	@Resource
	private KycFlowProcessFactory kycFlowProcessFactory;

	@Resource
	private KycCertificateMapper kycCertificateMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private UserKycApproveMapper userKycApproveMapper;

	@Resource
	private KycFillInfoMapper kycFillInfoMapper;

	@Resource
	private IUserChannelRiskRating iUserChannelRiskRating;

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Resource
	private UserChannelRiskRatingContext userChannelRiskRatingContext;
	
	@Resource
    private KycCertificateService kycCertificateService;

	@Resource
	private ITaxId taxIdBusiness;
	

	@Override
	public APIResponse<Void> auditBaseInfo(@Validated @RequestBody APIRequest<KycAuditRequest> request) {
		kycCertificateService.auditBaseInfo(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> auditGoogleForm(@Validated @RequestBody APIRequest<KycAuditRequest> request) {
		kycCertificateService.auditGoogleForm(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> auditFaceOcr(@Validated @RequestBody APIRequest<FaceOcrAuthRequest> request) {
		kycCertificateService.auditFaceOcr(request.getBody());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> updateFaceOcrName(@Validated @RequestBody APIRequest<UpdateFaceOcrNameRequest> request) {
		UpdateFaceOcrNameRequest body = request.getBody();
		if (body == null || body.getUserId() == null) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		if (StringUtils.isBlank(body.getName())) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(body.getUserId());

		if (kycCertificate != null) {
			KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(body.getUserId(),
					KycFillType.BASE.name());
			if (kycFillInfo == null) {
				log.warn("当前kyc记录fill_info不存在 userId:{}", body.getUserId());
				throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
			}

			kycFillInfo.setFirstName(body.getName());
			kycFillInfo.setMiddleName(null);
			kycFillInfo.setLastName(null);
			kycFillInfoMapper.updateNameByUk(kycFillInfo);

			UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(body.getUserId());

			if (userKycApprove == null) {
				log.warn("当前kyc approve 记录不存在 userId:{}", body.getUserId());
				return APIResponse.getOKJsonResult();
			}

			userKycApprove.getBaseInfo().setFirstName(body.getName());
			userKycApprove.getBaseInfo().setMiddleName(null);
			userKycApprove.getBaseInfo().setLastName(null);

			userKycApproveMapper.updateOcrResult(userKycApprove);

			return APIResponse.getOKJsonResult();
		}

		UserKyc userKyc = userKycMapper.getLast(body.getUserId());

		if (userKyc == null) {
			log.warn("当前kyc记录不存在 userId:{}", body.getUserId());
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}

		userKyc.getBaseInfo().setFirstName(body.getName());
		userKyc.getBaseInfo().setMiddleName(null);
		userKyc.getBaseInfo().setLastName(null);

		userKycMapper.updateFillName(userKyc);

		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(body.getUserId());

		if (userKycApprove == null) {
			log.warn("当前kyc approve 记录不存在 userId:{}", body.getUserId());
			return APIResponse.getOKJsonResult();
		}

		userKycApprove.getBaseInfo().setFirstName(body.getName());
		userKycApprove.getBaseInfo().setMiddleName(null);
		userKycApprove.getBaseInfo().setLastName(null);

		userKycApproveMapper.updateOcrResult(userKycApprove);

		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> auditFace(@Validated @RequestBody APIRequest<FaceAuthRequest> request) {
		FaceAuthRequest body = request.getBody();
		kycFlowProcessFactory.getProcessor(KycFlowProcessor.PROCESSOR_KYC_FACE_AUTH_RESULT).process(body);
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> updateFillName(APIRequest<UpdateFillNameRequest> request) {

		UpdateFillNameRequest body = request.getBody();
		if (body == null || body.getUserId() == null || body.getFillType() == null) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		if (StringUtils.isBlank(body.getFirstName()) && StringUtils.isBlank(body.getMiddleName())
				&& StringUtils.isBlank(body.getLastName())) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}

		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(body.getUserId(), body.getFillType());
		if (kycFillInfo == null) {
			log.warn("当前kyc记录fill_info不存在 userId:{}", body.getUserId());
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}

		StringBuffer oldNameBuffered = new StringBuffer()
				.append(StringUtils.isBlank(kycFillInfo.getFirstName()) ? "" : kycFillInfo.getFirstName())
				.append(StringUtils.isBlank(kycFillInfo.getMiddleName()) ? "" : " " + kycFillInfo.getMiddleName())
				.append(StringUtils.isBlank(kycFillInfo.getLastName()) ? "" : " " + kycFillInfo.getLastName());
		StringBuffer newNameBuffered = new StringBuffer()
				.append(StringUtils.isBlank(body.getFirstName()) ? "" : body.getFirstName())
				.append(StringUtils.isBlank(body.getMiddleName()) ? "" : " " + body.getMiddleName())
				.append(StringUtils.isBlank(body.getLastName()) ? "" : " " + body.getLastName());

		kycFillInfo.setFirstName(body.getFirstName());
		kycFillInfo.setMiddleName(body.getMiddleName());
		kycFillInfo.setLastName(body.getLastName());
		kycFillInfoMapper.updateFillName(kycFillInfo);

		UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(body.getUserId());
		if (userKycApprove == null) {
			log.warn("当前kyc approve 记录不存在 userId:{}", body.getUserId());
			return APIResponse.getOKJsonResult();
		}

		userKycApprove.getBaseInfo().setFirstName(body.getFirstName());
		userKycApprove.getBaseInfo().setMiddleName(body.getMiddleName());
		userKycApprove.getBaseInfo().setLastName(body.getLastName());
		userKycApproveMapper.updateOcrResult(userKycApprove);

		// 姓名修改 触发wck上报
		if (!oldNameBuffered.toString().trim().equals(newNameBuffered.toString().trim())) {
			try {
				KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(body.getUserId());

				List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper
						.selectByUserId(kycCertificate.getUserId());
				if (ratings != null && !ratings.isEmpty()) {
					for (UserChannelRiskRating riskRating : ratings) {
						try {
							UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode
									.valueOf(riskRating.getChannelCode());
							userChannelRiskRatingContext.getRatingHandler(channelCode).applyThirdPartyRisk(riskRating,
									kycCertificate, kycFillInfo, userKycApprove);
						} catch (Exception e) {
							log.warn("Base上送三方风险评估异常. userId:{},riskRatingId:{},channelCode:{}",
									kycCertificate.getUserId(), riskRating.getId(), riskRating.getChannelCode(), e);
						}

					}
				}
			} catch (Exception e) {
				log.warn("Base上送worldCheck记录执行异常 userId:{}", body.getUserId(), e);
			}
		}

		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<SearchResult<TaxIdBlacklistVo>> queryTaxIdBlacklist(APIRequest<TaxIdBlacklistQuery> request) {
		return APIResponse.getOKJsonResult(taxIdBusiness.queryTaxIdBlacklist(request.getBody()));
	}

	@Override
	public APIResponse<Void> pushTaxIdToBlacklist( APIRequest<TaxIdBlacklistPushRequest> request) {
		TaxIdBlacklistPushRequest body = request.getBody();
		// taxId已在黑名单
		if (taxIdBusiness.getBlacklistByTaxId(body.getTaxId()) != null) {
			return APIResponse.getErrorJsonResult("taxId已存在");
		} else {
			taxIdBusiness.pushBlacklist(body.getTaxId(), body.getCreator(), body.getCreator());
		}
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Void> removeTaxIdFromBlacklist(String taxId) {
		if (taxIdBusiness.removeBlacklist(taxId) == 0) {
			return APIResponse.getErrorJsonResult("taxId删除失败");
		}
		return APIResponse.getOKJsonResult();
	}
}
