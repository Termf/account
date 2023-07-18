package com.binance.account.vo.card.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@ApiModel("CreateCardAccountRequest")
@Getter
@Setter
public class CreateCardAccountRequest {
    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
