package com.binance.account.job;

import com.binance.account.common.constant.ResetConst;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.service.security.IFace;
import com.binance.account.service.security.impl.UserSecurityResetBusiness;
import com.binance.inspector.common.enums.FaceStatus;
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
 * @date 2018-09-21 11:58
 * 重置流程：失败20次（可配置），那么整条记录被拒绝并发送邮件加上拒绝原因
 */
@Log4j2
@JobHandler("securityResetFaceFailCountJobHandler")
@Component
public class SecurityResetFaceFailCountJobHandler extends IJobHandler {

    @Resource
    private UserSecurityResetMapper userSecurityResetMapper;
    @Resource
    private UserSecurityResetBusiness resetBusiness;
    @Resource
    private IFace iFace;
    @Resource
    private ApolloCommonConfig apolloCommonConfig;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            resetFaceFailCountHandler();
            return SUCCESS;
        }catch (Exception e) {
            XxlJobLogger.log("SecurityResetFaceFailCountJobHandler error-->{0}", e);
            log.error("SecurityResetNoChangeJobHandler error-->{}", e);
            return FAIL;
        }finally {
            stopWatch.stop();
            XxlJobLogger.log("end SecurityResetFaceFailCountJobHandler,endTimeStamp={0}s", stopWatch.getTotalTimeSeconds());
        }
    }

    private void resetFaceFailCountHandler() {
        Integer count = 20;
        Integer faceResetFailCount = apolloCommonConfig.getFaceResetFailCount();
        if (faceResetFailCount != null && faceResetFailCount > 0) {
            count = faceResetFailCount;
        }
        //先获取正在处于人脸失败状态的重置数据(最近20天的数据)
        Date endTime = DateUtils.addDays(DateUtils.getNewUTCDate(), -20);
        List<UserSecurityReset> resets = userSecurityResetMapper.getFaceFailStatusList(endTime);
        for (UserSecurityReset reset : resets) {
            try {
                TrackingUtils.putTracking("RESET_FACE_FAIL_COUNT", UUID.randomUUID().toString());
                Long userId = Long.valueOf(reset.getUserId());
                String resetId = reset.getId();
                UserSecurityReset current = userSecurityResetMapper.selectByPrimaryKey(resetId);
                UserSecurityResetStatus status = current == null ? null : current.getStatus();
                String faceStatus = current == null ? null : current.getFaceStatus();
                if (current == null || !UserSecurityResetStatus.isReviewPending(status)
                        || StringUtils.equalsIgnoreCase(faceStatus, FaceStatus.FACE_FAIL.name())) {
                    log.info("当前重置记录的人脸识别状态不在拒绝状态. userId:{} resetId:{} status:{} faceStatus:{}",
                            userId, resetId, status, faceStatus);
                    continue;
                }else {
                    reset = current;
                }
                //检测当前用户的人脸操作失败次数是否达到设定值，如果达到，则可以进行直接拒绝
                int failCount = iFace.getFaceLogTimes(userId, resetId, FaceTransType.getByCode(reset.getType().name()), FaceStatus.FACE_FAIL, null);
                if (failCount < count) {
                    //没达到错误次数，直接退出
                    continue;
                }
                log.info("人脸错误次数超过指定次数，直接进行拒绝, userId:{}, resetId:{}", userId, resetId);
                XxlJobLogger.log("人脸错误次数超过指定次数，直接进行拒绝, userId:{0}, resetId:{1}", userId, resetId);
                reset.setUpdateTime(DateUtils.getNewUTCDate());
                reset.setStatus(UserSecurityResetStatus.refused);
                reset.setAuditMsg("人脸对比错误次数超过" + count + "次, 系统自动拒绝");
                reset.setAuditTime(DateUtils.getNewUTCDate());
                reset.setFailReason(ResetConst.KEY_FACE_MULTIPLE_COUNT);
                userSecurityResetMapper.updateByPrimaryKeySelective(reset);
                //发起拒绝邮件
                resetBusiness.sendResetAuthEmail(UserSecurityResetStatus.refused, reset, ResetConst.KEY_FACE_MULTIPLE_COUNT, true);
                log.info("已发送重置流程人脸错误次数超过指定次数直接拒绝的通知邮件. userId:{} resetId:{}", reset.getUserId(), reset.getId());
            }catch (Exception e) {
                log.error("人脸错误次数超过指定次数拒绝操作异常. userId:{} resetId:{}", reset.getUserId(), reset.getId(), e);
            }finally {
                TrackingUtils.removeTracking();
            }
        }
    }
}
