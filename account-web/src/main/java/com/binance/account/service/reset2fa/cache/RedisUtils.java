package com.binance.account.service.reset2fa.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.binance.account.constants.AccountConstants;
import com.binance.account.vo.reset.response.Reset2faNextStepResponse;
import com.binance.master.utils.RedisCacheUtils;

/**
 * 缓存辅助类
 *
 */
public final class RedisUtils {
	private static final Logger log = LoggerFactory.getLogger(RedisUtils.class);

	/**
	 * 缓存下一步操作对内容
	 *
	 * @param nextStepResponse
	 */
	public static void cacheNextStepInfo(Reset2faNextStepResponse nextStepResponse,long seconds) {
		String cacheKey = AccountConstants.RESET_NEXT_STEP_CACHE + nextStepResponse.getRequestId();
		RedisCacheUtils.set(cacheKey, nextStepResponse, seconds);
	}

	/**
	 * 获取缓存下一步对内容
	 *
	 * @param requestId
	 * @return
	 */
	public static Reset2faNextStepResponse getNextStepCacheInfo(String requestId) {
		String cacheKey = AccountConstants.RESET_NEXT_STEP_CACHE + requestId;
		try {
			return RedisCacheUtils.get(cacheKey, Reset2faNextStepResponse.class);
		} catch (Exception e) {
			log.error("get reset next step info fail. requestId:{} ", requestId, e);
			return null;
		}
	}
	
	/**
	 * 当前用户是否能重发邮件
	 * 
	 * @param userId
	 * @param seconds
	 * @return
	 */
	public static boolean canResendEmail(long userId,long seconds) {
		String cacheKey = AccountConstants.RESET_RESEND_EMAIL_CACHE + userId;
		// 同一个用户互斥不用解锁操作，这个方法也够了
		return RedisCacheUtils.setNX(cacheKey, "0", seconds);
	}
	
}
