package com.binance.account.job;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import com.binance.account.data.mapper.user.FutureInvitationLogMapper;
import com.binance.account.utils.InvitationCodeUtil;
import com.binance.master.utils.DateUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;


@Log4j2
@JobHandler(value = "GeneratorInvitationCodeHandler")
@Component
public class GeneratorInvitationCodeHandler extends IJobHandler {
	
    @Autowired
    private FutureInvitationLogMapper futureInvitationLogMapper;


	@Override
	public ReturnT<String> execute(String param) throws Exception {
		StopWatch sw = new StopWatch();
        log.info("param={}", param);
        log.info("start GeneratorInvitationCodeHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(),DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start();
            generatorInvitationCode();
            return SUCCESS;
        } catch (Exception e) {
            log.error("GeneratorInvitationCodeHandler error-->{}", e);
            return FAIL;
        }finally {
            sw.stop();
            log.info("end GeneratorInvitationCodeHandler,endTimeStamp={}", sw.getTotalTimeSeconds());
            
        }
	}
	
	public void generatorInvitationCode() {
            for(int i=0;i<100000;i++){
                try{
                    String invitationCode = InvitationCodeUtil.generateCode();
                    futureInvitationLogMapper.insert(invitationCode);
                }catch (Exception e){
                    log.info("generatorInvitationCode error-->{}", e);
                }
            }
	}



}
