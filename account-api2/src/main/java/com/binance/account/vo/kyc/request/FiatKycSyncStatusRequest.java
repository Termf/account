package com.binance.account.vo.kyc.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author liufeng
 *
 */
@Getter
@Setter
public class FiatKycSyncStatusRequest extends ToString {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4955983348728296399L;

	@NotNull
	private Long userId;

	@NotNull
	private String fiatPtStatus;

	@NotNull
	private String fiatPtTips;

}
