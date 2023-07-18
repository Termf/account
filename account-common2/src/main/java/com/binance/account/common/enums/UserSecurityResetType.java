package com.binance.account.common.enums;

/**
 * @author liliang1
 * @date 2019-01-03 18:37
 */

//TODO: rename class to make it general...
public enum UserSecurityResetType {

    /** 0-重置谷歌认证 */
    google,
    /** 1-重置手机认证 */
    mobile,
    /** 2-账号解禁 */
    enable,
    /** 3-授权设备 */
    authDevice;

    /**
     * 根据名称匹配对应的数据值
     * @param name
     * @return
     */
    public static UserSecurityResetType getByName(String name) {
        if (name == null) {
            return null;
        }
        for (UserSecurityResetType type : UserSecurityResetType.values()) {
            if (type.name().equalsIgnoreCase(name)) {
                return type;
            }
        }
        return null;
    }
}
