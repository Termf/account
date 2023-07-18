package com.binance.account.job;

import com.binance.account.service.certificate.impl.UserChainAddressBusiness;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

/**
 * @author mikiya.chen
 * @date 2020/4/23 11:10 上午
 */
@Log4j2
@JobHandler(value = "ChainAddressWhiteListRefreshJobHandler")
@Component
public class ChainAddressWhiteListRefreshJobHandler  extends IJobHandler {

    @Resource
    private UserChainAddressBusiness userChainAddressBusiness;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        XxlJobLogger.log("开始执行 ChainAddressWhiteListRefreshJobHandler 执行参数:" + s);
        log.info("START-ChainAddressWhiteListRefreshJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try{
            handler(s);
            return ReturnT.SUCCESS;
        }catch (Exception e){
            XxlJobLogger.log("执行 ChainAddressWhiteListRefreshJobHandler 失败 param:{0} {1}", s, e);
            log.error("执行 ChainAddressWhiteListRefreshJobHandler 失败 param:{}", s, e);
            return FAIL;
        }finally {
            stopWatch.stop();
            XxlJobLogger.log("执行 ChainAddressWhiteListRefreshJobHandler 完成 use {0}s", stopWatch.getTotalTimeSeconds());
        }
    }

    private void handler(String params){
        userChainAddressBusiness.refreshWhiteAddrCache();
    }
}
