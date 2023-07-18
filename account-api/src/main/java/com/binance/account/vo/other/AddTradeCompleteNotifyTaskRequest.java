package com.binance.account.vo.other;

import com.binance.account.common.enums.CacheRefreshType;
import com.binance.account.common.enums.UserKycEmailNotifyType;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("US站添加交易满额邮件通知任务")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AddTradeCompleteNotifyTaskRequest extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7629302695056600375L;
	
	@ApiModelProperty("userId")
	private Long userId;
	
	@ApiModelProperty("type")
	private UserKycEmailNotifyType type;

}
