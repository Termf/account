package com.binance.account.vo.reset.request;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("重置2FA问题配置接口参数")
@Setter
@Getter
public class ResetQuestionConfigArg extends ToString {
	private static final long serialVersionUID = 2990154391184356427L;
	@ApiModelProperty("启/禁用:enable/disable")
	private String opType;
	@ApiModelProperty("问题id")
	private Long id;
}
