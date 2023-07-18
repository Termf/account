package com.binance.account.common.enums;

public enum KycCertificateStatus {

    /** 用户正在进行验证 */
    PROCESS,
    /** 通过 */
    PASS,
    /** 拒绝 */
    REFUSED,
    /** 等待后台审核 */
    REVIEW,
    /** 跳过 */
    SKIP,
    /** 禁止(不合规国籍通过) */
    FORBID_PASS;



    public static KycCertificateStatus getByName(String name) {
        if (name == null) {
            return null;
        }
        try {
            return KycCertificateStatus.valueOf(name.toUpperCase());
        }catch (Exception e) {
            return null;
        }
    }

    /**
     * 判断是否已经是终态
     * @return
     */
    public static boolean isEndStatus(KycCertificateStatus status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case PASS:
            case REFUSED:
            case SKIP:
            case FORBID_PASS:
                return true;
            default:
                return false;
        }
    }
}
