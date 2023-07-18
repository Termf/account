package com.binance.account.common.constant;

/**
 * Created by Shining.Cai on 2018/10/23.
 **/
public final class UserConst {

    public static final String USER_ID = "userId";
    public static final String EMAIL = "email";
    public static final String CREATE_TIME = "createTime";
    public static final String UPDATE_TIME = "updateTime";
    public static final String BATCH_ID = "batchId";
    public static final String IS_RESTORE = "isRestore";

    /** 开关开启时的字符串 */
    public static final String SWITCH_ON = "ON";

    /** 提现风控是否需要做人脸状态：0-不需要做 1-需要做人脸识别 */
    public static final Integer WITHDRAW_SECURITY_FACE_STATUS_UNDO = 0;
    public static final Integer WITHDRAW_SECURITY_FACE_STATUS_DO = 1;

    public static final String WITHDRAW_FACE_REFUSED_KYC = "withdraw.face.refused.kyc";
    public static final String RESET_EMAIL_FACE_REFUSED_KYC = "resetEmail.face.refused.kyc";

    /** us kyc notify email */
    public static final String US_KYC_EMAIL_L0_TO_L1 = "us.kyc.email.L0toL1";
    public static final String US_KYC_EMAIL_L1_TO_L2 = "us.kyc.email.L1toL2";
    public static final String US_KYC_EMAIL_L2_TO_L1 = "us.kyc.email.L2toL1";
    public static final String US_KYC_EMAIL_L1_TO_L0 = "us.kyc.email.L1toL0";
    public static final String US_KYC_L1_END = "us.kyc.email.L1End";


}
