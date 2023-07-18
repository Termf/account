package com.binance.account.mq;

import com.alibaba.fastjson.JSON;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.JumioType;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.IUserKyc;
import com.binance.account.service.security.IUserFace;
import com.binance.account.service.security.impl.RiskManageBusiness;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioScanSource;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.risk.vo.OperationType;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Objects;

/**
 * @author liliang1
 * @date 2018-10-12 20:01
 */
@Log4j2
@Component
public class KycJumioInfoMsgExecutor {

    @Setter
    @Getter
    private class JumioStatusResult {
        private boolean checkResult = false;
        private Country country = null;
        private String msg = null;
    }

    @Resource
    private JumioMapper jumioMapper;
    @Resource
    private UserKycMapper userKycMapper;
    @Resource
    private CompanyCertificateMapper companyCertificateMapper;
    @Resource
    private IUserCertificate iUserCertificate;
    @Resource
    private CountryMapper countryMapper;
    @Resource
    private IUserKyc iUserKyc;
    @Resource
    private RiskManageBusiness riskManageBusiness;
    @Resource
    private TransactionFaceLogMapper transactionFaceLogMapper;
    @Resource
    private IUserFace iUserFace;

    public String executor(Long userId, String bizId, JumioHandlerType handlerType, JumioInfoVo jumioInfoVo) {
        JumioType type = JumioType.getByName(handlerType.getCode());
        if (type == null) {
            return "类型错误";
        }
        if (!JumioStatus.isEndStatus(jumioInfoVo.getStatus())) {
            //JUMIO 的状态不是最终态时，先不做处理
            log.info("JUMIO结果处理还未到最终态, userId:{} bizId:{} type:{}", userId, bizId, jumioInfoVo.getHandlerType());
            return "状态不处于终态";
        }
        //先根据Type he bizId(记录的ID) 和 userId 获取对应的kyc记录
        String jumioId = null;
        Long id = Long.valueOf(bizId);
        String scanRef = "";
        switch (type) {
            case user:
                UserKyc userKyc = userKycMapper.getById(userId, id);
                if (userKyc != null) {
                    jumioId = userKyc.getJumioId();
                    scanRef = userKyc.getScanReference();
                }
                break;
            case company:
                CompanyCertificate companyCertificate = companyCertificateMapper.selectByPrimaryKey(userId, id);
                if (companyCertificate != null) {
                    jumioId = companyCertificate.getJumioId();
                    scanRef = companyCertificate.getScanReference();
                }
                break;
            default:
                return null;
        }
        if (StringUtils.isBlank(jumioId) || !StringUtils.equalsAnyIgnoreCase(scanRef, jumioInfoVo.getScanReference(), jumioInfoVo.getMerchantReference())) {
            log.info("KYC JUMIO结果->根据业务编号和用户信息，获取对应的jumio_id 失败. userId:{} bizId:{}, type:{} scanRef:{}", userId, bizId, type, scanRef);
            return "获取业务信息失败";
        }
        Jumio jumio = jumioMapper.selectByPrimaryKey(userId, jumioId);
        if (jumio == null || jumio.getStatus() != null) {
            log.info("KYC JUMIO结果->根据 jumioId 获取对应的 JUMIO 信息失败或者状态已经不在未处理状态. userId:{} bizId:{} jumioId:{}", userId, bizId, jumioId);
            return "JUMIO信息获取失败或者状态错误";
        }
        // 下面条件只要有一个相同，就认为是同一笔数据
        if (!(StringUtils.equalsIgnoreCase(jumio.getScanReference(), jumioInfoVo.getScanReference())
                || StringUtils.equalsIgnoreCase(jumio.getMerchantReference(), jumioInfoVo.getMerchantReference()))) {
            log.info("KYC JUMIO结果->JUMIO的唯一标识不一致，不进行任何处理. userId:{} bizId:{} scanRef:{} infoScanRef:{}",
                    userId, bizId, jumio.getScanReference(), jumioInfoVo.getScanReference());
            return "JUMIO唯一标识不匹配";
        }
        //如果返回的信息状态是过期，直接按过期逻辑处理(过期意味着没有上传任何信息，这时候获取不到基础信息)
        if (Objects.equals(JumioStatus.EXPIRED, jumioInfoVo.getStatus())) {
            log.info("KYC JUMIO结果->jumio info 信息返回过期状态, 进行过期逻辑处理. userId:{} bizId:{} jumioId:{}", userId, bizId, jumioId);
            iUserCertificate.jumioExpireHandler(userId, jumio);
            return "JUMIO信息返回过期状态";
        }
        //获取国家码，如果国家码不存在的话，保存不了信息，这个时候，当过期处理
        Country country = null;
        if (StringUtils.isNotBlank(jumioInfoVo.getIssuingCountry())) {
            country = countryMapper.selectByCode2(jumioInfoVo.getIssuingCountry());
        }
        if (country == null) {
            log.error("KYC JUMIO结果->获取的国家编码不存在，无法保存用户证件信息: userId:{} bizId:{} issuingCountry:{}", userId, bizId, jumioInfoVo.getIssuingCountry());
            iUserCertificate.jumioExpireHandler(userId, jumio);
            return "JUMIO解析的国家编码不存在";
        }
        //触发下风控的逻辑
        final String countryCode = country.getCode();
        final String number = jumioInfoVo.getNumber();
        final String documentType = jumioInfoVo.getDocumentType();
        final String jumioFacePath = jumioInfoVo.getFace();
        AsyncTaskExecutor.execute(() -> riskManageBusiness.checkIdNumberBackList(userId, documentType, number, countryCode, OperationType.IDENTITY_AUTHENTICATION, jumioFacePath));
        AsyncTaskExecutor.execute(() -> riskManageBusiness.indexFaceIfNeeded(jumioInfoVo.getFront()));


        //如果都正常的话，根据消息信息进行对JUMIO信息进行赋值和保存.
        setJumioByMessageInfo(jumio, jumioInfoVo, country);
        //设置状态值
        JumioStatusResult statusResult = setJumioStatus(userId, bizId, jumio, jumioInfoVo);
        //把数据保存到库表中
        int row = jumioMapper.updateByPrimaryKeySelective(jumio);
        log.info("KYC JUMIO结果->JUMIO 解析结果: userId:{} bizId:{} statusResult:{}", userId, bizId, JSON.toJSONString(statusResult));
        boolean jumioPassed = statusResult.isCheckResult();
        String msg = statusResult.getMsg();
        if (row > 0) {
            switch (jumio.getType()) {
                case company:
                    iUserCertificate.handleCompanyCertificate(jumio, jumioPassed, msg);
                    break;
                case user:
                    iUserKyc.handleUserKyc(jumio, jumioPassed, msg);
                    break;
                default:
                    return "类型错误";
            }
        } else {
            log.error("KYC JUMIO结果->保存JUMIO信息失败. userId:{} bizId:{}", userId, bizId);
            return "JUMIO信息保存失败";
        }
        if (jumioPassed) {
            // jumio pass 情况下，检查当前流程是否已经发起过人脸识别，如果未有，进行发起人脸识别, SDK 的jumio 不要需要人脸识别
            if (!JumioScanSource.SDK.name().equalsIgnoreCase(jumioInfoVo.getSource())) {
                FaceTransType transType = jumio.getType() == JumioType.user ? FaceTransType.KYC_USER : FaceTransType.KYC_COMPANY;
                TransactionFaceLog faceLog = transactionFaceLogMapper.findByTransId(bizId, transType.name());
                if (faceLog == null) {
                    log.info("KYC JUMIO结果->KYC认证流程还未发起人脸识别，进行发起人脸识别认证流程. userId:{} transId:{} transType:{}", userId, bizId, transType);
                    iUserFace.initFaceFlowByTransId(bizId, userId, transType, true, false);
                }
            }
        }
        return null;
    }

