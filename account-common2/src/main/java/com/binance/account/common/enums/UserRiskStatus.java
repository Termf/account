package com.binance.account.common.enums;

import org.springframework.util.Assert;

import lombok.Getter;

/**
 * 用户流程后，轮训风控结果的处理状态
 *
 */
@Getter
public enum UserRiskStatus {
	UNDO("尚未处理"), DOING("处理中"), DONE("处理完");

	private String desc;

	UserRiskStatus(String desc) {
		this.desc = desc;
	}

	public static UserRiskStatus convertFrom(String name) {
		Assert.isNull(name, "name must not be null");
		name = name.trim();
		UserRiskStatus r = null;
		for (UserRiskStatus s : UserRiskStatus.values()) {
			if (name.equalsIgnoreCase(s.name())) {
				r = s;
				break;
			}
		}
		Assert.isNull(r, "UserRiskStatus not contain the name");
		return r;
	}

}
