package com.binance.account.service.security.model;

import lombok.Data;
/**
 * 这个类的作用是记录发送绑定信息验证码使用的邮箱或者手机号，防止用户通过邮箱A发送验证码然后改成
 * 绑定邮箱B
 * */
@Data
public class UserBindSendInfo {
    private Long userId;
    private String email;
    private String mobile;
    private String mpbileCode;//存入的是CN不是86


}
