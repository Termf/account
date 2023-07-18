package com.binance.account.vo.security.response;
 
import java.io.Serializable;
import java.util.List;

import com.binance.account.vo.security.UserMobileVo;
import com.binance.master.utils.StringUtils;
 
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@ApiModel("批量获取用户手机号等信息")
@Getter
@Setter
@NoArgsConstructor
public class UserSecurityListResponse implements Serializable{
 	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2577876753126572793L;
	
	private List<UserMobileVo> result;
	
	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}