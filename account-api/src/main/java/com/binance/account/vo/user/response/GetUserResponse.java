package com.binance.account.vo.user.response;

import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("获取用户信息Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class GetUserResponse implements Serializable {

    private static final long serialVersionUID = 8709805917749346913L;

    private UserVo user;
    private UserSecurityVo userSecurity;
    private UserInfoVo userInfo;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
