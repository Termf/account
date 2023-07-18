package com.binance.account.common.validator;

import lombok.Getter;

/**
 * 校验结果
 * @author: caixinning
 * @date: 2018/05/17 17:54
 **/
@Getter
public class ValidateResult {

    private boolean ok;
    private String message;

    private ValidateResult(boolean ok, String message) {
        this.ok = ok;
        this.message = message;
    }

    public static ValidateResult pass(String message){
        return new ValidateResult(true, message);
    }

    public static ValidateResult pass(){
        return OK_EMPTY;
    }

    public static ValidateResult reject(String message){
        return new ValidateResult(false, message);
    }

    private static final ValidateResult OK_EMPTY = new ValidateResult(true, null);
}
