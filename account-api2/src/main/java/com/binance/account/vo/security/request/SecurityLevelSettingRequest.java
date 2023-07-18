package com.binance.account.vo.security.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/6/4.
 */
@ApiModel("安全级别设置Request")
@Data
public class SecurityLevelSettingRequest implements Serializable {

    private static final long serialVersionUID = -7101713965347094121L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("安全级别")
    @NotNull
    private Integer level;
}
