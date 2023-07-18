package com.binance.account.data.entity.apimanage;

import com.binance.master.commons.ToString;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ApiModel extends ToString {

    private static final long serialVersionUID = -4031762587138427778L;

    private Long id;

    private String userId;

    private String email;

    private Integer keyId;

    private String apiKey;

    private String apiName;

    private String secretKey;

    private String tradeIp;

    private String withdrawIp;

    private String ruleId;

    private int status;

    private boolean disableStatus;

    private String info;

    private Date createTime;

    private Date updateTime;

    private boolean enableWithdrawStatus;

    private String withdrawVerifycode;

    private Date withdrawVerifycodeTime;

    private boolean withdraw;

    private String uuid;

    private boolean apiEmailVerify;

    private Date createEmailSendTime;

    private Integer smallEnforcedLimit;

    private Integer bigEnforcedLimit;

    private String type;

    private String accountType;
}
