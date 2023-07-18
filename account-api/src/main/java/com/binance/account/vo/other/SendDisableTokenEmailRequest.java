package com.binance.account.vo.other;

import java.util.Map;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SendDisableTokenEmailRequest {
    private Long userId;
    
    private String tplCode;
    
    private String remark;
    
    private String customForbiddenLink;
    
    private Map<String,Object> data;
}
