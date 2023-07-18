package com.binance.account.service.kyc;

import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;

import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;

import javax.annotation.Resource;

/**
 * KYC 认证流程中，成功或者失败时做出检查，如果成功到处理，需要触发
 */
@Log4j2
public abstract class AbstractKycFlowEndExecutor {

    @Resource
    protected KycCertificateMapper kycCertificateMapper;
    
    @Resource
    protected KycFillInfoMapper kycFillInfoMapper;

    /**
     * 后置处理器
     */
    public abstract void execute(Long userId);

    protected KycCertificate getMasterKycCertificate(Long userId) {
        if (userId == null) {
            return null;
        }
        HintManager hintManager = null;
        KycCertificate kycCertificate = null;
        try {
            // 防止下主从同步问题
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
        }catch (Exception e){
            log.error("get KYC Certificate from master db fail, userId:{}", userId);
        } finally {
            if (hintManager != null) {
                hintManager.close();
            }
        }
        return kycCertificate;
    }


}
