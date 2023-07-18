package com.binance.account.data.entity.certificate;

import com.binance.master.enums.LanguageEnum;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Setter
@Getter
public class MessageMap implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 3676653810257396152L;

	private String code;

    private String lang;

    private String message;

    public MessageMap() {
    }

    public MessageMap(String code, String lang, String message) {
        this.code = code;
        this.lang = LanguageEnum.findByLang(lang).getLang();
        this.message = message;
    }
}
