package com.binance.account.vo.user.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
public class BaseDetailRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -5397464975128983570L;

    @ApiModelProperty(required = true, notes = "用戶Id")
    @NotNull
    private Long userId;

}
