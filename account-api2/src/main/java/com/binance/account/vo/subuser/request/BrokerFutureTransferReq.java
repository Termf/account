package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 */
@ApiModel("BrokerFutureTransferReq")
@Data
public class BrokerFutureTransferReq {


    @NotNull
    private Long parentUserId;
    private Long fromId;
    private Long toId;
    //1: 永续合约，2: 交割合约
    @NotNull
    private Integer futuresType;

    private String clientTranId;
    @NotBlank
    private String asset;
    @NotNull
    private BigDecimal amount;

}