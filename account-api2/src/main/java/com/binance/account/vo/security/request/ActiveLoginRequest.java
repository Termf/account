package com.binance.account.vo.security.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class ActiveLoginRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6982755706894020020L;
    
    @NotNull
    private Long userId;

}
