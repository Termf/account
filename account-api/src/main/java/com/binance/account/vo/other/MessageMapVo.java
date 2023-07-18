package com.binance.account.vo.other;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class MessageMapVo implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7298844383927598379L;

	private String code;

    private String lang;

    private String message;

    public MessageMapVo() {
    }

    public MessageMapVo(String code, String lang, String message) {
        this.code = code;
        this.lang = lang;
        this.message = message;
    }
}
