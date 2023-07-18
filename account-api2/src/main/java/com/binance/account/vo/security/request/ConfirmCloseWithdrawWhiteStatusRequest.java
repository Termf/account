package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

@ApiModel("确认关闭提币白名单Request")
@Getter
@Setter
public class ConfirmCloseWithdrawWhiteStatusRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -1081493234856696502L;

    @ApiModelProperty(required = true, notes = "认证令牌")
    @NotEmpty
    private String token;


    @ApiModelProperty(required = true, notes = "userId")
    @NotNull()
    private Long userId;
}
