package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by yangyang on 2019/11/7.
 */
@Data
public class AccountUnifyUserInfoResponse {

    private Long userId;

    private String email;

    private String name;

    private String cellphone;

    private Integer accountType;

    private String accountTypeDesc;

    /**
     * 相关账户个数
     */
    private Integer relevantAccountNum;


    /**
     * 交易等级、VIP等级
     */
    private Integer tradeLevel;

    private Date registerTime;

    private Date lastLoginTime;

    private Integer identityStatus;

    /**
     * 禁用登录
     */
    private Integer loginForbid;

    /**
     * 禁用交易
     */
    private Integer tradeForbid;

    /**
     * 手动禁用
     */
    private Integer withdrawSecurityStatus;

    /**
     * 风控禁用
     */
    private Integer withdrawSecurityAutoStatus;

    /**
     * 剩余额度
     */
    private BigDecimal  remainWithdrawAmount;

    /**
     * 总额度
     */
    private BigDecimal totalWithdrawAmount;

    /**
     * 是否被禁用
     */
    private boolean isDisable;

    /**
     * 提币人脸识别是否激活
     */
    private Integer withdrawFaceStatus;
}
