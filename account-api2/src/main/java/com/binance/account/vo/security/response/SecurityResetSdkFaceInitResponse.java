package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author liliang1
 * @date 2018-10-22 18:33
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class SecurityResetSdkFaceInitResponse extends ToString {
    private static final long serialVersionUID = -3465541322472450699L;

    /**
     * 是否初始化成功
     */
    private boolean success;

    /**
     * 二维码值
     */
    private String qrCode;

    /** 错误提示语 */
    private String message;

}
