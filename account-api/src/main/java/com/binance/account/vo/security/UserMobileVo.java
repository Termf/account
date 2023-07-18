package com.binance.account.vo.security;
 

import java.io.Serializable;

import com.binance.master.utils.StringUtils;
 
import lombok.Getter;
import lombok.Setter;
 
@Getter
@Setter
public class UserMobileVo implements Serializable{
 
	/**
	 * 
	 */
	private static final long serialVersionUID = 4177399614705422674L;
	
	private Long userId;
	
	private String email;
	
	private String mobile;
	
	private String mobileCode;
	
    private String mobileNum;
    
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}