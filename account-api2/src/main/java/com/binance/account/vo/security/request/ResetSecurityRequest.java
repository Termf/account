package com.binance.account.vo.security.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/6/5.
 */
@ApiModel("重置用户二次验证Request")
@Data
public class ResetSecurityRequest {

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("重置类型")
    @NotNull
    private ResetType resetType;

    public enum ResetType {
        GOOGLE,
        MOBILE,
        ENABLE
    }
}
