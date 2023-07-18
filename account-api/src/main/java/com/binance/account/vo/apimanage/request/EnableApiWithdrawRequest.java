package com.binance.account.vo.apimanage.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

@ApiModel
@Getter
@Setter
public class EnableApiWithdrawRequest extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = 5486441949374972912L;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String verifyCode;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String userId;
}
