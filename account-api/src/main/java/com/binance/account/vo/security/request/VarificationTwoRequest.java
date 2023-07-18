package com.binance.account.vo.security.request;

import com.binance.master.enums.AuthTypeEnum;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("验证手机验证码/谷歌验证码Request")
@Getter
@Setter
public class VarificationTwoRequest implements Serializable {

    private static final long serialVersionUID = 4337520945072011095L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;
    
    @ApiModelProperty("验证类型")
    @NotNull
    private AuthTypeEnum authType;
    
    @ApiModelProperty("验证码")
    @NotNull
    private String code;
    
    @ApiModelProperty("是否删除redis")
    @NotNull
    private Boolean autoDel; 

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
    
    
}
