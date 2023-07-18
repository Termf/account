package com.binance.account.common.enums;

import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

/**
 * <p>问答模块场景定义枚举；</p>
 * 
 */
public enum QuestionSceneEnum {

	RESET_2FA("重置2fa","reset2fa","need_question_reset2fa_hit_rule"),
	AUTH_DEVICE("新设备授权","device_auth","need_question_device_auth_hit_rule");
	
	QuestionSceneEnum(String cn,String en,String defaultRule){
		this.cn = cn;// 显示值
		this.en = en;// 枚举值
		this.defaultRule = defaultRule;// 默认规则
	}
	
	private String cn;
	private String en;
    private String defaultRule;

	public static QuestionSceneEnum ConvertUserSecurityResetTypeToScene(UserSecurityResetType type) {
		Assert.notNull(type, "type MUST NOT be Null");
		switch (type) {
		case authDevice:
			return QuestionSceneEnum.AUTH_DEVICE;
		case enable:
		case google:
		case mobile:
			return QuestionSceneEnum.RESET_2FA;
		default:
			throw new RuntimeException("Unrecognized Enum:" + type);
		}
	}
	
	public static QuestionSceneEnum ConvertUserSecurityResetTypeToScene(String flowType) {
		Assert.notNull(flowType, "flowType MUST NOT be Null");
		return ConvertUserSecurityResetTypeToScene(UserSecurityResetType.getByName(flowType));
	}
	
	/**
	 * [
	 *   {"name":"RESET_2FA","en":"reset2fa","cn":"reset2fa"},
	 *   {"name":"AUTH_DEVICE","en":"Equipment Authorization","cn":"设备授权"}
	 * ]
	 */
	public static List<Map<String,String>> Convert2Map(){
		List<Map<String,String>> lst = Lists.newLinkedList();
		for (QuestionSceneEnum e:QuestionSceneEnum.values()) {
			Map<String,String> map =Maps.newHashMapWithExpectedSize(3);
			map.put("name",e.name());
			map.put("cn",e.getCn());
			map.put("en",e.getEn());
			lst.add(map);
		}
		return lst;
	}

	public String getCn() {
		return cn;
	}

	public String getEn() {
		return en;
	}

	public String getDefaultRule() {
		return defaultRule;
	}
}
