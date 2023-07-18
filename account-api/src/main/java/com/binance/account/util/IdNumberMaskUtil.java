package com.binance.account.util;

import org.apache.commons.lang3.StringUtils;

public class IdNumberMaskUtil {

    private static final String ID_NUMBER_MARK = "************";

    /**
     *
     * 身份证号掩码
     * @param idNumber
     */
    public static String getIdNumberMark(String idNumber) {
        String result;
        if (StringUtils.isBlank(idNumber)) {
            result = "";
        }else {
            StringBuilder tempNumber = new StringBuilder();
            if(idNumber.length() > 3) {
            	tempNumber.append(ID_NUMBER_MARK).append(idNumber.substring(idNumber.length()-4, idNumber.length()));
            }else {
            	tempNumber.append(ID_NUMBER_MARK).append(idNumber);

            }
            result = tempNumber.toString();
        }
        return result;
    }
}
