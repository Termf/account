package com.binance.account.service.security.impl;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.constant.UserConst;
import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.enums.WithdrawFaceTipStatus;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificateResult;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.face.FaceHandlerHelper;
import com.binance.account.service.face.handler.WithdrawFaceHandler;
import com.binance.account.service.security.IWithdrawSecurityFace;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.utils.MessageUtils;
import com.binance.account.vo.security.request.SecurityFaceStatusRequest;
import com.binance.account.vo.withdraw.request.WithdrawFaceInHoursRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawFaceTipResponse;
import com.binance.inspector.common.enums.FaceStatus;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.TrackingUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * @author liliang1
 * @date 2018-11-29 11:23
 */
@Log4j2
@Service
public class WithdrawSecurityFaceBusiness implements IWithdrawSecurityFace {

    @Resource
    private UserSecurityMapper userSecurityMapper;
    @Resource
    private UserCommonBusiness userCommonBusiness;
    @Resource
    private MessageUtils messageUtils;
    @Resource
    private ApolloCommonConfig commonConfig;
    @Resource
    private WithdrawFaceHandler withdrawFaceHandler;
    @Resource
    private TransactionFaceLogMapper transactionFaceLogMapper;
    @Resource
    private FaceHandlerHelper faceHandlerHelper;

    @Override
    public Integer changeWithdrawSecurityFaceStatus(SecurityFaceStatusRequest statusRequest) {
        Integer changeStatus = statusRequest.getWithdrawSecurityFaceStatus();
        final Long userId = statusRequest.getUserId();
        final boolean needEmail = statusRequest.isNeedEmail();
        log.info("请求变更用户提现是否需要做人脸识别标识: userId:{} status:{}", userId, changeStatus);
        if (!UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO.equals(changeStatus) && !UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO.equals(changeStatus)) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        // 在开关关闭的情况下也能直接修改为不需要操作人脸识别的标识，如果是要标识为需要人脸识别，需要校验开关
        if (!commonConfig.isWithdrawFaceSwitchOn() && UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO.equals(changeStatus)) {
            log.info("当前提现风控是否需要操作人脸规则功能开关是关闭状态, 不能开启人脸识别请求. userId:{}",userId);
            throw new BusinessException(AccountErrorCode.WITHDRAW_FACE_SWITCH_CLOSED);
        }
        if (UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO.equals(changeStatus)) {
            int row = withdrawFaceHandler.changeWithdrawFaceStatus(userId, changeStatus, null, null, needEmail);
            log.info("变更用户提现人脸识别标识为关闭成功. userId:{} row:{}", userId, row);
            //查看当前人脸识别的业务标识是否正在处理中，如果时直接修改到终态通过
            directPassWithdrawTransFace(userId, "强制关闭提币人脸识别标识");
            withdrawFaceHandler.deleteRiskBlackListByUserId(userId, null,null,null,null,null,null);
            return row;
        }
        log.info("开启用户需要做提币人脸识别逻辑: userId:{}", userId);
        int row = withdrawFaceHandler.changeWithdrawFaceStatus(userId, changeStatus, UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO, null, needEmail);
        if (row <= 0) {
            log.info("用户当前的禁用提币人脸识别标识已经为开启人脸识别状态，userId:{}", userId);
            throw new BusinessException(AccountErrorCode.WITHDRAW_FACE_OPEN_FAIL_OR_ALREADY_OPEN);
        }else {
            try {
                log.info("提币标识变更为开启状态成功，发起用户提币人脸识别流程. userId:{}", userId);
                withdrawFaceHandler.initWithdrawFaceByUserId(userId, statusRequest.getWithdrawId(),statusRequest.getSource());
                return row;
            }catch (Exception e) {
                throw new BusinessException(AccountErrorCode.WITHDRAW_FACE_FLOW_INIT_FAIL);
            }
        }
    }

    /**
     * 强制把最后一笔的提币人脸识别设置为通过状态
     * @param userId
     */
    private TransactionFaceLog directPassWithdrawTransFace(Long userId, String failReason) {
        TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(userId, FaceTransType.WITHDRAW_FACE.name(), null);
        if (faceLog != null && !faceLog.isEndStatus()) {
            log.info("当前用户的提币人脸标识被强制通过，直接终止该人脸识别业务. userId:{} transId:{}", userId, faceLog.getTransId());
            updateWithdrawFaceLog(faceLog, TransFaceLogStatus.PASSED, failReason);
        }
        return faceLog;
    }

