package com.binance.account.vo.apimanage.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class EnableApiWithdrawResponse extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = 6865178294012025932L;

    @ApiModelProperty
    private String type;
}
