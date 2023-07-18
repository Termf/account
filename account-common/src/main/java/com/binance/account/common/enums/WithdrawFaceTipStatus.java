package com.binance.account.common.enums;

/**
 * 提现需要做人脸识别检查提示内容的状态值
 * @author liliang1
 * @date 2018-12-20 15:08
 */
public enum WithdrawFaceTipStatus {

    NORMAL(0, "不需要做人脸识别"),
    NEED_KYC(1, "需要先完成KYC认证"),
    EMAIL_NOTIFY(2, "可以进行人脸识别"),
    WAIT_AUDIT(3, "人脸识别待人工审核"),
    FACE_REFUSED(4, "人脸识别已被拒绝,需走人工");

    private int code;
    private String desc;

    WithdrawFaceTipStatus(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static WithdrawFaceTipStatus getByCode(int code) {
        for (WithdrawFaceTipStatus status : WithdrawFaceTipStatus.values()) {
            if (code == status.getCode()) {
                return status;
            }
        }
        return null;
    }

    public int getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