    /**
     * KYC认证通过时，检查是否需要触发提币人脸识别流程
     * 启动一个线程检查这个操作流程
     * @param userId
     * @param transId kyc的记录ID
     * @param transType
     * @param transFaceLogId 如果kcy记录关联有提币人脸信息，需要带上, 用于风控黑名单删除
     */
    @Override
    public void kycPassCheckSecurityFaceCheck(final Long userId, final String transId, FaceTransType transType, String transFaceLogId,String kycStatus,Date kycPassTime) {
        final String track = TrackingUtils.getTrackingChain();
        AsyncTaskExecutor.execute(() -> {
            if (StringUtils.isBlank(track)) {
                TrackingUtils.putTracking("KYC_PASS_WITHDRAW_FACE", UUID.randomUUID().toString().replaceAll("-", ""));
            }else {
                TrackingUtils.putTracking(track);
            }
            try {
                // 先检查开关
                if (!commonConfig.isWithdrawFaceSwitchOn()) {
                    //如果开启未开启，直接退出
                    return;
                }
                UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
                if (userSecurity == null) {
                    throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户安全信息失败");
                }

                if (UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO.equals(userSecurity.getWithdrawSecurityFaceStatus())) {
                    log.info("KYC 认证通过时, 关闭用户的提币人脸识别标识并删除用户风控黑名单. userId:{} transId:{}", userId, transId);
                    withdrawFaceHandler.changeWithdrawFaceStatus(userId, UserConst.WITHDRAW_SECURITY_FACE_STATUS_UNDO, null, null, true);
                    directPassWithdrawTransFace(userId, "KYC通过引起关闭提币人脸识别");
                }
                withdrawFaceHandler.deleteRiskBlackListByUserId(userId, transFaceLogId,transId,transType,null,kycStatus,kycPassTime);
                if(commonConfig.getWithdrawFaceDeleteBlackListSwitch() != 3) {
               	 	TransactionFaceLog transactionFaceLog = withdrawFaceHandler.getByMasterdb(transId, transType);
               	 	if (transactionFaceLog != null && FaceStatus.FACE_PASS.name().equalsIgnoreCase(transactionFaceLog.getFaceStatus())) {
                        log.info("KYC 认证通过且人脸识别通过，推送人脸信息到大数据: userId:{} transId:{}", userId, transId);
                        // 获取这次KYC通过的人脸识别信息推送到大数据
                        withdrawFaceHandler.pullRiskByAudit(transId, userId, null, transFaceLogId);
                    }
               }

            }catch (Exception e) {
                log.error("KYC 认证通过后检查提币人脸标识变更异常. userId:{}", userId, e);
            }finally {
                TrackingUtils.removeTracking();
            }
        });
    }

    private void updateWithdrawFaceLog(TransactionFaceLog transactionFaceLog, TransFaceLogStatus status, String failReason) {
        transactionFaceLog.setStatus(status);
        transactionFaceLog.setFailReason(failReason);
        transactionFaceLog.setUpdateTime(DateUtils.getNewUTCDate());
        transactionFaceLogMapper.updateByPrimaryKeySelective(transactionFaceLog);
    }

