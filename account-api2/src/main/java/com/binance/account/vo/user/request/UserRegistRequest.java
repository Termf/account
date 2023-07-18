package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import org.springframework.format.annotation.DateTimeFormat;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;

@ApiModel("统计注册量的request")
@Getter
@Setter
public class UserRegistRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 487216862653475120L;

	@ApiModelProperty("开始时间")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startTime;
	
	@ApiModelProperty("结束时间")
	@DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endTime;
}
