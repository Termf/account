package com.binance.account.service.face.channel.risk;

import com.binance.account.vo.certificate.UserChannelWckAuditVo;
import com.binance.master.commons.ToString;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author liufeng
 *
 */
@Getter
@Setter
public class UserChannelRiskRatingHandlerParam extends ToString {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3846858514817059548L;

	private UserChannelWckAuditVo userChannelWckAuditVo;

	/**
	 * wck审核返回风控字段
	 */
	private Long isPep;

	/**
	 * wck审核返回风控字段
	 */
	private Long sanctionsHits;

	private String name;
	private String birthday;
	private String country;

	public void buildWckResult(UserChannelWckAuditVo userChannelWckAuditVo, Long isPep, Long sanctionsHits) {
		this.userChannelWckAuditVo = userChannelWckAuditVo;
		this.isPep = isPep;
		this.sanctionsHits = sanctionsHits;
	}

	public void buildUserInfo(String name, String birthday, String country) {
		this.name = name;
		this.birthday = birthday;
		this.country = country;
	}

}