    @Override
    public UserWithdrawFaceTipResponse checkWithdrawFaceStatus(Long userId) {
        if (userId == null) {
            log.info("请求参数错误. userId is null");
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        //判断功能开关，如果开关关闭，直接发布不需要验证
        if (!commonConfig.isWithdrawFaceSwitchOn()) {
            log.info("当前提现风控人脸识别功能开关为关闭状态. userId:{}", userId);
            return new UserWithdrawFaceTipResponse();
        }
        // 看下当前的提现风控是否为需要做人脸识别的标识
        UserSecurity userSecurity = userSecurityMapper.selectByPrimaryKey(userId);
        if (userSecurity == null) {
            log.info("获取用户安全信息识别. userId:{}", userId);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        if (userSecurity.getWithdrawSecurityFaceStatus() == null || !UserConst.WITHDRAW_SECURITY_FACE_STATUS_DO.equals(userSecurity.getWithdrawSecurityFaceStatus())) {
            log.info("用户没有被要求进行提现人脸操作. userId:{}", userId);
            return new UserWithdrawFaceTipResponse();
        }
        // 如果开关已经开启，先查询当前用户是否已经通过的KYC认证
        KycCertificateResult certificateResult = userCommonBusiness.getKycStatusByUserId(userId);
        if (!faceHandlerHelper.canSkipKycUpload(certificateResult)) {
            log.info("用户KYC认证未完成, 需要先完成KYC认证. userId:{}", userId);
            String tipMessage = checkWithdrawFaceTip(WithdrawFaceTipStatus.NEED_KYC);
            return new UserWithdrawFaceTipResponse(WithdrawFaceTipStatus.NEED_KYC.getCode(), tipMessage);
        }
        log.info("用户KYC已经验证通过, 进行检查当前提现人脸审核状态, userId:{}", userId);
        TransactionFaceLog transactionFaceLog = transactionFaceLogMapper.findLastByUserId(userId, FaceTransType.WITHDRAW_FACE.name(), null);
        if (transactionFaceLog == null || transactionFaceLog.getStatus() == null) {
            log.error("用户提现人脸标识已经打开，但获取人脸业务记录获取失败. userId:{} transType:{}", userId, FaceTransType.WITHDRAW_FACE.name());
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        WithdrawFaceTipStatus tipStatus;
        switch (transactionFaceLog.getStatus()) {
            case PENDING:
                tipStatus = WithdrawFaceTipStatus.EMAIL_NOTIFY;
                break;
            case PASSED:
                tipStatus = WithdrawFaceTipStatus.NORMAL;
                break;
            case REVIEW:
                tipStatus = WithdrawFaceTipStatus.WAIT_AUDIT;
                break;
            case FAIL:
            case EXPIRED:
                tipStatus = WithdrawFaceTipStatus.FACE_REFUSED;
                break;
            default:
                tipStatus = WithdrawFaceTipStatus.WAIT_AUDIT;
                break;
        }
        log.info("用户提现人脸业务当前提示信息类型: userId:{} tipStatus:{}", userId, tipStatus);
        UserWithdrawFaceTipResponse tipResponse = new UserWithdrawFaceTipResponse(tipStatus.getCode(), checkWithdrawFaceTip(tipStatus));
        if (TransFaceLogStatus.PENDING.equals(transactionFaceLog.getStatus())) {
            tipResponse.setId(transactionFaceLog.getTransId());
            tipResponse.setType(FaceTransType.WITHDRAW_FACE.getCode());
            tipResponse.setQrCode(AccountConstants.KYC_FL_PREFIX+":"+FaceTransType.WITHDRAW_FACE.getCode()+":"+transactionFaceLog.getTransId());
        }
        return tipResponse;
    }

    /**
     * 各种情况下的提现风控提示语信息
     * @param tipStatus
     * @param reason 错误
     * @return
     */
    private String checkWithdrawFaceTip(WithdrawFaceTipStatus tipStatus, String... reason) {
        if (tipStatus == null) {
            return null;
        }
        switch (tipStatus) {
            case NORMAL:
                return null;
            case NEED_KYC:
                return messageUtils.getMessage(AccountErrorCode.WITHDRAW_FACE_NEED_KYC, reason);
            case EMAIL_NOTIFY:
                return messageUtils.getMessage(AccountErrorCode.WITHDRAW_FACE_PENDING, reason);
            case WAIT_AUDIT:
                return messageUtils.getMessage(AccountErrorCode.WITHDRAW_FACE_WAIT_AUDIT, reason);
            case FACE_REFUSED:
                return messageUtils.getMessage(AccountErrorCode.WITHDRAW_FACE_REFUSED, reason);
            default:
                return null;
        }
    }

    @Override
    public boolean checkWithdrawFaceInHours(WithdrawFaceInHoursRequest request) {
        if (request == null || request.getUserId() == null) {
            throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
        }
        Long userId = request.getUserId();
        //默认不比
        int hours = -1;
        if (request.getHours() == null) {
            // 没传时间，则使用系统配置值
            hours = commonConfig.getWithdrawFaceLastCompareHours();
        }else {
            hours = request.getHours();
        }
        if (hours <= 0) {
            // 比较小时小于等于0时认为直接不在设定范围内, 不比较
            return false;
        }
        List<String> types = Arrays.asList(FaceTransType.KYC_USER.name(), FaceTransType.KYC_COMPANY.name(), FaceTransType.WITHDRAW_FACE.name());
        TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserIdMultipleType(userId, types, TransFaceLogStatus.PASSED);
        if (faceLog == null || faceLog.getUpdateTime() == null) {
            // 如果没有通过的记录, 直接认为不在设定时间内
            return false;
        }
        boolean result = DateUtils.getNewUTCDate().before(DateUtils.add(faceLog.getUpdateTime(), Calendar.HOUR_OF_DAY, hours));
        log.info("校验上次提币人脸识别通过的时间对比: userId:{} transId:{} hours:{} result:{}", userId, faceLog.getTransId(), hours, result);
        return result;
    }

}
