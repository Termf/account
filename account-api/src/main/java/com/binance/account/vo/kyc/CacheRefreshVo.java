package com.binance.account.vo.kyc;

import java.io.Serializable;

import com.binance.account.common.enums.CacheRefreshType;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CacheRefreshVo implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1575872375271671133L;
	
	private CacheRefreshType type;

}
