package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
/**
 * 通用userId request
 */
@ApiModel(description = "通用userId Request", value = "通用userId Request")
@Getter
@Setter
public class IdRequest extends ToString{

    private static final long serialVersionUID = -6170926340623745582L;

    @ApiModelProperty(required = true, notes = "账号id")
    @NotNull
    private Long userId;


}
