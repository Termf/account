package com.binance.account.vo.other;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

import com.binance.account.common.enums.CacheRefreshType;

@ApiModel("清除消息缓存的请求参数")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class CleanLocalCacheRequest implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 5815886031445417502L;
	
	@ApiModelProperty("清楚缓存类型")
    private CacheRefreshType type;
}
