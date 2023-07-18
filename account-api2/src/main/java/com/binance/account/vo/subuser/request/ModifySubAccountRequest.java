package com.binance.account.vo.subuser.request;

import com.binance.master.enums.AuthTypeEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("母账户修改子账户邮箱Request")
@Getter
@Setter
public class ModifySubAccountRequest implements Serializable{
    private static final long serialVersionUID = -346225289427422921L;
    @ApiModelProperty(required = true, notes = "母账号userId")
    @NotNull
    private Long parentUserId;

    @ApiModelProperty(required = true, notes = "子账号userId")
    @NotNull
    private Long subAccountUserId;

    @ApiModelProperty(required = true, notes = "需要修改的邮箱")
    @NotNull
    private String modifyEmail;

    @ApiModelProperty(required = true, notes = "认证类型")
    @NotNull
    private AuthTypeEnum authType;

    @ApiModelProperty(required = true, notes = "2次验证码")
    @NotNull
    private String code;

}
