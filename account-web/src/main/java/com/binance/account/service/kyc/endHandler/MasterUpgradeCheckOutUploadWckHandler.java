package com.binance.account.service.kyc.endHandler;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.service.certificate.IUserChannelRiskRating;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingContext;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.vo.certificate.UserChannelRiskRatingVo;
import com.binance.master.utils.StringUtils;

import lombok.extern.log4j.Log4j2;

/**
 * risk rating 触发上送WorldCheck
 * 
 * @author liufeng
 *
 */
@Log4j2
@Service
public class MasterUpgradeCheckOutUploadWckHandler extends AbstractEndHandler {

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;
	
	@Resource
	private UserChannelRiskRatingContext userChannelRiskRatingContext;

	@Override
	public boolean isDoHandler() {
		KycEndContext context = KycEndContext.getContext();
		KycCertificate kycCertificate = context.getKycCertificate();
		UserKycApprove userKycApprove = context.getUserKycApprove();
		if (kycCertificate == null || userKycApprove == null) {
			log.info("kyc升级触发checkOut渠道上送kycCertificate、userKycApprovew为空");
			return false;
		}
		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(kycCertificate.getUserId());
		if (ratings == null || ratings.isEmpty()) {
			log.info("kyc升级触发checkOut渠道上送riskRating为空 userId:{}", kycCertificate.getUserId());
			return false;
		}

		if (!KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
			log.info("kyc升级触发checkOut渠道上送kyc状态不为PASS. userId:{}", kycCertificate.getUserId());
			return false;
		}
		log.info("kyc升级触发checkOut渠道上送worldCheck. userId:{}", kycCertificate.getUserId());
		return true;

	}

	@Override
	public void handler() {
		UserKycApprove userKycApprove = KycEndContext.getContext().getUserKycApprove();
		KycCertificate kycCertificate = KycEndContext.getContext().getKycCertificate();
		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(kycCertificate.getUserId(), KycFillType.BASE.name());
		
		if(userKycApprove == null) {
			return;
		}
		
		try {
			List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(userKycApprove.getUserId());

			if (ratings == null || ratings.isEmpty()) {
				log.info("kyc通过上送worldCheck记录riskRating不存在. userId:{}", userKycApprove.getUserId());
				return;
			}
			for (UserChannelRiskRating riskRating : ratings) {
				try {
					UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());
					userChannelRiskRatingContext.getRatingHandler(channelCode).applyThirdPartyRisk(riskRating, kycCertificate, kycFillInfo, userKycApprove);
				}catch(Exception e) {
					log.warn("kyc上送三方风险评估异常. userId:{},riskRatingId:{},channelCode:{}", kycCertificate.getUserId(),riskRating.getId(),riskRating.getChannelCode(),e);
				}
				
			}
		} catch (Exception e) {
			log.warn("kyc上送三方风险评估异常异常. userId:{}",
					kycCertificate.getUserId(), e);
		}
	}

}
