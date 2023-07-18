package com.binance.account.vo.apimanage.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class UpdateApiKeyResponse extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = -3564226205776692210L;

    @ApiModelProperty
    private String userId;
    @ApiModelProperty(value = "前端提示信息，如果一起正常且发送了邮件则不为空")
    private String frontendTip;
}
