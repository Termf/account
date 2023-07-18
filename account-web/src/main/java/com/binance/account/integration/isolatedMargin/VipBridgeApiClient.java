package com.binance.account.integration.isolatedMargin;

import com.binance.margin.isolated.api.vip.VipBridgeApi;
import com.binance.margin.isolated.api.vip.request.UpdateVipLevelRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class VipBridgeApiClient {
    @Autowired
    private VipBridgeApi vipBridgeApi;



    public void updateVipLevel(Long rootUserId, int vipLevel){
        log.info("VipBridgeApiClient.updateVipLevel.rootUserId:{},vipLevel:{}",rootUserId,vipLevel);
        UpdateVipLevelRequest updateVipLevelRequest=new UpdateVipLevelRequest();
        updateVipLevelRequest.setUserId(rootUserId);
        updateVipLevelRequest.setVipLevel(vipLevel);
        APIResponse<Void> response = vipBridgeApi.updateVipLevel(APIRequest.instance(updateVipLevelRequest));
        if (APIResponse.Status.ERROR == response.getStatus()) {
            log.error("VipBridgeApiClient.updateVipLevel :rootUserId=" + rootUserId + "  error" + response.getErrorData());
            throw new BusinessException("updateVipLevel failed");
        }
    }

}
