package com.binance.account.vo.reset.request;

import java.util.Map;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("重置2FA查询问题参数")
@Setter
@Getter
public class ResetQuestionArg extends ToString {
	private static final long serialVersionUID = 1804186338002720482L;

	@ApiModelProperty("重置请求id")
	@NotNull
	private String requestId;
	
	@ApiModelProperty("重置id,用于校验")
	@NotNull
    private String transId;

	@ApiModelProperty("重置type,用于校验")
	@NotNull
    private String type;
	
	@ApiModelProperty("设备信息,中台封装")
	@NotNull
	Map<String, String> deviceInfo;
}
