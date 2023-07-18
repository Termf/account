package com.binance.account.vo.user.response;

import com.binance.account.vo.user.UserInfoVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("模糊匹配用户info信息Response")
@Data
@AllArgsConstructor
public class FuzzyMatchUserInfoResponse implements Serializable {

    private static final long serialVersionUID = 7361549902907628917L;
    private List<UserInfoVo> userInfoVoList;
    private Long total;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
