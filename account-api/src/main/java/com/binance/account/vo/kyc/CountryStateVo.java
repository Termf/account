package com.binance.account.vo.kyc;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CountryStateVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1337497024297914639L;
	
	private String code;

	private String stateCode;

	private String en;

	private String cn;

	private String nationality;

	private boolean enable;

}
