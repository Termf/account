package com.binance.account.vo.security.response;

import com.binance.account.vo.security.CapitalWithdrawVerifyParam;
import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel("GetCapitalWithdrawVerifyParamResponse")
@Getter
@Setter
public class GetCapitalWithdrawVerifyParamResponse extends ToString {
    private CapitalWithdrawVerifyParam emailParam;

    private CapitalWithdrawVerifyParam smsParam;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
