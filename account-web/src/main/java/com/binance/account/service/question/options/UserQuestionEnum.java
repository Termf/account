package com.binance.account.service.question.options;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.jsonwebtoken.lang.Assert;

/**
 * <p>用户问题枚举，每增加一个问题需要在此处配置一个枚举。</p>
 * <p>每个问题对应一类问题选项生成逻辑，参考{@link QustionOptionsTypeEnum}</p>
 *
 */
public enum UserQuestionEnum {
	/**
	 * 用户收藏币种
	 */
	@Deprecated
	USER_PORTFOLIO_ASSET, 
	/**
	 * 用户持币币种
	 */
	@Deprecated
	USER_ASSET, /**
	 * 用户最近交易
	 */
	USER_TRADED_ASSET, 
	/**
	 * 用户持BTC数量
	 */
	USER_BTC_AMOUNT, 
	/**
	 * 用户持有或收藏的币种
	 */
	USER_ASSERT_OR_PORTFOLIO_ASSET,
	/**
	 * 用户最近48h交易的币种
	 */
	USER_TRADED_IN_48_H,
	/**
	 * 用户注册日期
	 */
	USER_REGISTRATION_DATE
	;
	
	private static Map<String, UserQuestionEnum> map = new ConcurrentHashMap<>(UserQuestionEnum.values().length);

	static {
		for (UserQuestionEnum e : UserQuestionEnum.values()) {
			map.put(e.name().toLowerCase(), e);
		}
	}
	
	/**
	 * 当前问题的选项类型
	 * 
	 * @return
	 */
	public QustionOptionsTypeEnum getOptionType() {
		switch (this) {
		case USER_PORTFOLIO_ASSET:
		case USER_ASSET:
		case USER_TRADED_ASSET:
		case USER_ASSERT_OR_PORTFOLIO_ASSET:
		case USER_TRADED_IN_48_H:
			return QustionOptionsTypeEnum.CURRENCY_SELECTED;
		case USER_BTC_AMOUNT:
			return QustionOptionsTypeEnum.RANDOM_NUMBER_INTERNAL;
		case USER_REGISTRATION_DATE:
			return QustionOptionsTypeEnum.USER_REGISTRATION_DATE;
		default:
			throw new RuntimeException();
		}
	}
	
	public static UserQuestionEnum Convert2Enum(String name) {
		Assert.hasText(name,"入参问题枚举name应该有值");
		String key = name.trim().toLowerCase();
		UserQuestionEnum q = map.get(key);
		Assert.isTrue(q != null, "问题枚举不含有name:" + name);
		return q;
	}
}
