package com.binance.account.job;

import com.binance.account.common.constant.ResetConst;
import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.account.data.mapper.security.UserSecurityResetMapper;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.security.impl.UserSecurityResetBusiness;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.TrackingUtils;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;
import com.xxl.job.core.log.XxlJobLogger;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author liliang1
 * @date 2018-09-20 20:28
 * 重置流程，用户从最后一次更新时间起，7天内无操作的，除Jumio通过/失败且人脸通过的状态外，一律拒绝并发送拒绝邮件。并加上拒绝原因
 */
@Log4j2
@JobHandler("securityResetNoChangeJobHandler")
@Component
public class SecurityResetNoChangeJobHandler extends IJobHandler {

    @Resource
    private UserSecurityResetMapper userSecurityResetMapper;
    @Resource
    private UserSecurityResetBusiness resetBusiness;
    @Resource
    private JumioBusiness jumioBusiness;

    @Override
    public ReturnT<String> execute(String s) throws Exception {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        try {
            resetNoChangeHandler(s);
            return SUCCESS;
        }catch (Exception e) {
            XxlJobLogger.log("SecurityResetNoChangeJobHandler error-->{0}", e);
            log.error("SecurityResetNoChangeJobHandler error-->{}", e);
            return FAIL;
        }finally {
            stopWatch.stop();
            XxlJobLogger.log("end SecurityResetNoChangeJobHandler,endTimeStamp={0}s", stopWatch.getTotalTimeSeconds());
        }
    }

    private int parseParam(String s) {
        //默认7天 168小时
        int defaultHours = 168;
        try {
            int hours = defaultHours;
            if (StringUtils.isNotBlank(s)) {
                hours = Integer.valueOf(s);
            }
            if (hours <= 0) {
                return defaultHours;
            }else {
                return hours;
            }
        }catch (Exception e) {
            return defaultHours;
        }
    }

    private void resetNoChangeHandler(String s) {
        //把update是小于七天之前的时间并且状态不是人脸通过状态的数据全部查询出来
        int hours = parseParam(s);
        Date endTime = DateUtils.addHours(DateUtils.getNewUTCDate(), -hours);
        // RM-260 过滤掉jumio和face中通过的状态
        List<UserSecurityReset> resets = userSecurityResetMapper.getLongTimeNoChangeList(endTime);
        if (resets == null || resets.isEmpty()){
            return;
        }
        XxlJobLogger.log("由于超出{0}小时无操作的记录数: {1}", hours, resets.size());
        log.info("由于超出{}小时无操作的记录数: {}", hours, resets.size());
        for (UserSecurityReset reset : resets) {
            try {
                TrackingUtils.putTracking("RESET_NO_CHANGE", UUID.randomUUID().toString());
                UserSecurityReset current = userSecurityResetMapper.selectByPrimaryKey(reset.getId());
                if (current == null || !UserSecurityResetStatus.isReviewPending(current.getStatus())) {
                    log.info("当前状态信息已经不在处理中的状态。userId:{} resetId:{}", reset.getUserId(), reset.getId());
                    continue;
                }else {
                    reset = current;
                }
                // 只有在数据scan_reference 不为空的时候才去查询jumio状态
                if (StringUtils.isNotBlank(reset.getScanReference())) {
                    JumioInfoVo vo = jumioBusiness.getByUserAndScanRef(reset.getUserId(),
                            reset.getScanReference(),
                            reset.getType().name());
                    if (vo != null && vo.getStatus() == JumioStatus.REVIEW) {
                    	//修改最新更新时间，防止被后面再起捞起
                    	UserSecurityReset record = new UserSecurityReset();
                    	record.setId(reset.getId());
                    	record.setUpdateTime(DateUtils.getNewUTCDate());
                    	userSecurityResetMapper.updateByPrimaryKeySelective(record);
                        log.info("jumio审核中,不能自动拒绝,userId:{}, resetId:{}", reset.getUserId(), reset.getId());
                        continue;
                    }
                }
                log.info("重置流程由于长时间没有操作后续流程，直接进行取消, userId:{}, resetId:{}", reset.getUserId(), reset.getId());
                reset.setUpdateTime(DateUtils.getNewUTCDate());
                reset.setStatus(UserSecurityResetStatus.cancelled);
                reset.setAuditMsg("流程长时间没有操作, 系统自动取消");
                reset.setAuditTime(DateUtils.getNewUTCDate());
                reset.setFailReason(ResetConst.KEY_7_DAYS_REFUSED);
                userSecurityResetMapper.updateByPrimaryKeySelective(reset);
                //发起拒绝邮件
                resetBusiness.sendResetAuthEmail(UserSecurityResetStatus.refused, reset, ResetConst.KEY_7_DAYS_REFUSED, true);
                log.info("已发送重置流程长时间没有操作直接拒绝的通知邮件. userId:{} resetId:{}", reset.getUserId(), reset.getId());
            }catch (Exception e) {
                log.error("处理长时间内无操作的拒绝操作异常. userId:{} resetId:{}", reset.getUserId(), reset.getId(), e);
            }finally {
                TrackingUtils.removeTracking();
            }
        }
    }

}
