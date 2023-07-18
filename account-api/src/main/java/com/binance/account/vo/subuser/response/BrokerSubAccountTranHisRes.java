package com.binance.account.vo.subuser.response;

import java.io.Serializable;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("broker子母账户划转Response")
@Data
public class BrokerSubAccountTranHisRes  {

    private String fromId;
    private String toId;
    private String asset;
    private String qty;
    private Long time;
    private Long txnId;//事务操作id
    private String clientTranId;
}
