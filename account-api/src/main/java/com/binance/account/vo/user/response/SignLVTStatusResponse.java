package com.binance.account.vo.user.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel("lvt签约状态查询 Response")
public class SignLVTStatusResponse implements Serializable {

    private static final long serialVersionUID = -4122110745955432215L;
    
    @ApiModelProperty(value = "userId")
    private Long userId;

    @ApiModelProperty(value = "是否签署了lvt协议")
    private Boolean isSignedLVTRiskAgreement;

}
