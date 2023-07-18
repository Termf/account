package com.binance.account.vo.user.request;

import com.binance.account.vo.user.UserVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

@ApiModel("根据Email更新用户")
@Getter
@Setter
public class UpdateUserByEmailRequest implements Serializable {

    private static final long serialVersionUID = -1731805356602781000L;

    @ApiModelProperty("账号")
    @NotNull
    private String email;

    @ApiModelProperty("用户信息")
    @NotNull
    private UserVo user;

    public UserVo getUser() {
        if(!Objects.isNull(email)){
            user.setEmail(email.toLowerCase());
        }
        return user;
    }

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
