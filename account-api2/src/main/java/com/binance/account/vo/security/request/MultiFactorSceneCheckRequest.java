package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.BizSceneEnum;
import lombok.Data;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/19 7:13 下午
 */
@Data
public class MultiFactorSceneCheckRequest {

    private Long userId;
    private BizSceneEnum bizScene;
    private Boolean isBindMobile;
    private Boolean isBindEmail;
    private Boolean isBindGoogle;
    private Boolean isBindYubikey;
    private Long devicePk;// 判断是否新设备，新设备为空
    private String clientType;// 客户端类型
}
