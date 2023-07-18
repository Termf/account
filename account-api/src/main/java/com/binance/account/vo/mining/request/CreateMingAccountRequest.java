package com.binance.account.vo.mining.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("CreateMingAccountRequest")
@Getter
@Setter
public class CreateMingAccountRequest {
    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
