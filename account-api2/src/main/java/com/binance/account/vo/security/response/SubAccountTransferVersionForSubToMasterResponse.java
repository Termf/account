package com.binance.account.vo.security.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("SubAccountTransferVersionForSubToMasterResponse")
@Getter
@Setter
@NoArgsConstructor
public class SubAccountTransferVersionForSubToMasterResponse {

    @ApiModelProperty("事务操作id")
    private Long transactionId;//事务操作id

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}