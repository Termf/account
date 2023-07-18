package com.binance.account.common.enums;

/**
 * 企业认证状态 <br>
 * WARNING: 请勿改动枚举值的顺序！！ 数据库中保存枚举值的序号，如“后台审核通过”对应状态“2”（参考 EnumOrdinalTypeHandler）
 **/
public enum CompanyCertificateStatus {

    /**
     * 0 等待上传
     */
    pending,

    /**
     * 1 后台审核通过
     */
    passed,

    /**
     * 2 后台审核拒绝
     */
    refused,

    /**
     * 3 jumio审核通过
     */
    jumioPassed,

    /**
     * 4 jumio审核拒绝
     */
    jumioRefused,

    /**
     * 5 已过期
     */
    expired,

    /**
     * 6 不合规国籍通过
     */
    forbidPassed,

    /**
     * 7 删除
     */
    delete
    ;

    /**
     * 是否处于终态
     * @param status
     * @return
     */
    public static boolean isEndStatus(CompanyCertificateStatus status) {
        if (status == null) {
            return false;
        }
        switch (status) {
            case refused:
            case passed:
            case expired:
            case forbidPassed:
            case delete:
                return true;
            default:
                return false;
        }
    }
}
