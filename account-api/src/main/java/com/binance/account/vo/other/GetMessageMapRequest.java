package com.binance.account.vo.other;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("获取消息映射的Key")
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class GetMessageMapRequest implements Serializable {

    private static final long serialVersionUID = -1434747492365388580L;

    @ApiModelProperty("key")
    @NotNull
    private String code;
}
