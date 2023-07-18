package com.binance.account.vo.security.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import lombok.Data;

@Data
public class OneButtonActivationRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6982755706894020020L;
    
    @NotNull
    private Long userId;

}
