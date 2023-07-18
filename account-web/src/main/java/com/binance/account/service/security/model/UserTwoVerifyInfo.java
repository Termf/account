package com.binance.account.service.security.model;

import com.binance.account.vo.security.enums.BizSceneEnum;
import lombok.Data;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 3:29 下午
 */
@Data
public class UserTwoVerifyInfo {
    private Long userId;
    private BizSceneEnum bizScene;
    private Boolean isBindMobile;
    private Boolean isBindEmail;
    private Boolean isBindGoogle;
    private Boolean isBindYubikey;
}
