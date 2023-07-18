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
public class EnableApiCreateRequest extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = -5064769854280230822L;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String uuid;

    @ApiModelProperty(required = true)
    @NotEmpty
    private String userId;
}
