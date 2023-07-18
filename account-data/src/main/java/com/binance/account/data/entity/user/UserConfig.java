package com.binance.account.data.entity.user;

import java.io.Serializable;
import java.util.Date;
import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserConfig implements Serializable{
    /**
     * 
     */
     private static final long serialVersionUID = -35143489575208418L;
     private Long userId;
     private String configType;//配置项类型名
     private String configName;//配置项名称值
     private Date createTime;
     private Date updateTime;
     private String description;
    
    
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
} 
