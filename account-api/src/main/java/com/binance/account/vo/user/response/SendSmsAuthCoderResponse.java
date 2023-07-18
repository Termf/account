package com.binance.account.vo.user.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel(description = "发送短信认证码Response", value = "发送短信认证码Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SendSmsAuthCoderResponse extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(readOnly = true, notes = "用户id")
    private Long userId;

    @ApiModelProperty(readOnly = true, notes = "认证码")
    private String code;

    @ApiModelProperty(readOnly = true, notes = "手机")
    private String mobile;

    @ApiModelProperty(readOnly = true, notes = "手机编码")
    private String mobileCode;

}
