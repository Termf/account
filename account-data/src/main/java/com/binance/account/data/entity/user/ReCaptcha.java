package com.binance.account.data.entity.user;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by mengjuan on 2018/11/21.
 */
@Getter
@Setter
public class ReCaptcha implements Serializable{

    private static final long serialVersionUID = -6538820468033078588L;

    private Long userId;
    private String  email;
    private String score;//分数
    private String success;//是否成功
    private String action;
    private Date challengeTs;//请求时间
    private String errorCodes;//错误码
}