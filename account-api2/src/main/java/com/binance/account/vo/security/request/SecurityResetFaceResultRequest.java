package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-08-27 17:32
 */
@ApiModel("重置2FA 解析Face++的回调结果")
@Setter
@Getter
public class SecurityResetFaceResultRequest implements Serializable {


    private static final long serialVersionUID = 4467623129022755369L;

    @ApiModelProperty("face++的回调数据")
    @NotNull
    private String data;

    @ApiModelProperty("face++的回调数据的数字签名")
    @NotNull
    private String sign;


}
