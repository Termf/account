package com.binance.account.common.constant;

import java.util.HashMap;
import java.util.Map;

/**
 * @author liliang1
 * @date 2018-09-21 11:07
 */
public final class ResetConst {

    public enum ConverLanguage {
        CN,
        EN
    }



    public static final String KEY_7_DAYS_REFUSED = "7_DAYS_REFUSED";
    public static final String KEY_FACE_MULTIPLE_COUNT = "FACE_MULTIPLE_COUNT";
    public static final String KEY_ID_NUMBER_USED = "id.photo.authenticated";
    public static final String KEY_ID_EXPIRD = "ID_EXPIRD";
    public static final String KEY_HAND_NOTE_NOT_MATCH = "HAND_NOTE_NOT_MATCH";
    public static final String KEY_STATUS_UNVERIFIED_ERROR = "STATUS_UNVERIFIED_ERROR";

    public static final Map<String,String> MESSAGE_CN = new HashMap<>();
    public static final Map<String,String> MESSAGE_EN = new HashMap<>();

    static {
        //把JUMIO的错误也直接加入到该列表中
        Map<String, String> jumioCnMap = JumioConst.REASON_CN;
        Map<String, String> jumioEnMap = JumioConst.REASON_EN;
        jumioCnMap.forEach((k, v) -> MESSAGE_CN.put(k, v));
        jumioEnMap.forEach((k, v) -> MESSAGE_EN.put(k, v));

        MESSAGE_CN.put(KEY_7_DAYS_REFUSED, "您的申请记录已过期");
        MESSAGE_EN.put(KEY_7_DAYS_REFUSED, "Your application record has expired");
        MESSAGE_CN.put(KEY_FACE_MULTIPLE_COUNT, "你的人脸信息认证失败次数已超出限制");
        MESSAGE_EN.put(KEY_FACE_MULTIPLE_COUNT, "Your facial verification failure have exceeded the limit");
        MESSAGE_CN.put(KEY_ID_NUMBER_USED, "证件号码已被使用");
        MESSAGE_EN.put(KEY_ID_NUMBER_USED, "Your photo ID has been authenticated");
        MESSAGE_CN.put(KEY_STATUS_UNVERIFIED_ERROR, "请重新发起流程并回答完问题");
        MESSAGE_EN.put(KEY_STATUS_UNVERIFIED_ERROR, "Please re-initiate the process and answer the questions");

    }

    public static String converResetMessag(String key, ConverLanguage language, String... params) {
        String message;
        if (language == ConverLanguage.CN) {
            message = MESSAGE_CN.get(key);
        }else {
            message = MESSAGE_EN.get(key);
        }
        if (message != null && params != null && params.length > 0) {
            message = String.format(message, params);
        }
        return message == null ? key : message;
    }

}
