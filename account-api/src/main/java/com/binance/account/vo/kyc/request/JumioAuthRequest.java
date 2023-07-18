package com.binance.account.vo.kyc.request;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("Jumio状态变更处理")
@Setter
@Getter
public class JumioAuthRequest extends KycFlowRequest {


    @ApiModelProperty("jumio的状态")
    private String jumioStatus;

    @ApiModelProperty("jumio审核结果提示语")
    private String message;
    
    @ApiModelProperty("jumio审核业务流水")
    private String bizId;
    
    //新老迁移使用
    private Object jumioInfoVo;

}
