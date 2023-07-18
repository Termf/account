package com.binance.account.vo.apimanage.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel
@Getter
@Setter
public class GetApiListRequest extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = -7456891026580346802L;

    @ApiModelProperty
    private Long id;
    @ApiModelProperty
    private String userId;
    @ApiModelProperty
    private String apiKey;
    @ApiModelProperty
    private String apiName;
}
