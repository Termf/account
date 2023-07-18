package com.binance.account.vo.security.request;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotBlank;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;

@ApiModel("大户ip变更确定Request")
@Getter
@Setter
@ToString
public class ConfirmedUserIpChangeRequest implements Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 5567884736696441660L;

    @ApiModelProperty(value = "id", required = true)
    @NotBlank
    private String id;

    @ApiModelProperty(value = "userId", required = true)
    @NotNull
    private Long userId;

}
