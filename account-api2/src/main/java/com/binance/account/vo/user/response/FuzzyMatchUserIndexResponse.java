package com.binance.account.vo.user.response;

import com.binance.account.vo.user.ex.UserIndexEx;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@ApiModel("模糊匹配用户index信息Response")
@Data
@AllArgsConstructor
public class FuzzyMatchUserIndexResponse implements Serializable {

    private static final long serialVersionUID = 3591507004210030519L;
    private List<UserIndexEx> userIndexExList;
    private Long total;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
