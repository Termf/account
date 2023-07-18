package com.binance.account.domain.bo;

import com.binance.master.commons.ToString;
import com.binance.master.constant.Constant;
import com.binance.master.utils.RedisCacheUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 特殊用户待授权的设备缓存（不入库，存redis，给后台管理员使用，故不考虑并发问题）
 * @author: caixinning
 * @date: 2018/07/23 14:20
 **/
@SuppressWarnings("unchecked")
@Component
public class SpecialUserDeviceAuthorizeCache extends ToString {

    private static final String CACHE_SPECIAL_USER_DEVICE_LIST = "CACHE_SPECIAL_USER_DEVICE_LIST";
    private static final String PATTERN_DEVICE_AUTH_EMAIL = "USER_DEVICE_AUTH_EMAIL_%s";

    public void add(Map<String, Object> item){
        List<Map<String, Object>> list = RedisCacheUtils.get(CACHE_SPECIAL_USER_DEVICE_LIST, List.class);
        if (list==null){
            list = new ArrayList<>();
        }else {
            list = arrange(list);
        }
        list.add(item);
        RedisCacheUtils.set(CACHE_SPECIAL_USER_DEVICE_LIST, list, Constant.HOUR_HALF);
    }

    public void arrange(){
        List<Map<String, Object>> list = RedisCacheUtils.get(CACHE_SPECIAL_USER_DEVICE_LIST, List.class);
        list = arrange(list);
        if (list!=null && !list.isEmpty()){
            RedisCacheUtils.set(CACHE_SPECIAL_USER_DEVICE_LIST, list, Constant.HOUR_HALF);
        }
    }

    public List<Map<String, Object>> getList() {
        return arrange(RedisCacheUtils.get(CACHE_SPECIAL_USER_DEVICE_LIST, List.class));
    }

    private List<Map<String, Object>> arrange(List<Map<String, Object>> oldList){
        List<Map<String, Object>> newList = new ArrayList<>();
        if (oldList!=null && !oldList.isEmpty()){
            oldList.forEach(item->{
                String code = StringUtils.right(item.get("link").toString(), 32);
                if (StringUtils.isNotBlank(RedisCacheUtils.get(String.format(PATTERN_DEVICE_AUTH_EMAIL, code)))){
                        newList.add(item);
                }
            });
        }
        return newList;
    }

}
