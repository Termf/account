package com.binance.account.vo.security.request;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Data
public class DisableLoginRequest implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = 6615509751633978259L;
    
    @NotNull
    private Long userId;
    
}
