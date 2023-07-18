package com.binance.account.domain.bo;

public class FrontPushEventType {

    public static final String REGISTER_COMPLETE = "REGISTER_COMPLETE";

    public static final String OPEN_ACCOUNT_COMPLETE = "OPEN_ACCOUNT_COMPLETE";

    public static final String REGISTER_COMPLETE_ROUTING = "user_task_event.register_complete";

    public static final String OPEN_ACCOUNT_COMPLETE_ROUTING = "user_task_event.open_account_complete";


    public static final String OPEN_ACCOUNT_COMPLETE_FUTURES = "FUTURES";

    public static final String OPEN_ACCOUNT_COMPLETE_MARGIN = "MARGIN";

    public static final String TWOFA_COMPLETE_ROUTING = "user_task_event.2fa_complete";

    public static final String TWOFA_COMPLETE = "2FA_COMPLETE";

    public static final String FRONT_EXCHANGE = "user-task-event-exchange";

    public static final String TWOFA_SMS = "SMS";

    public static final String TWOFA_GOOGLE_AUTH = "GOOGLE_AUTH";
}
