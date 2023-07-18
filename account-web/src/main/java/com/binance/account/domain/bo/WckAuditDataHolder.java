package com.binance.account.domain.bo;

import com.binance.account.common.constant.WckConst;
import com.binance.account.common.enums.WckStatus;
import com.binance.account.data.entity.certificate.UserWckAudit;
import com.binance.account.data.entity.certificate.UserWckAuditLog;
import com.binance.account.vo.certificate.request.WckAuditRequest;
import com.binance.master.error.BusinessException;
import java.util.Date;
import java.util.List;
import lombok.Data;
import org.apache.commons.collections4.CollectionUtils;

/**
 * Created by Shining.Cai on 2018/09/10.
 **/
@Data
public class WckAuditDataHolder {

    private WckAuditRequest request;
    private UserWckAudit wckAudit;

    private UserWckAuditLog firstLog;
    private UserWckAuditLog secondLog;
    private UserWckAuditLog thirdLog;

    private Integer auditorSeq;

    private boolean finished = false;

    public static WckAuditDataHolder build(WckAuditRequest request){
        WckAuditDataHolder holder = new WckAuditDataHolder();
        holder.setRequest(request);
        holder.setAuditorSeq(request.getAuditorSeq());
        return holder;
    }

    public void prepareData(UserWckAudit wckAudit, List<UserWckAuditLog> auditLogs){
        this.wckAudit = wckAudit;
        if (CollectionUtils.isNotEmpty(auditLogs)){
            for (UserWckAuditLog log:auditLogs){
                if (log.getAuditorSeq()==WckConst.AUDIT_FIRST){
                    this.firstLog = log;
                }else if (log.getAuditorSeq()==WckConst.AUDIT_SECOND){
                    this.secondLog = log;
                }else if (log.getAuditorSeq()==WckConst.AUDIT_THIRD){
                    this.thirdLog = log;
                }
            }
        }
    }

    public void validate(){
        if (wckAudit.getStatus()==WckStatus.PASSED || wckAudit.getStatus()==WckStatus.REJECTED){
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
        if (auditorSeq==WckConst.AUDIT_THIRD){
            if (secondLog==null){
                throw new BusinessException("还没有二审，不可进行终审");
            }
            if (thirdLog!=null){
                throw new BusinessException("终审已处理，不可重复处理");
            }
        }
        if (request.getForceFinal() && auditorSeq!=WckConst.AUDIT_FIRST){
            throw new BusinessException("只有一审才可以强制进行终审");
        }
    }

    /**
     * 审核数据
     *  1.强制直接终审，则直接跳到终审，否则：
     *  2.一审无论是否通过，都需要进行二审
     *  3.一审、二审皆通过，则整体通过，流程结束
     *    一审、二审皆不通过，则整体拒绝，流程结束
     *    一半通过，需进行终审
     *  4.终审通过，则整体通过，流程结束
     *    终审拒绝，则整体拒绝，流程结束
     *
     * 参考：<a href="https://confluence.fdgahl.cn/display/Technology/World+Check">产品文档
     */
    public void doAudit(){
        if (request.getForceFinal()){
            wckAudit.setStatus(WckStatus.AUDIT_THIRD);
            finished = false;
            return;
        }

        if (auditorSeq==WckConst.AUDIT_FIRST){
            wckAudit.setStatus(WckStatus.AUDIT_SECOND);
        }else if (auditorSeq==WckConst.AUDIT_SECOND){
            if (firstLog.getIsValid() && request.getIsValid()){
                finished = true;
                wckAudit.setStatus(WckStatus.PASSED);
            }else if (!firstLog.getIsValid() && !request.getIsValid()){
                finished = true;
                wckAudit.setStatus(WckStatus.REJECTED);
            }else {
                wckAudit.setStatus(WckStatus.AUDIT_THIRD);
            }
        }else if (auditorSeq==WckConst.AUDIT_THIRD){
            finished = true;
            if (request.getIsValid()){
                wckAudit.setStatus(WckStatus.PASSED);
            }else {
                wckAudit.setStatus(WckStatus.REJECTED);
            }
        }
        wckAudit.setUpdateTime(new Date());
    }

    public UserWckAudit getResult(){

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
        return wckAudit.getStatus() == WckStatus.PASSED;
    }

    public UserWckAuditLog getAuditLog(){
        return new UserWckAuditLog(request.getKycId(), request.getAuditorSeq(), request.getAuditorId(), request.getIsValid(), request.getMemo(),request.getIsPep(),request.getIsAdverse());
    }

    /**
     * 强制终审log
     */
    public UserWckAuditLog getForceFinalAuditLog(){
        if (request.getForceFinal()){
            return new UserWckAuditLog(request.getKycId(), WckConst.AUDIT_SECOND, request.getAuditorId(), true, "Skipped double-check",request.getIsPep(),request.getIsAdverse());
        }
        return null;
    }


    private WckAuditDataHolder(){}
}
