package com.binance.account.job;

import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.data.entity.certificate.UserChannelWckAudit;
import com.binance.account.data.mapper.certificate.UserChannelWckAuditMapper;
import com.binance.inspector.api.WorldCheckApi;
import com.binance.inspector.vo.worldcheck.request.WckInspectApplyRequest;
import com.binance.inspector.vo.worldcheck.response.WckInspectApplyResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.List;

/**
 * @author mikiya.chen
 * @date 2020/3/10 4:45 下午
 */
@Log4j2
@JobHandler(value = "KycChannelErrorWckRepairJobHandler")
@Component
public class KycChannelErrorWckRepairJobHandler extends IJobHandler {

    @Resource
    private WorldCheckApi worldCheckApi;
    @Resource
    private UserChannelWckAuditMapper userChannelWckAuditMapper;

    private Integer PAGE_SIZE = 500;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        XxlJobLogger.log("开始执行 KycChannelErrorWckRepairJobHandler 执行参数:" + param);
        log.info("START-KycChannelErrorWckRepairJobHandler");
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            Long userId = null;
            if (StringUtils.isNotBlank(param) && StringUtils.isNumeric(param)) {
                userId = Long.parseLong(param);
            }
            //分页处理
            int count = userChannelWckAuditMapper.countByUserIdAndStatus(userId, WckChannelStatus.ERROR);
            int batchNum = count%PAGE_SIZE == 0 ? count/PAGE_SIZE : ((count/PAGE_SIZE) + 1);
            for(int i = 0; i < batchNum; i++){
                int start = i*PAGE_SIZE;
                List<UserChannelWckAudit> errorList = userChannelWckAuditMapper.selectByUserIdAndStatusInPage(userId, WckChannelStatus.ERROR, start, PAGE_SIZE);
                errorList.forEach(item -> {
                    repairErrorJob(item);
                });
            }
            return SUCCESS;
        } catch (Exception e){
            XxlJobLogger.log("KycChannelErrorWckRepairJobHandler error-->{0}", e);
            log.error("KycChannelErrorWckRepairJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("END-KycChannelErrorWckRepairJobHandler use {0}", stopWatch.getTotalTimeSeconds());
            log.info("END-KycChannelErrorWckRepairJobHandler use {}s", stopWatch.getTotalTimeSeconds());
        }
    }

    private void repairErrorJob(UserChannelWckAudit audit) {
        try {
            WckInspectApplyRequest request = new WckInspectApplyRequest();
            request.setCaseId(audit.getCaseId());
            request.setName(audit.getCheckName());
            request.setBirthDate(audit.getBirthDate());
            request.setNationality(audit.getIssuingCountry());

            APIResponse<WckInspectApplyResponse> response = worldCheckApi.applyWorldCheck(APIRequest.instance(request));
            if (response.getStatus() == APIResponse.Status.OK){
                WckInspectApplyResponse body = response.getData();
                audit.setStatus(WckChannelStatus.INITIAL);
                audit.setCaseSystemId(body.getCaseSystemId());
                userChannelWckAuditMapper.updateByPrimaryKeySelective(audit);
            }else {
                log.warn("repair error worldCheck failed, request:{}, response:{}", request, response);
            }
        }catch (Exception e) {
            log.error("repair error worldCheck exception, {} {}", audit.getUserId(), audit.getCaseId());
        }
    }
}
