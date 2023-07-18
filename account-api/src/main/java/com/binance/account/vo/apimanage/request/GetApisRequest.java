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
public class GetApisRequest extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = 3688181103123602659L;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String userId;
}
