package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
@ApiModel("CreateMarginAccountRequest")
@Getter
@Setter
public class CreateMarginAccountRequest {
    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("母账号userId")
    private Long parentUserId;

    @ApiModelProperty("是否需要同步margin")
    private Boolean needSyncMargin=true;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
