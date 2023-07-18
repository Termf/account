package com.binance.account.domain.bo;

import com.binance.account.common.constant.WckConst;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.data.entity.certificate.UserChannelWckAudit;
import com.binance.account.data.entity.certificate.UserChannelWckAuditLog;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.entity.certificate.UserWckAuditLog;
import com.binance.account.vo.certificate.request.WckAuditRequest;
import com.binance.account.vo.certificate.request.WckChannelAuditRequest;
import com.binance.master.error.BusinessException;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Date;
import java.util.List;

/**
 * world check迁移新平台的审核逻辑
 * @author mikiya.chen
 * @date 2020/2/24 5:53 下午
 */
@Data
public class NewWckAuditDataHolder {

    private WckChannelAuditRequest request;
    private UserChannelWckAudit wckAudit;

    private UserChannelWckAuditLog firstLog;
    private UserChannelWckAuditLog secondLog;

    private Integer auditorSeq;

    private boolean finished = false;

    public static NewWckAuditDataHolder build(WckChannelAuditRequest request){
        NewWckAuditDataHolder holder = new NewWckAuditDataHolder();
        holder.setRequest(request);
        holder.setAuditorSeq(request.getAuditorSeq());
        return holder;
    }

    public void prepareData(UserChannelWckAudit wckAudit, List<UserChannelWckAuditLog> auditLogs){
        this.wckAudit = wckAudit;
        if (CollectionUtils.isNotEmpty(auditLogs)){
            for (UserChannelWckAuditLog log:auditLogs){
                if (log.getAuditorSeq()== WckConst.AUDIT_FIRST){
                    this.firstLog = log;
                }else if (log.getAuditorSeq()==WckConst.AUDIT_SECOND){
                    this.secondLog = log;
                }
            }
        }
    }

    public void validate(){
        if (wckAudit.getStatus()== WckChannelStatus.PASSED || wckAudit.getStatus()==WckChannelStatus.REJECTED){
            throw new BusinessException("已经是最终状态，无法提交审核");
        }
        if (firstLog!=null && auditorSeq==WckConst.AUDIT_FIRST){
            throw new BusinessException("一审已处理，不可重复处理");
        }
        if (auditorSeq==WckConst.AUDIT_SECOND){
            if (firstLog==null){
                throw new BusinessException("还没有一审，不可进行二审");
            }
            if (secondLog!=null){
                throw new BusinessException("二审已处理，不可重复处理");
            }
        }
    }

    /**
     * 审核数据
     *  1.一审无论是否通过，都需要进行二审
     *  2.审核结果以二审为主，二审通过则通过，二审拒绝则拒绝，一审结果仅为二审参考
     * 参考：<a href="https://confluence.toolsfdg.net/pages/viewpage.action?pageId=27876465">产品文档
     */
    public void doAudit(){
        if (auditorSeq==WckConst.AUDIT_FIRST){
            wckAudit.setFirstAuditorId(request.getAuditorId());
            wckAudit.setStatus(WckChannelStatus.AUDIT_SECOND);
        }else if (auditorSeq==WckConst.AUDIT_SECOND){
            finished = true;
            wckAudit.setSecondAuditorId(request.getAuditorId());
            if (request.getIsValid()){
                wckAudit.setStatus(WckChannelStatus.PASSED);
            }else {
                wckAudit.setStatus(WckChannelStatus.REJECTED);
            }
        }
        wckAudit.setUpdateTime(new Date());
    }

    public UserChannelWckAudit getResult(){

        return wckAudit;
    }

    /**
     * 审核流程结束
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * 审核通过
     */
    public boolean isPassed(){
        return wckAudit.getStatus() == WckChannelStatus.PASSED;
    }

    public UserChannelWckAuditLog getAuditLog(){
        return new UserChannelWckAuditLog(request.getCaseId(), request.getAuditorSeq(), request.getAuditorId(), request.getIsValid(), request.getMemo(),request.getIsPep(),request.getSanctionsHits(),request.getFailReason());
    }

    private NewWckAuditDataHolder(){}

}
