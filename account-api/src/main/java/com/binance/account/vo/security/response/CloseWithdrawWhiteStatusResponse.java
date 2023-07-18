package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("关闭提币白名单Response")
@Getter
@Setter
@NoArgsConstructor
public class CloseWithdrawWhiteStatusResponse extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 7604768792964982970L;

    @ApiModelProperty(required = true, notes = "认证令牌")
    private String token;

    @ApiModelProperty(required = true, notes = "一键禁用认证令牌")
    private String disableToken;

    public CloseWithdrawWhiteStatusResponse(String token, String disableToken) {
        super();
        this.token = token;
        this.disableToken = disableToken;
    }

}
