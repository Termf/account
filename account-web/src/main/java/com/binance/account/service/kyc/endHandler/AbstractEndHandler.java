package com.binance.account.service.kyc.endHandler;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserCertificateIndex;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.KycTaxidIndexMapper;
import com.binance.account.data.mapper.certificate.UserCertificateIndexMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.certificate.impl.JumioBusiness;
import com.binance.account.service.certificate.impl.UserCertificateBusiness;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.kyc.executor.KycEndContext;
import com.binance.account.service.security.IFace;
import com.binance.account.service.subuser.ISubUserAdmin;
import com.binance.account.service.user.IUserKycEmailNotify;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.country.CountryVo;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.inspector.common.enums.JumioDocumentType;
import com.binance.inspector.common.enums.JumioError;
import com.binance.inspector.common.enums.JumioHandlerType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.request.ChangeStatusRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.report.api.USCommissionApi;
import com.google.common.collect.Maps;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Log4j2
public abstract class AbstractEndHandler {

    @Resource
    protected KycCertificateMapper kycCertificateMapper;
    @Resource
    protected IUserCertificate iUserCertificate;
    @Resource
    protected IMsgNotification iMsgNotification;
    @Resource
    protected UserCertificateBusiness userCertificateBusiness;
    @Resource
    protected UserCertificateIndexMapper userCertificateIndexMapper;
    @Resource
    protected JumioBusiness jumioBusiness;
    @Resource
    protected ICountry iCountry;
    @Resource
    protected IFace iFace;
    @Resource
    protected UserCommonBusiness userCommonBusiness;
    @Resource
    protected UserIndexMapper userIndexMapper;
    @Resource
    protected UserMapper userMapper;
    @Autowired
    protected ApolloCommonConfig config;
    
    @Resource
    protected KycFillInfoMapper kycFillInfoMapper;
    
    @Resource
    protected KycTaxidIndexMapper kycTaxidIndexMapper;
    
    @Resource
    protected IUserKycEmailNotify iUserKycEmailNotify;
    @Resource
    protected ApolloCommonConfig apolloCommonConfig;
    @Resource
    protected ISubUserAdmin iSubUserAdmin;

    @Resource
    protected USCommissionApi uSCommissionApi;
    
    @Resource
    protected ApplicationEventPublisher applicationEventPublisher;

    /**
     * 是否执行
     *
     * @return
     */
    public abstract boolean isDoHandler();

    /**
     * 处理逻辑
     *
     */
    public abstract void handler();


    /**
     * 变更用户KYC认证状态和等级信息
     *
     * @param userId
     * @param status
     * @param messageTips
     * @return 变更条数
     */
    protected int updateKycFlowStatus(Long userId, Integer kycLevel, KycCertificateStatus status, String messageTips) {
        KycCertificate kycCertificate = new KycCertificate();
        kycCertificate.setUserId(userId);
        kycCertificate.setStatus(status == null ? null : status.name());
        kycCertificate.setKycLevel(kycLevel);
        kycCertificate.setMessageTips(messageTips);
        kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
        return kycCertificateMapper.updateStatus(kycCertificate);
    }

    /**
     * 变更用户安全等级和发送变更消息
     *
     * @param userId
     * @return
     */
    protected int updateSecurityLevel(Long userId, int securityLevel) {
        log.info("KYC -> 变更用户安全等级信息: userId:{} securityLevel:{}", userId, securityLevel);
        int row = iUserCertificate.updateSecurityLevel(userId, securityLevel);
        // 修改用户等级消息通知 start
        Map<String, Object> dataMsg = new HashMap<>();
        dataMsg.put("userId", userId);
        dataMsg.put("level", securityLevel);
        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
        log.info("KYC -> iMsgNotification security level:{}", JSON.toJSONString(msg));
        this.iMsgNotification.send(msg);
        return row;
    }

    /**
     * 设置为母账户（开启子母账户功能）
     * @param userId
     */
    protected void enableSubUserFunction(Long userId) {
        if (!apolloCommonConfig.isAutoEnableSubUserFunction()) {
            return;
        }
        try {
            APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
            ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
            parentUserIdReq.setParentUserId(userId);
            subUserReq.setBody(parentUserIdReq);
            APIResponse<Boolean> result = iSubUserAdmin.enableSubUserFunction(subUserReq);
            log.info("EndHandler autoEnableSubUserFunction parentUserId:{}, result:{}", userId, result);
        } catch (Exception e) {
            log.error("EndHandler autoEnableSubUserFunction error, parentUserId:{}, msg:", userId, e);
        }
    }

    /**
     * 设置为普通账户(关闭子母账户功能)
     * @param userId
     */
    protected void disableSubUserFunction(Long userId) {
        try {
            APIRequest<ParentUserIdReq> subUserReq = new APIRequest<>();
            ParentUserIdReq parentUserIdReq = new ParentUserIdReq();
            parentUserIdReq.setParentUserId(userId);
            subUserReq.setBody(parentUserIdReq);
            APIResponse<Boolean> result = iSubUserAdmin.disableSubUserFunction(subUserReq);
            log.info("EndHandler disableSubUserFunction parentUserId:{}, result:{}", userId, result);
        } catch (Exception e) {
            log.error("EndHandler disableSubUserFunction error, parentUserId:%s, msg:", userId, e);
        }
    }

