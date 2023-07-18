package com.binance.account.vo.user.request;

import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotBlank;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("UserIpRequest")
@Getter
@Setter
public class UserIpRequest extends ToString{

    /**
     * 
     */
    private static final long serialVersionUID = 3490358083866937165L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;
    
    @ApiModelProperty("客户端IP")
    @NotBlank
    private String ip;
}
