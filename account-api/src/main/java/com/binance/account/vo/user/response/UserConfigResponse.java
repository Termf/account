package com.binance.account.vo.user.response;

import java.io.Serializable;
import java.util.Date;
import org.springframework.format.annotation.DateTimeFormat;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

 @ApiModel("用户默认配置Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserConfigResponse implements Serializable{
     /**
     * 
     */
    private static final long serialVersionUID = -3167276093961258139L;
    @ApiModelProperty(name = "用户ID")
    private Long userId;
    
    @ApiModelProperty(name = "配置项类型名")
    private String configType;
     
    @ApiModelProperty(name = "配置项名称值")
    private String configName;
     
    @ApiModelProperty(name = "创建时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date createTime;
    
    @ApiModelProperty(name = "修改时间")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
    private Date updateTime;
    
    @ApiModelProperty(name = "描述")
    private String description;
    
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}