package com.binance.account.vo.subuser.request;

import com.binance.account.vo.user.request.RegisterUserRequest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/10/9.
 */
@ApiModel("母账户注册子账户Request")
@Getter
@Setter
public class CreateSubUserReq extends RegisterUserRequest {

    /**
	 * 
	 */
	private static final long serialVersionUID = -2082660053991885452L;

	@ApiModelProperty(required = true, notes = "母账号UserId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = false, notes = "子账号备注")
    private String remark;

    @ApiModelProperty(name = "自定义邮件链接", required = false)
    private String customEmailLink;
}