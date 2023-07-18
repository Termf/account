package com.binance.account.job;

import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.service.security.IUserFace;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author liliang1
 * @date 2018-08-23 17:39
 */
@Log4j2
@JobHandler("securityResetFaceJobHandler")
@Component
public class SecurityResetFaceJobHandler extends IJobHandler {

    @Resource
    private UserSecurityResetMapper userSecurityResetMapper;
    @Resource
    private IUserFace iUserFace;

    @Override
    public ReturnT<String> execute(String param) throws Exception {
        StopWatch stopWatch = new StopWatch();
        XxlJobLogger.log("start securityResetFaceJobHandler,startTimeStamp={0}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        log.info("start securityResetFaceJobHandler,startTimeStamp={}", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.DETAILED_NUMBER_PATTERN));
        try {
            stopWatch.start();
            securityResetFaceHandler(param);
            return SUCCESS;
        } catch (Exception e) {
            XxlJobLogger.log("securityResetFaceJobHandler error-->{0}", e);
            log.error("securityResetFaceJobHandler error-->{}", e);
            return FAIL;
        } finally {
            stopWatch.stop();
            XxlJobLogger.log("end securityResetFaceJobHandler,endTimeStamp={0}", stopWatch.getTotalTimeSeconds());
            log.info("end securityResetFaceJobHandler,endTimeStamp={}", stopWatch.getTotalTimeSeconds());
        }
    }

    private Date parseEndTime(String param) {
        //默认只查询最近30天内的数据
        int days = 30;
        try {
            if (StringUtils.isNotBlank(param)) {
                int paramInt = Integer.parseInt(param);
                if (paramInt > 0) {
                    days = paramInt;
                }
            }
        }catch (Exception e) {
            log.error("重置流程发起人脸识别任务参数信息解析异常. param:{}", param);
        }
        return DateUtils.addDays(DateUtils.getNewUTCDate(), -days);
    }

    /**
     * 查询出正在需要发送邮件进行通知用户做Face活体失败的过程
     * 每3分钟检查一次, 每次取30条数据来处理
     * 处理条件:
     * 1.已经有JUMIO验证结果(jumioPassed:6 和 jumioRefused:7)
     * 2. 且JUMIO照片中hand照片已经拉下来了，
     * 3. 并且face_status是为处理状态的(空值)
     */
    public void securityResetFaceHandler(String param) {
        Date endTime = parseEndTime(param);
        //把需要处理的输出查询出来
        List<UserSecurityReset> resets = userSecurityResetMapper.getPendingFaceList(endTime);
        if (resets == null || resets.isEmpty()) {
            log.info("need face++ check list is empty");
            return;
        }
        log.info("当前需要做人脸识别的重置流程记录数:{}", resets.size());
        //循环做处理
        for (UserSecurityReset reset : resets) {
            TrackingUtils.putTracking("RESET_FACE_JOB", UUID.randomUUID().toString());
            String transId = reset.getId();
            Long userId = reset.getUserId();
            try {
                FaceTransType faceTransType = FaceTransType.getByCode(reset.getType().name());
                iUserFace.initFaceFlowByTransId(transId, userId, faceTransType, true, false);
                log.info("重置2FA流程, 发送邮件通知用户做人脸识别, userId:{} transId:{}", userId, transId);
                XxlJobLogger.log("重置2FA流程, 发送邮件通知用户做人脸识别结果, userId:{0} transId:{1}", userId, transId);
            } catch (Exception e) {
                log.error("处理重置流程发起人脸识别流程异常 userId:{} resetId", reset.getUserId(), reset.getId(), e);
                XxlJobLogger.log("处理重置流程发起人脸识别流程异常 userId:{0} transId:{1} message:{2}", userId, transId, e.getMessage());
            } finally {
                TrackingUtils.removeTracking();
            }
        }
    }

}
