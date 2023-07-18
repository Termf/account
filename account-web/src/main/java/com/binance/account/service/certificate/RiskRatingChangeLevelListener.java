package com.binance.account.service.certificate;

import java.util.List;

import javax.annotation.Resource;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.binance.account.common.enums.UserRiskRatingChannelCode;
import com.binance.account.common.enums.UserRiskRatingStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.UserChannelRiskRating;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.UserChannelRiskRatingMapper;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingContext;
import com.binance.account.service.face.channel.risk.UserChannelRiskRatingHandlerParam;
import com.binance.platform.common.TrackingUtils;

import lombok.extern.log4j.Log4j2;

@Component
@Log4j2
public class RiskRatingChangeLevelListener {

	@Resource
	private IUserChannelRiskRating userChannelRiskRating;

	@Resource
	private KycCertificateMapper kycCertificateMapper;

	@Resource
	private UserChannelRiskRatingMapper userChannelRiskRatingMapper;

	@Resource
	private UserChannelRiskRatingContext userChannelRiskRatingContext;

	@EventListener
	public void onApplication(RiskRatingChangeLevelEvent event) {
		TrackingUtils.saveTrace(event.getTraceId());
		KycCertificate kycCertificate = event.getKycCertificate();
		if (event.getUserId() != null) {
			kycCertificate = kycCertificateMapper.selectByPrimaryKey(event.getUserId());
		}
		if (kycCertificate == null) {
			log.info("RiskRating 监听到event. kycCertificate为空");
			return;
		}

		log.info("RiskRating 监听到event. userId:{}", kycCertificate.getUserId());
		List<UserChannelRiskRating> ratings = userChannelRiskRatingMapper.selectByUserId(kycCertificate.getUserId());

		if (ratings == null || ratings.isEmpty()) {
			log.info("RiskRating变更tier等级记录为空 userId:{}", kycCertificate.getUserId());
			return;
		}

		for (UserChannelRiskRating riskRating : ratings) {
			try {
				UserRiskRatingChannelCode channelCode = UserRiskRatingChannelCode.getByCode(riskRating.getChannelCode());
				UserChannelRiskRatingHandlerParam param = new UserChannelRiskRatingHandlerParam();
				param.setUserChannelWckAuditVo(event.getUserChannelWckAuditVo());
				if(event.isAddressPush()) {
					log.info("RiskRating变更tier等级记录,address触发需要状态为ENABLE userId:{} status:{}", kycCertificate.getUserId(),riskRating.getStatus());
					if(UserRiskRatingStatus.ENABLE.name().equals(riskRating.getStatus())) {
						userChannelRiskRatingContext.getRatingHandler(channelCode).changeRiskRatingLevel(riskRating,
								kycCertificate, param);
					}
				}else {
					userChannelRiskRatingContext.getRatingHandler(channelCode).changeRiskRatingLevel(riskRating,
							kycCertificate, param);
				}
				
			} catch (Exception e) {
				log.warn("RiskRating变更tier等级 处理失败. userId:{} ratingId:{},channelCode:{}", kycCertificate.getUserId(),
						riskRating.getId(), riskRating.getChannelCode(), e);
			}
		}

	}
}
