package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * Created by Fei.Huang on 2018/6/4.
 */
@ApiModel("修改用户Request")
@Data
public class ModifyUserEmailRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("用户邮箱")
    @NotEmpty
    private String email;

    @ApiModelProperty("用户新密码")
    @NotEmpty
    private String newPassword;

    @ApiModelProperty(required = true, notes = "新算法的密码")
    private String newSafePassword;



}
