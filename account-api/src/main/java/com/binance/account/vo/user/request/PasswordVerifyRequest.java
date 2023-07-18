package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel("密码验证Request")
@Getter
@Setter
public class PasswordVerifyRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("密码")
    @NotEmpty
    private String password;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
