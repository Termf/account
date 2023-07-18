package com.binance.account.vo.user;

import com.binance.account.vo.user.enums.UserTypeEnum;
import lombok.Data;

import java.util.Date;

@Data
public class UserCommonPermissionVo {
    private UserTypeEnum userType;//用户类型

    private Boolean enableDeposit;//是否可以充值(0:禁止;1:允许)

    private Boolean enableWithdraw;//是否可以提币(0:禁止;1:允许)

    private Boolean enableTrade;//是否可以交易(0:禁止;1:允许)

    private Boolean enableTransfer;//是否可以划转(0:禁止;1:允许)

    private Boolean enableSubTransfer;//是否可以子账号划转(0:禁止;1:允许)

    private Boolean enableCreateApikey;//是否可以创建apikey(0:禁止;1:允许)

    private Boolean enableLogin;//是否可以登录(0:禁止;1:允许)

    private Boolean enableCreateMargin;//是否可以创建margin账号(0:禁止;1:允许)

    private Boolean enableCreateFutures;//是否可以创建期货账号(0:禁止;1:允许)

    private Boolean enableCreateFiat;//是否可以创建法币账号(0:禁止;1:允许)

    private Boolean enableCreateIsolatedMargin;//是否可以创建逐仓margin账号(0:禁止;1:允许)

    private Boolean enableCreateSubAccount;//是否可以创建子账号(0:禁止;1:允许)

    private Boolean enableParentAccount;//是否可以成为母账号(0:禁止;1:允许)

    private Boolean enableBrokerParentAccount;//是否可以broker母账号(0:禁止;1:允许)

    private Boolean enableCreateBrokerSubAccount;//是否可以创建broker子账号(0:禁止;1:允许)

    private Date insertTime;

    private Date updateTime;


}