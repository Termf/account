package com.binance.account.vo.device.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserDeviceHistoryVo implements Serializable {

    private static final long serialVersionUID = -2364338084748376825L;

    private Long id;

    private Long userId;

    private Long userDeviceId;

    private String agentType;

    private Byte operateType;

    private String content;

    private String memo;

    private Date createTime;

}
