package com.binance.account.service.kyc.executor.us;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.JumioInitValidator;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.JumioInitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.util.InspectorErrorCode;
import com.binance.inspector.vo.jumio.response.InitJumioResponse;
import com.binance.inspector.vo.jumio.response.InitSdkJumioResponse;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Objects;

@Log4j2
@Service
public class JumioInitExecutor extends AbstractKycFlowCommonExecutor {

    @Resource
    private JumioBusiness jumioBusiness;
    @Resource
    private KycCertificateMapper kycCertificateMapper;
    
    @Autowired
    private JumioInitValidator validator;

    @Override
    public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
        Long userId = kycFlowRequest.getUserId();
        KycCertificateKycType kycType = kycFlowRequest.getKycType();
        // 先检查下用户的当前 jumio 认证状态，如果已经验证完成的，则不能再做
        KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
        if (kycCertificate == null) {
            kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
        }
        if (kycCertificate == null) {
            log.warn("KYC认证JUMIO => 获取不到用户KYC认证记录信息. userId:{}", userId);
            throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
        }
        
        validator.validateKycCertificateStatus(kycCertificate);
        validator.validateRequestCount(kycCertificate);
        
        // 尝试初始化jumio信息
        try {
            boolean isSdk = Objects.equals(TerminalEnum.ANDROID, kycFlowRequest.getSource()) || Objects.equals(TerminalEnum.IOS, kycFlowRequest.getSource());
            KycFlowResponse flowResponse;
            if (isSdk) {
                flowResponse = initSdkJumio(userId, kycType);
            } else {
                flowResponse = initWebJumio(userId, kycType);
            }
            // 变更jumio 状态到处理中
            kycCertificate.setJumioStatus(KycCertificateStatus.PROCESS.name());
            kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
            kycCertificateMapper.updateJumioStatus(kycCertificate);
            return flowResponse;
        } catch (BusinessException e) {
            log.warn("KYC认证JUMIO => 初始化失败. userId:{}", userId, e);
            // 需要判断错误的原因，如果是已经通过，则直接变更到通过，如果是review 直接变更到review, 其他到错误不管，直接抛出上层
            if (Objects.equals(InspectorErrorCode.JUMIO_INIT_PASSED_ERROR.getCode(), e.getBizCode())) {
                // JUMIO 认证已经通过.
                kycCertificate.setJumioStatus(KycCertificateStatus.PASS.name());
                kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
                kycCertificateMapper.updateJumioStatus(kycCertificate);
                log.info("KYC认证JUMIO => jumio认证已经通过，不能再做. userId:{}", userId);
                throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_PASS);
            } else if (Objects.equals(InspectorErrorCode.JUMIO_INIT_REVIEW_ERROR.getCode(), e.getBizCode())) {
                // JUMIO 认证正在审核中
                kycCertificate.setJumioStatus(KycCertificateStatus.REVIEW.name());
                kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
                kycCertificateMapper.updateJumioStatus(kycCertificate);
                log.info("KYC认证JUMIO => jumio认证正在审核中. userId:{}", userId);
                throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_IN_REVIEW);
            } else {
                throw e;
            }
        }
    }

    private KycFlowResponse initWebJumio(Long userId, KycCertificateKycType kycType) {
        JumioInitResponse response = new JumioInitResponse();
        response.setUserId(userId);
        response.setKycType(kycType);
        final String flowId = UUIDUtils.getId();
        JumioHandlerType handlerType = KycCertificateKycType.COMPANY != kycType ? JumioHandlerType.USER_KYC : JumioHandlerType.COMPANY_KYC;
        InitJumioResponse jumioResponse = jumioBusiness.initWebJumioWithoutSave(userId, handlerType, flowId, true);
        String jumioRedirectUrl = jumioResponse == null ? null : jumioResponse.getRedirectUrl();
        if (StringUtils.isBlank(jumioRedirectUrl)) {
            log.error("KYC认证JUMIO => init jumio web url fail. userId: userId:{} kycType:{} flowId", userId, kycType, flowId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        response.setRedirectUrl(jumioRedirectUrl);
        return response;
    }

    private KycFlowResponse initSdkJumio(Long userId, KycCertificateKycType kycType) {
        JumioInitResponse response = new JumioInitResponse();
        response.setUserId(userId);
        response.setKycType(kycType);
        final String flowId = UUIDUtils.getId();
        JumioHandlerType handlerType = KycCertificateKycType.COMPANY != kycType ? JumioHandlerType.USER_KYC : JumioHandlerType.COMPANY_KYC;
        InitSdkJumioResponse jumioResponse = jumioBusiness.initSdkJumioWithoutSave(userId, handlerType, flowId, true);
        if (jumioResponse == null) {
            log.error("KYC认证JUMIO => init jumio web url fail. userId: userId:{} kycType:{} flowId", userId, kycType, flowId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        response.setApiKey(jumioResponse.getApiKey());
        response.setApiSecret(jumioResponse.getApiSecret());
        response.setMerchantReference(jumioResponse.getMerchantReference());
        response.setUserReference(jumioResponse.getUserReference());
        response.setCallBack(jumioResponse.getCallBack());
        return response;
    }


}
