package com.binance.account.job;

import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.apimanage.ApiModelMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.integration.futureengine.FutureAccountApiClient;
import com.binance.account.integration.futureengine.FutureDeliveryAccountApiClient;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.JsonUtils;
import com.google.common.collect.Maps;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Log4j2
@JobHandler(value = "FixFutureApikeyIpHandler")
@Component
public class FixFutureApikeyIpHandler extends IJobHandler {

    @Resource
    private ApiModelMapper apiModelMapper;


    @Value("${fix.futuure.apikey.starttime:1597230887695}")
    private Long fixStartTime;//修复开始时间
    @Value("${fix.futuure.apikey.endtime:1597230887699}")
    protected Long fixEndTime; //修复结束时间

    @Value("${fix.futuure.apikey.size:1000}")
    protected Long fixPageSize; //修复结束时间


    @Autowired
    private FutureAccountApiClient futureAccountApiClient;
    @Autowired
    private FutureDeliveryAccountApiClient futureDeliveryAccountApiClient;
    @Autowired
    private UserInfoMapper userInfoMapper;



	@Override
    @Trace
	public ReturnT<String> execute(String param) throws Exception {
        StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start FixFutureApikeyIpHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            fixFutureApikeyIp();
            return SUCCESS;
        } catch (Exception e) {
            log.error("FixFutureApikeyIpHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end FixFutureApikeyIpHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	public void fixFutureApikeyIp() {
        log.info("fixStartTime={},fixEndTime={}",fixStartTime,fixEndTime);
        if(null==fixEndTime||null==fixEndTime){
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "fixStartTime or fixEndTime is empty");
        }
        Date startDateTIme=new Date(fixStartTime);
        Date endDateTIme=new Date(fixEndTime);

        Map<String, Object> param = Maps.newHashMap();
        param.put("startTime", startDateTIme);
        param.put("endTime", endDateTIme);
        Long totalCount = this.apiModelMapper.selectApiListCount(param);
        if(totalCount<=0){
            log.info("skip totalCount={} <=0",totalCount);
            return;
        }
        log.info(" totalCount={}",totalCount);
        int size=fixPageSize.intValue();
        int page=totalCount.intValue()/size;
        if(page==0){
            page=1;
        }
        log.info(" page={}，size={}",page,size);
        for(int i=1;i<=page+1;i++){
            param.put("start", (i-1)*size);
            param.put("offset", size);
            log.info("fixFutureApikeyIp.loadApiList,the param is {}", JsonUtils.toJsonNotNullKey(param));
            List<ApiModel> apiModelList = this.apiModelMapper.selectApiList(param);
            if(CollectionUtils.isEmpty(apiModelList)){
                log.info("skip apiModelList is empty");
                continue;
            }
            log.info("fixFutureApikeyIp.loadApiList,the param is {}", JsonUtils.toJsonNotNullKey(param));
            for(ApiModel apiModel:apiModelList){
                // 同步ip限制给期货
                Long userId=Long.parseLong(apiModel.getUserId());
                UserInfo userInfo = userInfoMapper.selectByPrimaryKey(userId);
                String ip=apiModel.getTradeIp();
                try {
                    if (null!=userInfo && null !=userInfo.getFutureUserId()) {
                        Long futureUserId = userInfo.getFutureUserId();
                        log.info("userId={},futureUserId={}",userId,futureUserId);

                        UserInfo futureUserInfo = userInfoMapper.selectByPrimaryKey(futureUserId);
                        if (Objects.isNull(futureUserInfo)) {
                            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
                        }

                        // 同步ip限制给永续
                        if (futureUserInfo.getMeTradingAccount() != null) {
                            try {
                                this.futureAccountApiClient.updateApiKeyRules(Long.valueOf(apiModel.getKeyId()), futureUserInfo.getMeTradingAccount().intValue(), ip);
                            } catch (Exception e) {
                                log.warn("futureAccountApiClient.updateApiKeyRules:", e);
                            }
                        }

                        // 同步ip限制给交割
                        if (futureUserInfo.getDeliveryTradingAccount() != null) {
                            try {
                                this.futureDeliveryAccountApiClient.updateApiKeyRules(Long.valueOf(apiModel.getKeyId()), futureUserInfo.getDeliveryTradingAccount().intValue(), ip);
                            } catch (Exception e) {
                                log.warn("futureDeliveryAccountApiClient.updateApiKeyRules:", e);
                            }
                        }
                        log.info("fixFutureApikeyIp...syncIpToFutures end");
                    }
                }catch (Exception e){
                    log.warn("fixFutureApikeyIp syncIpToFutures error:", e);
                }

            }
        }






	}





}
