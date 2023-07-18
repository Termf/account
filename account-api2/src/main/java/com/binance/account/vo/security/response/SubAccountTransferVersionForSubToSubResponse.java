package com.binance.account.vo.security.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("SubAccountTransferVersionForSubToSubResponse")
@Getter
@Setter
@NoArgsConstructor
public class SubAccountTransferVersionForSubToSubResponse {

    @ApiModelProperty("事务操作id")
    private Long transactionId;//事务操作id

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}