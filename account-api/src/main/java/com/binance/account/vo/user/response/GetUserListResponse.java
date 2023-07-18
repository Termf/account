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
import java.util.List;

/**
 * @author lufei
 * @date 2018/5/7
 */
@ApiModel("批量获取用户信息Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class GetUserListResponse implements Serializable {

    private static final long serialVersionUID = 3548923189596216996L;

    private List<UserVo> users;
    private List<UserSecurityVo> userSecuritys;
    private List<UserInfoVo> userInfos;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }

}
