package com.binance.account.vo.device.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel(description = "查找最匹配设备请求", value = "查找最匹配设备请求")
@Getter
@Setter
@NoArgsConstructor
public class FindMostSimilarUserDeviceRequest extends ToString {

    @ApiModelProperty(required = true, notes = "device content")
    @NotNull
    private Map<String, String> content;

    @ApiModelProperty(required = true, notes = "user ID")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "agent type")
    @NotBlank
    private String agentType;

}
