package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.binance.account.common.enums.AgentRewardEnum;
import com.binance.account.common.enums.RestoreEnum;
import com.binance.master.commons.Page;
import com.binance.master.enums.OrderByEnum;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户返佣审核列表Request")
@Getter
@Setter
public class AgentRewardAuditRequest extends Page implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8430154057947181921L;

	@ApiModelProperty(required = false, notes = "用户Id")
	private Long userId;

	@ApiModelProperty(required = false, notes = "邮箱")
	private String email;
	
	@ApiModelProperty(required = false, notes = "批次号")
    private String batchId;
	
	@ApiModelProperty(required = false, notes = "审核状态")
    private AgentRewardEnum status;
	
	@ApiModelProperty(required = false, notes = "是否按批次号审核)")
    private String batch;
	
	@ApiModelProperty(required = false, notes = "是否预计恢复时间<n:否;y:是>")
    private RestoreEnum isRestore;
	
	@ApiModelProperty(required = false, notes = "申请开始时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startApplyTime;
	
	@ApiModelProperty(required = false, notes = "申请结束时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endApplyTime;
	
	@ApiModelProperty(required = false, notes = "操作开始时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date startOperatorTime;
	
	@ApiModelProperty(required = false, notes = "操作结束时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date endOperatorTime;
	
	@ApiModelProperty(required = false, notes = "修改原因")
    private String reason;
	
	@ApiModelProperty(required = false, notes = "排序字段")
	private String sort;

	@ApiModelProperty(required = false, notes = "排序")
	private OrderByEnum order;
}
