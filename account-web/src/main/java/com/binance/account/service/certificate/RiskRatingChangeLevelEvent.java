package com.binance.account.service.certificate;

import org.springframework.context.ApplicationEvent;

import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.vo.certificate.UserChannelWckAuditVo;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RiskRatingChangeLevelEvent extends ApplicationEvent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 4513369859861246269L;

	public RiskRatingChangeLevelEvent(Object source) {
		super(source);
	}
	
	private String traceId;
	
	private Long userId;
	
	private UserChannelWckAuditVo userChannelWckAuditVo;
	
	private KycCertificate kycCertificate;
	
	private boolean isAddressPush;

}
