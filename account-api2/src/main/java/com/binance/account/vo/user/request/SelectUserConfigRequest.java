package com.binance.account.vo.user.request;
 
import java.io.Serializable;
import javax.validation.constraints.NotNull;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
 @ApiModel("用户查询默认配置项Request")
@Getter
@Setter
public class SelectUserConfigRequest implements Serializable{
     /**
     * 
     */
    private static final long serialVersionUID = -1112402482086292230L;
    
    @ApiModelProperty(required = true, notes = "用户Id")
    @NotNull
    private Long userId;
    
    @ApiModelProperty(name = "配置项类型名", required = false)
    private String configType;
    
    @ApiModelProperty(name = "排除项", required = false)
    private String exclude;
 }