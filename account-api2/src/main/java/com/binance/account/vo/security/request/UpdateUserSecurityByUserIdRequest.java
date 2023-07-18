package com.binance.account.vo.security.request;

import com.binance.account.vo.security.UserSecurityVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("根据UserId更新UserSecurity")
@Getter
@Setter
public class UpdateUserSecurityByUserIdRequest {

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("用户安全相关信息")
    @NotNull
    private UserSecurityVo userSecurity;

    public UserSecurityVo getUserSecurity() {
        userSecurity.setUserId(userId);
        return userSecurity;
    }

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
