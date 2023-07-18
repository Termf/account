package com.binance.account.vo.device.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class CompareDeviceRequestV2 {

    @ApiModelProperty(required = true, notes = "device content 1")
    @NotNull
    private Map<String, String> content1;

    @ApiModelProperty(required = true, notes = "device content 2")
    @NotNull
    private Map<String, String> content2;

    @NotBlank
    private String agentType;
}