    /**
     * kyc 认证状态变更通知邮件
     *
     * @param userId
     * @param language
     * @param reason
     * @param emailTemplateCode
     * @param remark
     */
    protected void sendLevelChangeEmail(Long userId, LanguageEnum language, String reason, String emailTemplateCode, String remark) {
        try {
            UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
            final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
            language = language == null ? LanguageEnum.EN_US : language;
            // 发送提醒邮件
            Map<String, Object> data = Maps.newHashMap();
            if (language == LanguageEnum.ZH_CN) {
                String reasonMsg = userCommonBusiness.getJumioFailReason(reason, true);
                if (StringUtils.isNotBlank(reasonMsg)) {
                    data.put("reason", reasonMsg);
                }
                userCommonBusiness.sendEmailWithoutRequest(emailTemplateCode, dbUser, data, remark, LanguageEnum.ZH_CN);
            } else {
                String reasonMsg = userCommonBusiness.getJumioFailReason(reason, false);
                if (StringUtils.isNotBlank(reasonMsg)) {
                    data.put("reason", reasonMsg);
                }
                userCommonBusiness.sendEmailWithoutRequest(emailTemplateCode, dbUser, data, remark, LanguageEnum.EN_US);
            }
            //L0-L1 添加邮件trade通知任务。72小时后任务捞起去通知用户，
            if(UserConst.US_KYC_EMAIL_L0_TO_L1.equals(emailTemplateCode)) {
            	iUserKycEmailNotify.addTradeNotifyTask(userId, userIndex.getEmail());
            }
        } catch (Exception e) {
            log.error("kyc 等级变更邮件发送失败. userId:{}", userId, e);
        }
    }

    protected JumioInfoVo getLastJumio(Long userId) {
        return jumioBusiness.getLastByUserId(userId);
    }

    /**
     * 判断jumio的证件是否被别的用户占用了
     *
     * @param jumioInfoVo
     * @return
     */
    protected boolean isIdNumberUsedByOther(JumioInfoVo jumioInfoVo, CountryVo country) {
        if (jumioInfoVo == null || country == null) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        String idNumber = jumioInfoVo.getNumber();
        String countryCode = null;
        if (country != null) {
            countryCode = country.getCode();
        }
        String idType = jumioInfoVo.getDocumentType();
        Long userId = jumioInfoVo.getUserId();
        // 添加一个交易规则，如果是企业用户，直接忽略当前交易
        if (JumioHandlerType.COMPANY_KYC.equals(jumioInfoVo.getHandlerType())) {
            return false;
        }
        return userCertificateBusiness.isIDNumberOccupied(idNumber, countryCode, idType, userId);
    }

    /**
     * 保存证件绑定关系
     *
     * @param kycType
     * @param jumioInfoVo
     * @param country
     */
    protected void saveIdNumberMapIndex(KycCertificateKycType kycType, JumioInfoVo jumioInfoVo, CountryVo country) {
        if (country == null || jumioInfoVo == null || jumioInfoVo.getUserId() == null ||
                StringUtils.isAnyBlank(jumioInfoVo.getIssuingCountry(), jumioInfoVo.getDocumentType(), jumioInfoVo.getIssuingCountry())) {
            log.warn("Jumio信息缺失. userId:{} jumioId:{}", jumioInfoVo.getUserId(), jumioInfoVo.getId());
            return;
        }
        UserCertificateIndex index = new UserCertificateIndex();
        index.setUserId(jumioInfoVo.getUserId());
        index.setNumber(jumioInfoVo.getNumber());
        index.setType(jumioInfoVo.getDocumentType());
        index.setCountry(country.getCode());
        index.setCertificateType(kycType.getCode());
        index.setCreateTime(DateUtils.getNewUTCDate());
        userCertificateIndexMapper.insertIgnore(index);
    }

    protected int saveIdNumberMapIndex(KycEndContext endContext) {
        if (KycCertificateKycType.COMPANY ==endContext.getKycType()) {
            // 企业用户不再保存 id number index 信息
            return 0;
        }
        if (StringUtils.isAnyBlank(endContext.getCountry(), endContext.getDocumentType(), endContext.getIdNumber())) {
            log.warn("id number index save info miss. userId:{}", endContext.getUserId());
            return 0;
        }
        UserCertificateIndex index = new UserCertificateIndex();
        index.setUserId(endContext.getUserId());
        index.setNumber(endContext.getIdNumber());
        index.setType(endContext.getDocumentType());
        index.setCountry(endContext.getCountry());
        index.setCertificateType(endContext.getKycType().getCode());
        index.setCreateTime(DateUtils.getNewUTCDate());
        return userCertificateIndexMapper.insertIgnore(index);
    }

