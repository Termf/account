package com.binance.account.vo.security.response;

import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@ApiModel("获取用户安全日志Response")
@Getter
@Setter
@NoArgsConstructor
public class GetUserSecurityLogResponse implements Serializable {

    private static final long serialVersionUID = -3611888785263673288L;

    private List<UserSecurityLogVo> result;

    private Long count;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
