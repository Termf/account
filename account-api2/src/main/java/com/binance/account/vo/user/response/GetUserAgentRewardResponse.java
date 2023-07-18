package com.binance.account.vo.user.response;

import com.binance.account.vo.user.UserAgentRewardVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * Created by mengjuan on 2018/9/26.
 */
@ApiModel("获取用户返佣列表Response")
@Getter
@Setter
@NoArgsConstructor
public class GetUserAgentRewardResponse implements Serializable{

    private static final long serialVersionUID = -8866263400131461748L;

    private List<UserAgentRewardVo> result;

    private Long count;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
