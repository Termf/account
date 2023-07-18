package com.binance.account.data.entity.device;

import java.util.Date;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 设备关系
 */
@Data
@NoArgsConstructor
public class UserDeviceRelation{

    private Long devicePk;

    private Long releatedDevicePk;

    private Long userId;

    private Long releatedUserId;

    private String releatedUserEmail;

    private Date createTime;

    private Date updateTime;

    public UserDeviceRelation(Long userId, Long devicePk, UserDevice related){
        this.userId = userId;
        this.devicePk = devicePk;
        this.releatedDevicePk = related.getId();
        this.releatedUserId = related.getUserId();
    }

}