package com.binance.account.job;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StopWatch;

import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.mapper.apimanage.ApiModelMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.integration.mbxgateway.AccountApiClient;
import com.binance.account.integration.mbxgateway.MatchboxApiClient;
import com.binance.master.utils.DateUtils;
import com.binance.mbxgateway.vo.ApiKeyInfoVo;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;


@Log4j2
@JobHandler(value = "repairErrorApiTradeStatusHandler")
@Component
public class RepairErrorApiTradeStatusHandler extends IJobHandler {
	
	@Autowired
	private ApiModelMapper apiModelMapper;
	@Autowired
	private MatchboxApiClient matchboxApi;
	@Autowired
	private AccountApiClient accountApiClient;
	@Autowired
	private UserInfoMapper userInfoMapper;


	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start repairErrorApiTradeStastusHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
			repairErrorApiTradeStatus();
            return SUCCESS;
        } catch (Exception e) {
            log.error("repairErrorApiTradeStastusHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end repairErrorApiTradeStastusHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	public void repairErrorApiTradeStatus() {
		try {
			List<ApiModel> apiModelList=apiModelMapper.loadApikeyWhichisTradeEnabled();
			for(ApiModel apiModel:apiModelList){
				try{
					Boolean needFix=checkIfNeedUpdateTradeStatus(apiModel);
					if(needFix){
						log.info("fix userid={},keyId={} start",apiModel.getUserId(),apiModel.getKeyId());
						UserInfo userInfo=userInfoMapper.selectByPrimaryKey(Long.valueOf(apiModel.getUserId()));
						if(Objects.isNull(userInfo)|| Objects.isNull(userInfo.getTradingAccount())){
							continue;
						}
						this.matchboxApi.putApiKeyPermissions(userInfo.getTradingAccount().toString(), "true", "true", "true", "true",
								"true", apiModel.getKeyId().toString(), "false");
						log.info("fix userid={},keyId={} done",apiModel.getUserId(),apiModel.getKeyId());

					}
				}catch (Exception e){
					log.info("repair single status error-->{}", e);

				}
			}
		}catch (Exception e) {
	         log.info("repairErrorApiTradeStatus error-->{}", e);
		}
	}

	private Boolean checkIfNeedUpdateTradeStatus(ApiModel apiModel){
		try{
			List<ApiKeyInfoVo> apiKeyInfoVoList=accountApiClient.getApiInfo(apiModel.getUserId());
			if(CollectionUtils.isEmpty(apiKeyInfoVoList)){
				return false;
			}
			for(ApiKeyInfoVo apiKeyInfoVo:apiKeyInfoVoList){
				Boolean isEqualKeyId=apiKeyInfoVo.getKeyId().longValue()==apiModel.getKeyId().longValue();
				if(!isEqualKeyId){
					continue;
				}
				Boolean isContainTradeRule=apiKeyInfoVo.getPermissions().contains("TRADE");
				//如果不存在代表是要修的数据
				if(!isContainTradeRule){
					return true;
				}
			}
		}catch (Exception e){
			log.info("checkIfNeedUpdateTradeStatus error-->{}", e);
			return false;
		}
		return false;
	}


}
