package com.binance.account.utils;

import java.beans.PropertyDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.beanutils.PropertyUtilsBean;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.log4j.Log4j2;

/**
 * Company: jdp2p <br/>
 * Copyright: Copyright (c)2013 <br/>
 * Description: java.util.Map 与之相关的工具类
 *
 * @author: wangzhi
 * @version: 1.0 Create at: 2014-4-3 下午4:34:51
 *
 *           Modification History: <br/>
 *           Date Author Version Description
 *           ------------------------------------------------------------------ 2014-4-3 wangzhi 1.0
 */
@Log4j2
public class MapUtil {

    /**
     * json字符串变为hashMap
     *
     * @param str
     * @return
     * @throws JsonParseException
     * @throws JsonMappingException
     * @throws IOException
     */
    public static Map<String, String> jsonStringToHashMap(String str)
            throws JsonParseException, JsonMappingException, IOException {
        return new ObjectMapper().readValue(str, HashMap.class);
    }

    /**
     * 字符串变为map，字符串格式 key1=value1, key2=value2...
     *
     * @param str HashMap<String,String> toString()以后产生的字符串
     * @return
     * @deprecated
     */
    @Deprecated
    public static Map<String, String> stringToHashMap(String str) {
        Map<String, String> map = new HashMap<String, String>();
        for (final String entry : str.split(",")) {
            final String key = entry.substring(0, entry.indexOf('=')).trim();
            final String value = entry.substring(entry.indexOf('=') + 1, entry.length()).trim();
            map.put(key, value);
        }
        return map;
    }

    /**
     * HashMap 转换为 String
     *
     * @param map
     * @return
     * @deprecated
     * @see
     */
    @Deprecated
    public static String mapToString(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            if (iter.hasNext()) {
                sb.append(',').append(' ');
            }
        }
        return sb.toString();
    }

    public static String mapToString2(Map<String, String> map) {
        StringBuilder sb = new StringBuilder();
        Iterator<Entry<String, String>> iter = map.entrySet().iterator();
        while (iter.hasNext()) {
            Entry<String, String> entry = iter.next();
            sb.append(entry.getKey());
            sb.append('=');
            sb.append(entry.getValue());
            if (iter.hasNext()) {
                sb.append('&');
            }
        }
        return sb.toString();
    }

    // 将javabean实体类转为map类型，然后返回一个map类型的值
    public static Map<String, Object> beanToMap(Object obj) {
        Map<String, Object> params = new HashMap<String, Object>(0);
        try {
            PropertyUtilsBean propertyUtilsBean = new PropertyUtilsBean();
            PropertyDescriptor[] descriptors = propertyUtilsBean.getPropertyDescriptors(obj);
            int length = descriptors.length;
            for (int i = 0; i < length; i++) {
                try {
                    String name = descriptors[i].getName();
                    if (!"class".equals(name)) {
                        params.put(name, propertyUtilsBean.getNestedProperty(obj, name));
                    }
                } catch (Exception e) {
                    log.error("beanToMap exception, param:{},", obj, e);
                }
            }
        } catch (Exception e) {
            log.error("beanToMap exception, param:{},", obj, e);
        }
        return params;
    }

    /**
     * 将错误消息置顶
     * 
     * @param data
     */
    public static void sortCompareData(List<Map<String, Object>> data) {
        if (data == null)
            return;
        List<Map<String, Object>> errorList = new ArrayList<>();
        List<Map<String, Object>> normalList = new ArrayList<>();
        for (Map<String, Object> item : data) {
            if (item.get("msg") != null)
                errorList.add(item);
            else
                normalList.add(item);
        }
        if (errorList.size() > 0) {
            data.clear();
            data.addAll(errorList);
            data.addAll(normalList);
        }
    }

}
