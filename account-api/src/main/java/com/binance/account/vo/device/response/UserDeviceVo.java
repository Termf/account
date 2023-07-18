package com.binance.account.vo.device.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserDeviceVo implements Serializable {

    private static final long serialVersionUID = -3633149265091365750L;

    private Long id;

    private Long userId;

    private String agentType;

    private String source;

    private String content;

    private Date activeTime;

    private Date createTime;

    private Date updateTime;

    private Byte isDel;

    private Integer status;

}
