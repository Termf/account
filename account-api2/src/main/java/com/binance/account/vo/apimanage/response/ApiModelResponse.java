package com.binance.account.vo.apimanage.response;

import java.util.Date;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.*;

@ApiModel
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiModelResponse extends ToString {

    private static final long serialVersionUID = -2097537566488269783L;

    @ApiModelProperty
    private String id;

    @ApiModelProperty
    private String userId;

    @ApiModelProperty
    private String email;

    @ApiModelProperty
    private Integer keyId;

    @ApiModelProperty
    private String apiKey;

    @ApiModelProperty
    private String apiName;

    @ApiModelProperty
    private String secretKey;

    @ApiModelProperty
    private String tradeIp;

    @ApiModelProperty
    private String withdrawIp;

    @ApiModelProperty
    private String ruleId;

    @ApiModelProperty
    private int status;

    @ApiModelProperty
    private boolean disableStatus;

    @ApiModelProperty
    private String info;

    @ApiModelProperty
    private Date createTime;

    @ApiModelProperty
    private Date updateTime;

    @ApiModelProperty
    private boolean enableWithdrawStatus;

    @ApiModelProperty
    private String withdrawVerifycode;

    @ApiModelProperty
    private Date withdrawVerifycodeTime;

    @ApiModelProperty
    private boolean withdraw;

    @ApiModelProperty
    private String uuid;

    @ApiModelProperty
    private boolean apiEmailVerify;

    @ApiModelProperty
    private Date createEmailSendTime;

    @ApiModelProperty
    private Integer smallEnforcedLimit;

    @ApiModelProperty
    private Integer bigEnforcedLimit;

    @ApiModelProperty
    private String type;

    @ApiModelProperty
    private String accountType;

}
