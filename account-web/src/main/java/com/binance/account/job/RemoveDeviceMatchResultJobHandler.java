package com.binance.account.job;


import com.binance.account.data.mapper.device.DeviceMatchReportMapper;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.skywalking.apm.toolkit.trace.Trace;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;

@Log4j2
@JobHandler(value = "removeDeviceMatchResultJobHandler")
@Component
public class RemoveDeviceMatchResultJobHandler extends IJobHandler {

    @Resource
    private DeviceMatchReportMapper deviceMatchReportMapper;

    @Value("${remove.device.match.result.switch:true}")
    private boolean removeDeviceMatchResultSwitch;

    @Override
    @Trace
    public ReturnT<String> execute(String param) throws Exception {
        TrackingUtils.saveTraceId();
        StopWatch sw = new StopWatch();
        Date executionTime = DateUtils.getNewUTCDate();
        XxlJobLogger.log("start removeDeviceMatchResultJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(executionTime, DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start removeDeviceMatchResultJobHandler,startTimeStamp={}", DateUtils.formatterUTC(executionTime, DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            sw.start("query");
            //查询待修改数据
            Date deadline = new DateTime().minusMonths(1).toDate();
            final Long id = deviceMatchReportMapper.selectLastIdBefore(deadline);
            sw.stop();
            log.info("querying max id: {}, elapsed: {} ms", id, sw.getLastTaskInfo().getTimeMillis());
            if (id == null) {
                XxlJobLogger.log("Nothing need to be removed");
                log.info("Nothing need to be removed");
                return SUCCESS;
            }
            //update userAgentReward
            Integer affectedRows;
            int i = 0;
            do {
                sw.start("remove-" + i++);
                affectedRows = deviceMatchReportMapper.batchDeleteBefore(id);
                XxlJobLogger.log("affected rows: {0}", affectedRows);
                sw.stop();
                log.info("affected rows: {}, elapsed: {} ms", affectedRows, sw.getLastTaskInfo().getTimeMillis());
                // throw InterruptedException if interrupted
                Thread.sleep(1);
            } while (affectedRows != null && affectedRows > 0 && removeDeviceMatchResultSwitch);

            return SUCCESS;
        } catch (InterruptedException e) {
            log.info("removeDeviceMatchResultJobHandler interrupted-->{}", e);
            throw e;
        } catch (Exception e) {
            XxlJobLogger.log("removeDeviceMatchResultJobHandler error-->{0}", e);
            log.error("removeDeviceMatchResultJobHandler error-->{}", e);
            return FAIL;
        } finally {

            XxlJobLogger.log("end removeDeviceMatchResultJobHandler, costs={0}", sw.prettyPrint());
            log.info("end removeDeviceMatchResultJobHandler, costs={}", sw.prettyPrint());

        }
    }

}
