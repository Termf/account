package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("ChangeMobileRequest")
@Getter
@Setter
public class ChangeMobileRequest {


    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("newMobileCode")
    @NotNull
    private String newMobileCode;

     @ApiModelProperty("newMobile")
     @NotNull
     private String newMobile;

    @ApiModelProperty("是否提出用户登录态")
    private boolean logoutUser = true;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
