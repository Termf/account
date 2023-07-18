package com.binance.account.utils;

import com.binance.master.utils.StringUtils;

import java.util.regex.Pattern;

/**
 * @author: caixinning
 * @date: 2018/07/20 19:42
 **/
public class HtmlUtils {

    private static final Pattern HTML_PTN = Pattern.compile("<[^>]+>");

    /**
     * 判断字符串中是否有恶意的html注入
     */
    public static boolean isHtmlInject(String s){
        if (StringUtils.isBlank(s)){
            return false;
        }
        return HTML_PTN.matcher(s).find();
    }

}
