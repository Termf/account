package com.binance.account.service.other;

import com.binance.account.vo.other.SendDisableTokenEmailRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public interface IOther {
    
    public APIResponse<String> sendDisableTokenEmail(APIRequest<SendDisableTokenEmailRequest> request);
    
}
