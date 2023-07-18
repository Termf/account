package com.binance.account.job;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.service.user.IUserFuture;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;


/**
 * 存量future账户，开通交割合约账户
 * 有永续账号 && (30天内登陆过 || 有apikey)
 */
@Log4j2
@JobHandler(value = "fixDeliveryAccountJob")
@Component
public class FixDeliveryAccountJob extends IJobHandler {

    @Autowired
    private UserInfoMapper userInfoMapper;
    @Autowired
    private IUserFuture userFuture;

    private static final ExecutorService fixDeliveryAccountExecutor = Executors.newFixedThreadPool(10);

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        try{
            log.info("fixDeliveryAccountJob start, params={}", s);
            List<Long> userIds = Lists.newArrayList();
            if (StringUtils.isNotBlank(s)) {
                String[] userIdsArr = s.split("[,，]");
                userIds = Arrays.stream(userIdsArr).map(x ->Long.valueOf(x)).collect(Collectors.toList());
            } else {
                userIds = userInfoMapper.selectFixDeliveryUserIds();
            }
            log.info("fixDeliveryAccountJob userIds.size={}", userIds.size());

            for (Long rootUserId : userIds) {
                fixDeliveryAccountExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        log.info("fixDeliveryAccountJob 开始处理rootUserId={}", rootUserId);
                        try {
                            Boolean needCreate = userFuture.fixDeliveryAccount(rootUserId);
                            log.info("fixDeliveryAccountJob 处理结束rootUserId={} 是否创建deliveryAccount={}", rootUserId, needCreate);
                        } catch (Exception e) {
                            log.error("fixDeliveryAccountJob 处理失败, rootUserId="+rootUserId, e);
                        }
                    }
                });
            }
            log.info("fixDeliveryAccountJob done");
            return ReturnT.SUCCESS;
        }catch (Exception e){
            log.warn("fixDeliveryAccountJob exception", e);
            return ReturnT.FAIL;
        }
    }

}
