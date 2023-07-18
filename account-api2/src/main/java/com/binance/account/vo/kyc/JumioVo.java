package com.binance.account.vo.kyc;

import java.io.Serializable;
import java.util.Date;

import org.apache.commons.lang3.StringUtils;

import com.binance.master.enums.LanguageEnum;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JumioVo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Long id;

	private Long userId;

	private Date createTime;

	private Date updateTime;

	private String local;

	private String bizId;

	private String scanReference;

	private String redirectUrl;

	private String front;

	private String back;

	private String face;

	private String firstName;

	private String lastName;

	private String dob;

	private String address;

	private String postalCode;

	private String city;

	private String issuingCountry;

	private String expiryDate;

	private String number;

	private String documentType;

	private String source;

	private String clientIp;

	/**
	 * 如果错误，会加入错误原因的Code值，参加JUMIO的所有错误的Code
	 */
	private String failReason;

	/**
	 * 备注信息
	 */
	private String remark;

	/**
	 * 申请时的IP
	 */
	private String applyIp;

	/** 初始化时使用的语言 */
	private LanguageEnum baseLanguage;

	/** 初始化时设定的域名地址（方便后面的跳转域名问题） */
	private String baseUrl;

	/**
	 * 证件签发机构
	 */
	private String issuingAuthority;

	/**
	 * 证件签发日期
	 */
	private String issuingDate;

	/**
	 * 证件发行地
	 */
	private String issuingPlace;

	/**
	 * 证件子类型
	 */
	private String idSubType;

	/**
	 * 每一笔业务的binance定义的唯一标识
	 */
	private String merchantReference;
	
	private String jumioStatus;

	/**
	 * 用户标识这笔jumio 是否失效了，如果是，在初始化的时候不管什么状态都不影响创建新的，
	 * 并且在最终状态时，也不会发送任何变更通知信息。默认值是fase-未失效
	 */
	private boolean disableFlag;

	public String getName() {
		return StringUtils.join(firstName, " ", lastName).replaceAll("N/A", "").trim();
	}
}
