package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Getter
@Setter
public class CountTodaysUserOperationLogsRequest implements Serializable {

    private static final long serialVersionUID = 4678182145374634543L;

    @ApiModelProperty("操作")
    @NotBlank
    private String operation;

    @ApiModelProperty("用户ip")
    private String ip;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

}
