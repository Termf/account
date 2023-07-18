package com.binance.account.common.query;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-12-14 14:52
 */
@Setter
@Getter
public class JumioBizStatusQuery implements Serializable {
    private static final long serialVersionUID = -7834094719154276892L;

    private Long userId;
    private String bizId;
    private String handlerTypeCode;
}
