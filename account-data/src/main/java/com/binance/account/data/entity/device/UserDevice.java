package com.binance.account.data.entity.device;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
public class UserDevice implements Serializable {
    private static final long serialVersionUID = -3633149265091365750L;

    /** 设备的ip地址 */
    public static final String LOGIN_IP = "login_ip";
    /** 客户端分辨率（Web端已PC客户端分辨率大小）长*宽 */
    public static final String SCREEN_RESOLUTION = "screen_resolution";
    /** 操作系统 / 以及版本号（系统版本号） */
    public static final String SYS_VERSION = "system_version";
    /** 设备品牌及型号 */
    public static final String BRANCH_MODEL = "brand_model";
    /** 系统语言 */
    public static final String SYS_LANG = "system_lang";
    /** 当前设备时区信息（Timezone） */
    public static final String TIMEZONE = "timezone";
    /** 动态设备ID */
    public static final String DEVICE_ID = "device_id";
    /** 设备名称 */
    public static final String DEVICE_NAME = "device_name";
    /** 用于设备授权成功后的跳转地址 */
    public static final String DEVICE_CALLBACK = "device_callback";

    public static final String LOCATION_CITY = "location_city";

    public static enum Status {

        /**
         * 0 授权设备(default)
         */
        AUTHORIZED,

        /**
         * 1 非授权设备
         */
        NOT_AUTHORIZED;

        public static UserDevice.Status valueOfOrdinal(int ordinal) {
            if (ordinal > values().length - 1 || ordinal < 0) {
                return null;
            } else {
                return values()[ordinal];
            }
        }
    }

    private Long id;

    private Long userId;

    private String agentType;

    private String source;

    private String content;

    private Date activeTime;

    private Date createTime;

    private Date updateTime;

    private Byte isDel;

    private Status status;

    //问题模块 flowId
    private String flowId;
}
