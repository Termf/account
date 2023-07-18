package com.binance.account.common.query;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author liufeng
 *
 */
@Getter
@Setter
public class UserCertificateListRequest extends Pagination{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1476653855033172432L;
	
	private Long userId;
	
	private String email;
	
	private Integer type;
	
	private Byte status;
	
	private String number;
	
	private String firstName;
	
	private String lastName;
	
	private String country;
	
	private Date startCreateTime;
	
	private Date endCreateTime;

//	private Date startUpdateTime;

	//	private Date endUpdateTime;

}
