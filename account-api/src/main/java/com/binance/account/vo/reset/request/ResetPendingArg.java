package com.binance.account.vo.reset.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-02-18 18:28
 */
@Setter
@Getter
public class ResetPendingArg extends ToString {

    private static final long serialVersionUID = 3709628139279732456L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("重置类型")
    @NotNull
    private ResetType type;

    public enum ResetType {
        GOOGLE,
        MOBILE,
        ENABLE
    }
}
