package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor
public class ResetGoogleRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -441295284848594968L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("是否为重置申请由风控确定是否修改解绑时间，如果true代表是不会去修改解绑时间的")
    private Boolean riskEngineRes = false;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
