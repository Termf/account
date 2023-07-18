package com.binance.account.vo.security.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ApiModel("大户ip变更确定Response")
@Getter
@Setter
@ToString
@NoArgsConstructor
public class ConfirmedUserIpChangeResponse implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = -1673236487984730439L;
    @ApiModelProperty(value="userId",required=true)
    private Long userId;

    public ConfirmedUserIpChangeResponse(Long userId) {
        super();
        this.userId = userId;
    }
    
}
