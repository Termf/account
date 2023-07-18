package com.binance.account.data.entity.certificate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;

/**
 * @author liliang1
 * @date 2018-09-29 16:40
 */
@Getter
@AllArgsConstructor
public enum JumioHandlerType {

    /** user kyc 0 */
    USER_KYC("user", "用户KYC验证"),

    /** company kyc 1 */
    COMPANY_KYC("company", "企业KYC验证"),

    /** reset google 2 */
    RESET_GOOGLE("google", "重置谷歌验证"),

    /** reset mobile 3 */
    RESET_MOBILE("mobile", "重置手机验证"),

    /** reset enable 4 */
    RESET_ENABLE("enable", "解冻重置验证"),

    /** user old kyc 5 */
    REMEDIATION("remediation", "老照片重验KYC整治");

    private String code;
    private String decs;


    public static JumioHandlerType getByCode(String code) {
        if (StringUtils.isBlank(code)) {
            return null;
        }
        for (JumioHandlerType type : JumioHandlerType.values()) {
            if (type.getCode().equals(code)) {
                return type;
            }
        }
        return null;
    }

    public static JumioHandlerType getByName(String name) {
        if (StringUtils.isBlank(name)) {
            return null;
        }
        for (JumioHandlerType type : JumioHandlerType.values()) {
            if (StringUtils.equalsIgnoreCase(type.name(), name)) {
                return type;
            }
        }
        return null;
    }
}
