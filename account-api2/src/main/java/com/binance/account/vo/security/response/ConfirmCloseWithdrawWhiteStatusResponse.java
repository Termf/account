package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("确认关闭提币白名单Response")
@Getter
@Setter
@NoArgsConstructor
public class ConfirmCloseWithdrawWhiteStatusResponse extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 6524986776635020057L;

    @ApiModelProperty(required = true, notes = "userId")
    private Long userId;

    public ConfirmCloseWithdrawWhiteStatusResponse(Long userId) {
        super();
        this.userId = userId;
    }


}
