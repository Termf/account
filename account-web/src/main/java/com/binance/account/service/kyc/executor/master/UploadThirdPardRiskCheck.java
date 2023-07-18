package com.binance.account.service.kyc.executor.master;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.certificate.impl.UserWckBusiness;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingContext;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UploadThirdPardRiskCheck extends AbstractKycFlowCommonExecutor {

	@Resource
	private IUserChannelRiskRating iUserChannelRiskRating;

	@Resource
	private UserWckBusiness userWckBusiness;

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;
	
	@Resource
	private UserChannelRiskRatingContext userChannelRiskRatingContext;

	@Override
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {

		KycFlowResponse response = KycFlowContext.getContext().getKycFlowResponse();

		if (!KycFlowContext.getContext().isNeedWordCheck()) {
			return response;
		}

		KycCertificate kyc = KycFlowContext.getContext().getKycCertificate();

		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(kyc.getUserId());

		if (ratings == null || ratings.isEmpty()) {
			log.info("Base上送worldCheck记录riskRating不存在. userId:{}", kycFlowRequest.getUserId());
			return response;
		}
		KycFillInfo kycFillInfo = KycFlowContext.getContext().getKycFillInfo();
		
		for (UserChannelRiskRating riskRating : ratings) {
			try {
				UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());
				userChannelRiskRatingContext.getRatingHandler(channelCode).applyThirdPartyRisk(riskRating, kyc, kycFillInfo, null);
			}catch(Exception e) {
				log.warn("Base上送三方风险评估异常. userId:{},riskRatingId:{},channelCode:{}", kyc.getUserId(),riskRating.getId(),riskRating.getChannelCode(),e);
			}
			
		}
		return response;
	}

}
