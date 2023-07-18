package com.binance.account.data.entity.apimanage;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OperateLogModel extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = 2602894610707015911L;

    private String id;
    // 用户名
    private String userId;

    // 用户类型
    private String userType;

    // 操作时间
    private Date operateTime;

    // ip地址
    private String ipAddress;

    // 操作类型
    private String operateType;

    // 操作模块
    private String operateModel;

    // 操作结果
    private String operateResult;

    // 异常信息
    private String resInfo;
}
