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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

}
