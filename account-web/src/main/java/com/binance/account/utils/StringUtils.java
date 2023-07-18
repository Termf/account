package com.binance.account.utils;

public class StringUtils {

    /**
     * 过滤emoji字符
     * pnkweb的StringUtil.filterEmoji
     * @param source
     */
    public static String replaceEmoji(String source, String replacement){
        if (org.apache.commons.lang3.StringUtils.isBlank(source)){
            return source;
        }
        return source.replaceAll("[\\ud800\\udc00-\\udbff\\udfff\\ud800-\\udfff]", replacement);
    }
}