    /**
     * 清除证件绑定信息
     *
     * @param jumioInfoVo
     * @param country
     * @return
     */
    protected int removeIdNumberMapIndex(JumioInfoVo jumioInfoVo, CountryVo country) {
        if (country == null || jumioInfoVo == null || jumioInfoVo.getUserId() == null ||
                StringUtils.isAnyBlank(jumioInfoVo.getIssuingCountry(), jumioInfoVo.getDocumentType(), jumioInfoVo.getIssuingCountry())) {
            log.warn("Jumio信息缺失. userId:{} jumioId:{}", jumioInfoVo.getUserId(), jumioInfoVo.getId());
            return 0;
        }
        return userCertificateIndexMapper.deleteByPrimaryKey(jumioInfoVo.getNumber(), country.getCode(), jumioInfoVo.getDocumentType(), jumioInfoVo.getUserId());
    }

    /**
     * 把jumio进行拒绝
     *
     * @param jumioInfoVo
     */
    protected void refusedJumioByNumberUsed(JumioInfoVo jumioInfoVo) {
        if (jumioInfoVo == null) {
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        ChangeStatusRequest request = new ChangeStatusRequest();
        request.setJumioId(jumioInfoVo.getId());
        request.setUserId(jumioInfoVo.getUserId());
        request.setStatus(JumioStatus.REFUED);
        request.setFailReason(JumioError.ID_NUMBER_USED.name());
        request.setNotifyChange(false);
        jumioBusiness.changeJumioStatus(request);
    }

    protected void refusedFaceOcrByNumberUsed(FaceIdCardOcrVo faceIdCardOcrVo) {
        // todo 请求重置ocr 状态
    }
    
    protected KycFillInfo getMasterKycFillInfo(Long userId,KycFillType type) {
        if (userId == null) {
            return null;
        }
        HintManager hintManager = null;
        KycFillInfo kycFillInfo = null;
        try {
            // 防止下主从同步问题
            hintManager = HintManager.getInstance();
            hintManager.setMasterRouteOnly();
            kycFillInfo =kycFillInfoMapper.selectByUserIdFillType(userId, type.name());
        }catch (Exception e){
            log.error("get KYC Certificate from master db fail, userId:{}", userId);
        } finally {
            if (hintManager != null) {
                hintManager.close();
            }
        }
        return kycFillInfo;
    }

    
    public void initContext(Long userId) {
        KycEndContext context = KycEndContext.getContext();
    	 KycCertificate kycCertificate = context.getKycCertificate();
    	 if(kycCertificate == null) {
    		 kycCertificate = getMasterKycCertificate(userId);
    		 if(kycCertificate == null ) {
    			 return;
    		 }
             context.setKycCertificate(kycCertificate);
             context.setUserId(kycCertificate.getUserId());
             context.setKycType(KycCertificateKycType.getByCode(kycCertificate.getKycType()));
    	 }
         kycCertificate = KycEndContext.getContext().getKycCertificate();
    	 if (StringUtils.isNotBlank(kycCertificate.getJumioStatus()) && KycEndContext.getContext().getJumioInfoVo() == null) {
    	     // jumio 流程，初始化jumio 的信息
             JumioInfoVo jumioInfoVo = getLastJumio(userId);
             if (jumioInfoVo != null) {
                 context.setJumioInfoVo(jumioInfoVo);
                 context.setOcrFlow(false);
                 CountryVo countryVo = iCountry.getCountryByAlpha3(jumioInfoVo.getIssuingCountry());
                 context.setCountry(countryVo == null ? null : countryVo.getCode());
                 context.setIdNumber(jumioInfoVo.getNumber());
                 context.setDocumentType(jumioInfoVo.getDocumentType());
                 context.setLanguage(jumioInfoVo == null ? LanguageEnum.EN_US : jumioInfoVo.getBaseLanguage());
             }
         }
    	 if (StringUtils.isNotBlank(kycCertificate.getFaceOcrStatus()) && KycEndContext.getContext().getFaceIdCardOcrVo() == null) {
    	     // ocr 流程，初始化 ocr 信息
             FaceIdCardOcrVo faceIdCardOcrVo = iFace.getFaceIdCardOcr(userId);
             if (faceIdCardOcrVo != null) {
                 context.setFaceIdCardOcrVo(faceIdCardOcrVo);
                 context.setOcrFlow(true);
                 context.setCountry("CN"); // ocr 流程的国籍固定为中国
                 context.setIdNumber(faceIdCardOcrVo.getIdcardNumber());
                 context.setDocumentType(JumioDocumentType.ID_CARD.name());
                 context.setLanguage(LanguageEnum.ZH_CN); // ocr 流程语言固定在中文
             }
         }
    	 // 是否不合规国籍
         boolean isForbidCountry = apolloCommonConfig.isKycPassForbidCountry(KycEndContext.getContext().getCountry());
        context.setForbidCountry(isForbidCountry);
    }
    
    
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
