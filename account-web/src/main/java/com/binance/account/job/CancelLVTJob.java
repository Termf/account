package com.binance.account.job;

import com.binance.account.service.user.IUserLVT;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.account.service.question.Utils;
import com.binance.account.vo.subuser.request.UserIdReq;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;


/**
 * 取消user lvt状态
 */
@Log4j2
@JobHandler(value = "cancelLVTJob")
@Component
public class CancelLVTJob extends IJobHandler {
    
    @Autowired
    private IUserLVT iUserLVT;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try{
            log.info("cancelLVTJob start, param={}", s);
            if (StringUtils.isBlank(s)) {
                return ReturnT.SUCCESS;    
            }
            String[] userIdsArr = s.split("[,，]");
            for (String userId : userIdsArr) {
                try {
                    log.info("cancelLVTJob 开始处理{}", userId);
                    UserIdReq userIdReq = new UserIdReq();
                    userIdReq.setUserId(Long.valueOf(userId));
                    APIResponse<Boolean> apiResponse = iUserLVT.cancelSignLVT(APIRequest.instance(userIdReq));
                    Utils.CheckResponse(apiResponse);                    
                } catch (Exception e) {
                    log.error("cancelLVTJob 处理失败{} {}", userId, e.getMessage());
                }
            }
            log.info("cancelLVTJob  done");
            return ReturnT.SUCCESS;
        }catch (Exception e){
            log.warn("cancelLVTJob job exception", e);
            return ReturnT.FAIL;
        }
    }

}
