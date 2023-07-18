package com.binance.account.util;

public class UserEmailUtils {
    public UserEmailUtils() {
    }

    public static String getMobileUserEmail(String mobileCode, String mobile) {
       return mobileCode+mobile+"_mobileUser@binance.com";
    }

}
