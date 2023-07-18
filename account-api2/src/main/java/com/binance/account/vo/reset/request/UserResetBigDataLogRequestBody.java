package com.binance.account.vo.reset.request;

import java.util.Date;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户2FA,大数据处理流水，查询请求")
@Getter
@Setter
public class UserResetBigDataLogRequestBody extends ToString{
	private static final long serialVersionUID = 5872781972085650138L;
	@ApiModelProperty("userId")
	private Long userId;
	@ApiModelProperty("reset流程id")
	private String transId;
	@ApiModelProperty("大数据处理开始时间")
	private Date startTime;
	@ApiModelProperty("大数据处理结束时间")
	private Date endTime;
	@ApiModelProperty("reset重置类型")
	private String resetType;
}
