package com.binance.account.service.security;

import java.util.Date;

import com.binance.account.vo.security.request.SecurityFaceStatusRequest;
import com.binance.account.vo.withdraw.request.WithdrawFaceInHoursRequest;
import com.binance.account.vo.withdraw.response.UserWithdrawFaceTipResponse;
import com.binance.inspector.common.enums.FaceTransType;

/**
 * @author liliang1
 * @date 2018-11-29 10:50
 */
public interface IWithdrawSecurityFace {

    /**
     * 变更提现风控是否需要做人脸识别的标识
     * @param request
     * @return
     */
    Integer changeWithdrawSecurityFaceStatus(SecurityFaceStatusRequest request);

    /**
     * KYC认证通过时，检查是否需要触发提币人脸识别流程
     * @param userId
     * @param transId
     * @param transType
     * @param transFaceLogId KYC如果关联有提币人脸关联的信息，需要带上
     */
    void kycPassCheckSecurityFaceCheck(final Long userId, final String transId, FaceTransType transType, String transFaceLogId,String kycStatus,Date kycPassTime);

    /**
     * 检查当前提币风控人脸识别状态进度提示
     * @param userId
     * @return
     */
    UserWithdrawFaceTipResponse checkWithdrawFaceStatus(Long userId);


    /**
     * 检查用户上次提币人脸通过的业务与当前时间的时长比较
     * 当前时间是否在最后一次提币人脸通过的多少小时之内，如果是返回true,如果不是返回false
     * @param request
     * @return true: 当前时间 <= 最后一次通过的时间+HOURS小时内，false: 当前时间 > 最后一次通过的时间+HOURS小时
     */
    boolean checkWithdrawFaceInHours(WithdrawFaceInHoursRequest request);
}
