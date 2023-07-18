package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("解绑谷歌验证Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UnbindGoogleVerifyResponse extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 1566087286913632561L;

    @ApiModelProperty("用户id")
    private Long userId;

    @ApiModelProperty("一键禁用码")
    private String disableToken;

}
