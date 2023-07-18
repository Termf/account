package com.binance.account.common.enums;

/**
 * @author liliang1
 * @date 2018-12-06 16:13
 */
public enum TransFaceLogStatus {

    /** 初始化状态 */
    INIT,
    /** 待认证状态 (已通知用户待人脸识别认证) */
    PENDING,
    /** 认证完成(终态) */
    PASSED,
    /** 认证失败(终态) */
    FAIL,
    /** 过期(终态,目前没考虑) */
    EXPIRED,
    /** 进入审核中（这种状态需要人工审核）*/
    REVIEW,
    ;

    /**
     * 状态是否已经处于终态
     * @param status
     * @return
     */
    public static boolean isEndStatus(TransFaceLogStatus status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case PASSED:
            case FAIL:
            case EXPIRED:
                return true;
            default:
                return false;
        }
    }

}
