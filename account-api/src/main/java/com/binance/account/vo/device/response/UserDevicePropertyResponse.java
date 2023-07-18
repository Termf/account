package com.binance.account.vo.device.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;


/**
 * @author: caixinning
 * @date: 2018/05/09 11:16
 **/
@Getter
@Setter
public class UserDevicePropertyResponse extends ToString {
    private static final long serialVersionUID = 9063770998259499635L;

    private Long id;

    @ApiModelProperty(notes = "状态：1.开启 4.关闭")
    private Byte status;

    @ApiModelProperty(notes = "客户端类型：web,ios,android,pc,mac")
    private String agentType;

    @ApiModelProperty(notes = "属性名称")
    private String propertyName;

    @ApiModelProperty(notes = "属性key")
    private String propertyKey;

    @ApiModelProperty(required = true, notes = "权重（1~10）")
    private Integer propertyWeight;

    @ApiModelProperty(notes = "属性需满足的规则（选填）")
    private String propertyRule;

}
