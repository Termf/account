package com.binance.account.utils;

import com.binance.master.utils.RedisCacheUtils;

import lombok.extern.slf4j.Slf4j;

/**
 * 用于设备信息缓存
 *
 */
@Slf4j
public final class DeviceCacheUtils {

	private static final String PK_PREFIX = "account:device:cache:pk:";

	private DeviceCacheUtils() {
	}

	public static String getDevicePK(String key) {
		try {
			return RedisCacheUtils.get(key, String.class, PK_PREFIX);
		} catch (Exception e) {
			log.warn("查询devicepk,key:", e);
		}
		return null;
	}

	public static void setDevicePK(String key, String value, long timeoutInseconds) {
		try {
			RedisCacheUtils.set(key, value, timeoutInseconds, PK_PREFIX);
		} catch (Exception e) {
			log.warn("缓存devicepk,key:" + key, e);
		}
	}

	public static void delDevicePK(final String key) {
		try {
			RedisCacheUtils.del(key, PK_PREFIX);
		} catch (Exception e) {
			log.warn("删除devicepk,key:" + key, e);
		}
	}
}
