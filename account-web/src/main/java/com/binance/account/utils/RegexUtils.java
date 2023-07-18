package com.binance.account.utils;

import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.StringUtils;

import java.util.regex.Pattern;

public final class RegexUtils {
	
	public static final String IP_REGEX="^((?:(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d)))\\.){3}(?:25[0-5]|2[0-4]\\d|((1\\d{2})|([1-9]?\\d))))$";

	private static final Pattern EMAIL_PATTERN =
			Pattern.compile("^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$");


	private static final Pattern MOBILE_PATTERN = Pattern.compile("^[1]\\d{10}$");


	/**
	 * 邮箱匹配
	 */
	public static boolean matchEmail(String email) {
		if (StringUtils.isBlank(email)) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		return EMAIL_PATTERN.matcher(email).matches();
	}

	/**
	 * 手机号匹配
	 */
	public static boolean matchMobile(String mobileCode, String mobile) {
		if (StringUtils.isAnyBlank(mobileCode, mobile)) {
			throw new BusinessException(GeneralCode.USER_ILLEGAL_PARAMETER);
		}
		if (!StringUtils.equals(mobileCode.toLowerCase(), "cn")) {
			return true;
		}
		return MOBILE_PATTERN.matcher(mobile).matches();
	}

	// 校验tin格式,固定为10个数字
	public static boolean matchTin(String tin) {
		Pattern pattern = Pattern.compile("^\\d{10}$");
		return pattern.matcher(tin).matches();
	}
}
