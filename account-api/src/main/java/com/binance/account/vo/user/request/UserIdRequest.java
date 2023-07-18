package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "登录失败次数记录Request", value = "登录失败次数记录Request")
@Getter
@Setter
public class UserIdRequest extends ToString{

    private static final long serialVersionUID = 8611399561586960269L;
    
    @ApiModelProperty(required = false, notes = "是否走大户保护逻辑")
    private String userId;
    private String userType;
     
       
}
