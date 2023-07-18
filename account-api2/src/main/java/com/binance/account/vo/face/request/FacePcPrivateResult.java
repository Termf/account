package com.binance.account.vo.face.request;

import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FacePcPrivateResult extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7247642772680333490L;
	
	private String transId;
	
	private String faceTransType;
	
	private String bizNo;
	
	private boolean success;
	
	private String errorMessage;

}
