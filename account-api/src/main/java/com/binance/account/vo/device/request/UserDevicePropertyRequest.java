package com.binance.account.vo.device.request;

import com.binance.master.commons.ToString;
import com.binance.master.validator.groups.Edit;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;

/**
 * @author: caixinning
 * @date: 2018/05/08 18:24
 **/

@ApiModel(description = "设备指纹属性request", value = "设备指纹属性request")
@Getter
@Setter
public class UserDevicePropertyRequest extends ToString {

    private static final long serialVersionUID = 6177284945429888216L;

    @NotNull(groups = Edit.class)
    private Long id;

    @ApiModelProperty(notes = "状态：1.开启 4.关闭")
    @NotNull(groups = Edit.class)
    private Byte status;

    @ApiModelProperty(required = true, notes = "客户端类型：web,ios,android,pc,mac")
    @NotNull
    private String agentType;

    @ApiModelProperty(required = true, notes = "属性名称")
    @NotEmpty
    private String propertyName;

    @ApiModelProperty(required = true, notes = "属性key")
    @NotEmpty
    private String propertyKey;

    @ApiModelProperty(required = true, notes = "权重（1~10）")
    @NotNull
    private Integer propertyWeight;

    @ApiModelProperty(notes = "属性需满足的规则（选填）")
    private String propertyRule;

}
