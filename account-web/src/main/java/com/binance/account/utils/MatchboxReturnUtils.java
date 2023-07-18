package com.binance.account.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.mbxgateway.vo.MbxErrorMsg;
import com.google.common.collect.Lists;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.StringUtils;

import java.lang.reflect.Field;
import java.util.List;

@Log4j2
public class MatchboxReturnUtils {

    @SuppressWarnings("unchecked")
    public static <T> T getMbxValue(String jsonStr, Class<T> clazz) {
        if (clazz.isAssignableFrom(String.class)) {
            return (T) jsonStr;
        }
        JSON json = (JSON) JSON.parse(jsonStr);
        return json.toJavaObject(clazz);
    }

    public static <T> List<T> getMbxValueAsList(String jsonStr, Class<T> clazz) {
        JSON json = (JSON) JSON.parse(jsonStr);
        if (json instanceof JSONObject) {
            return Lists.newArrayList(getMbxValue(jsonStr, clazz));
        }
        return ((JSONArray) json).toJavaList(clazz);
    }

    public static void processMbxErrorMsg(String json, String warnInfo) {
        MbxErrorMsg mbxErrorMsg = MatchboxReturnUtils.getMbxValue(json, MbxErrorMsg.class);
        log.warn(warnInfo + ": code:{}, msg:{}", mbxErrorMsg.getCode(), mbxErrorMsg.getMsg());
        if(StringUtils.isNotEmpty(mbxErrorMsg.getCode()) && StringUtils.isNotEmpty(mbxErrorMsg.getMsg())) {
            throw new BusinessException(mbxErrorMsg.getCode(), mbxErrorMsg.getMsg());
        }
    }

    /**
     * 判断该对象是否: 返回ture表示所有属性为null
     * 返回false表示不是所有属性都是null
     *
     */
    public static boolean isAllFieldNull(Object obj){
        boolean flag = false;
        try {
            // 得到类对象
            Class stuCla = (Class) obj.getClass();
            //得到属性集合
            Field[] fs = stuCla.getDeclaredFields();
            flag = true;
            for (Field f : fs) {
                f.setAccessible(true);
                Object val = f.get(obj);
                //只要有1个属性不为空,那么就不是所有的属性值都为空
                if(val != null) {
                    flag = false;
                    break;
                }
            }
        } catch (Exception e) {
            log.error("isAllFieldNull check error! msg:{}", e);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }

        return flag;
    }
}
