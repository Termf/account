package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.SubUserInfoVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by mengjuan on 2018/10/26.
 */
@ApiModel(description = "子账户列表Response", value = "子账户列表Response")
@Getter
@Setter
public class SubUserInfoResp {

    private List<SubUserInfoVo> result;

    private Long count;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }

}
