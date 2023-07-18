package com.binance.account.vo.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-09-14 20:44
 */
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class ResetFaceCaptureResult implements Serializable {
    private static final long serialVersionUID = 612549935776183133L;

    /** 是否成功 */
    private boolean success;

    /** 提示信息 */
    private String message;


}
