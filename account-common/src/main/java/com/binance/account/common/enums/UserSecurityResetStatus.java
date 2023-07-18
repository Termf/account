package com.binance.account.common.enums;

/**
 * 重置流程记录的状态值
 * @author liliang1
 * @date 2019-01-03 18:41
 */
public enum UserSecurityResetStatus {

    /** 0- 未答题 */
    unverified,
    /** 1- 未提交文件或提交后结果未回来 */
    unsubmitted,
    /** 2- 待审核(已经废弃，由于数据库占位保留先) */
    pendingReview,
    /** 3- 已通过 */
    passed,
    /** 4- 已拒绝 */
    refused,
    /** 5- 已取消 */
    cancelled,
    /** 6- JUMIO-审核通过 */
    jumioPassed,
    /** 7- JUMIO-审核拒绝 */
    jumioRefused,
    /** 8- 等待人脸识别 */
    facePending,
    /** 9- 人脸识别照片检测错误 */
    FIE,
    /** 10- JUMIO通过人脸识别拒绝 */
    JPFR,
    /** 11- JUMIO拒绝人脸识别拒绝 */
    JRFR,
    /** 12- JUMIO通过人脸识别通过 */
    JPFP,
    /** 13- JUMIO拒绝人脸识别通过 */
    JRFP,
    /** 14- JUMIO处于UPLOAD、REVIEW*/
    jumioPending;

    /**
     * 根据名称匹配对应的数据值
     * @param name
     * @return
     */
    public static UserSecurityResetStatus getByName(String name) {
        if (name == null) {
            return null;
        }
        for (UserSecurityResetStatus status : UserSecurityResetStatus.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否处于中间态
     * @param status
     * @return
     */
    public static boolean isReviewPending(UserSecurityResetStatus status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case passed:
            case refused:
            case cancelled:
                return false;
        }
        return true;
    }

    /**
     * 当前状态是否可以审核
     * @param status
     * @return
     */
    public static boolean isCanAudit(UserSecurityResetStatus status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case pendingReview:
            case jumioRefused:
            case jumioPassed:
            case jumioPending:
            case FIE:
            case facePending:
            case JPFR:
            case JPFP:
            case JRFP:
            case JRFR:
                return true;
        }
        return false;
    }

}
