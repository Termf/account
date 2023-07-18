package com.binance.account.vo.security.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class OneButtonDisableRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6615509751633978259L;
    
    @NotNull
    private Long userId;


    @ApiModelProperty("备注")
    private String remark;
    
}
