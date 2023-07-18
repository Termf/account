package com.binance.account.vo.security.response;

import java.io.Serializable;
import java.util.List;

import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("根据ip获取用户安全日志列表Response")
@Getter
@Setter
@NoArgsConstructor
public class UserSecurityLogListResponse implements Serializable{/**
	 * 
	 */
	private static final long serialVersionUID = 7688566747811095788L;
 
	private List<UserSecurityLogVo> result;
	
	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }	  
}
