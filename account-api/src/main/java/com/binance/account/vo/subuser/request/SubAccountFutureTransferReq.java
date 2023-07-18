package com.binance.account.vo.subuser.request;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 */
@ApiModel("SubAccountFutureTransferReq")
@Data
public class SubAccountFutureTransferReq {


    @NotNull
    private Long parentUserId;
    private String fromEmail;
    private String toEmail;
    //1: 永续合约，2: 交割合约
    @NotNull
    private Integer futuresType;

    private String clientTranId;
    @NotBlank
    private String asset;
    @NotNull
    private BigDecimal amount;

}