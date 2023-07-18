package com.binance.account.vo.user.request;

import java.io.Serializable;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.NotEmpty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户添加默认配置项Request")
@Getter
@Setter
public class SetUserConfigRequest implements Serializable{
     /**
     * 
     */
    private static final long serialVersionUID = -6322679704008443437L;
     @ApiModelProperty(name = "配置项类型名", required = true)
    @NotEmpty
    private String configType;
    
    @ApiModelProperty(name = "配置项名称值", required = true)
    @NotEmpty
    private String configName;
    
    @ApiModelProperty(required = true, notes = "用户Id")
    @NotNull
    private Long userId;
}