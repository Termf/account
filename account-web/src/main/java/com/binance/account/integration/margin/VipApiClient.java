package com.binance.account.integration.margin;

import com.binance.margin.api.bookkeeper.VipApi;
import com.binance.margin.api.bookkeeper.request.UpdateVipLevelRequest;
import com.binance.master.error.BusinessException;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class VipApiClient {
    @Autowired
    private VipApi vipApi;

    public void updateVipLevel(Long rootUserId, int vipLevel){
        log.info("VipApiClient.updateVipLevel.rootUserId:{},vipLevel:{}",rootUserId,vipLevel);
        UpdateVipLevelRequest updateVipLevelRequest = new UpdateVipLevelRequest();
        updateVipLevelRequest.setMajorUid(rootUserId);
        updateVipLevelRequest.setVipLevel(vipLevel);
        APIResponse<Void> response = vipApi.updateVipLevel(APIRequest.instance(updateVipLevelRequest));
        if (APIResponse.Status.ERROR == response.getStatus()) {
            log.error("VipApiClient.updateVipLevel :rootUserId=" + rootUserId + "  error" + response.getErrorData());
            throw new BusinessException("updateVipLevel failed");
        }
    }

}
