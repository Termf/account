package com.binance.account.vo.user.request;

import com.binance.account.vo.user.UserInfoVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("根据用户Id更新用户信息Request")
@Getter
@Setter
public class UpdateUserInfoByUserIdRequest implements Serializable {

    private static final long serialVersionUID = 1072365580207885416L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("用户信息")
    @NotNull
    private UserInfoVo userInfo;

    public UserInfoVo getUserInfo() {
        userInfo.setUserId(userId);
        return userInfo;
    }

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