    /**
     * JUMIO模块化后，不再需要判断具体信息，直接根据JUMIO通过或者拒绝的状态确认最后的状态
     *
     * @param userId
     * @param bizId
     * @param jumio
     * @param jumioInfoVo
     * @return
     */
    public JumioStatusResult setJumioStatus(Long userId, String bizId, Jumio jumio, JumioInfoVo jumioInfoVo) {
        JumioStatusResult statusResult = new JumioStatusResult();
        String scanRef = jumioInfoVo.getScanReference();
        if (Objects.equals(JumioStatus.PASSED, jumioInfoVo.getStatus())) {
            log.info("JUMIO 模块化审核通过. userId:{} bizId:{} scanRef:{}", userId, bizId, scanRef);
            jumio.setStatus(com.binance.account.common.enums.JumioStatus.jumioPassed);
            statusResult.setCheckResult(true);
        } else {
            log.info("JUMIO 模块化审核拒绝. userId:{} bizId:{} scanRef:{}", userId, bizId, scanRef);
            jumio.setStatus(com.binance.account.common.enums.JumioStatus.jumioRefused);
            statusResult.setMsg(jumioInfoVo.getFailReason());
            statusResult.setCheckResult(false);
        }
        return statusResult;
    }

    /**
     * 设置基础信息
     *
     * @param jumio
     * @param jumioInfoVo
     */
    public void setJumioByMessageInfo(Jumio jumio, JumioInfoVo jumioInfoVo, Country country) {
        jumio.setFirstName(jumioInfoVo.getFirstName());
        jumio.setLastName(jumioInfoVo.getLastName());
        jumio.setDob(jumioInfoVo.getDob());
        jumio.setAddress(jumioInfoVo.getAddress());
        jumio.setPostalCode(jumioInfoVo.getPostalCode());
        jumio.setCity(jumioInfoVo.getCity());
        jumio.setIssuingCountry(country.getCode());
        jumio.setExpiryDate(jumioInfoVo.getExpiryDate());
        jumio.setNumber(jumioInfoVo.getNumber());
        jumio.setDocumentType(jumioInfoVo.getDocumentType());
        jumio.setSource(jumioInfoVo.getSource());
        jumio.setFront(jumioInfoVo.getFront());
        jumio.setBack(jumioInfoVo.getBack());
        jumio.setFace(jumioInfoVo.getFace());
    }
}
