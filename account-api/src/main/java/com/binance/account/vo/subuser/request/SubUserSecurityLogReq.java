package com.binance.account.vo.subuser.request;

import java.io.Serializable;
import java.util.Date;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.Page;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("子账户登录历史Request")
@Getter
@Setter
public class SubUserSecurityLogReq extends Page implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4960709008894647136L;
 
	@ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;
	
	@ApiModelProperty(required = false, notes = "子账户userId")
    private Long subUserId;
	
	@ApiModelProperty(required = false, notes = "操作类型")
    private String operateType;
	
	@ApiModelProperty(required = false, notes = "操作起始时间")
    private Date startOperateTime;
	
	@ApiModelProperty(required = false, notes = "操作结束时间")
    private Date endOperateTime;
	
}
