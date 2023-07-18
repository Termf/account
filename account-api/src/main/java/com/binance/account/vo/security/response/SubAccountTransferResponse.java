package com.binance.account.vo.security.response;

import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@ApiModel("子母账户划转Response")
@Getter
@Setter
@NoArgsConstructor
public class SubAccountTransferResponse implements Serializable {

    private static final long serialVersionUID = -2632238652751866913L;
    @ApiModelProperty("事务操作id")
    private Long transactionId;//事务操作id

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
