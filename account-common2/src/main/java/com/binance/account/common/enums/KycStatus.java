package com.binance.account.common.enums;

import org.springframework.util.StringUtils;

/**
 * kyc状态 <br>
 * WARNING: 请勿改动枚举值的顺序！！ 数据库中保存枚举值的序号，如“后台审核通过”对应状态“2”（参考 EnumOrdinalTypeHandler）
 * @author: caixinning
 * @date: 2018/08/08 13:44
 **/
public enum KycStatus {

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
     * 6 World-Check等待审核
     */
    wckWaiting,
    /**
     * 7 World-Check审核通过
     */
    wckPassed,
    /**
     * 8 World-Check审核拒绝
     */
    wckRefused,

    /**
     * 9 禁止(不合归)国籍通过
     */
    forbidPassed,

    /**
     * 10 删除kyc记录
     */
    delete,

    /**
     * 11 basic认证
     */
    basic;

    public static KycStatus getByNmae(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (KycStatus status : KycStatus.values()) {
            if (status.name().equalsIgnoreCase(name)) {
                return status;
            }
        }
        return null;
    }

    /**
     * 是否处于终态
     * @param kycStatus
     * @return
     */
    public static boolean isEndStatus(KycStatus kycStatus) {
        if (kycStatus == null) {
            return false;
        }
        switch (kycStatus) {
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
