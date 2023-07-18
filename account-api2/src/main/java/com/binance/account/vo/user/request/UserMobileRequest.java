package com.binance.account.vo.user.request;

import java.io.Serializable;

import org.hibernate.validator.constraints.NotEmpty;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户手机号Request")
@Getter
@Setter
public class UserMobileRequest implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1819228660534076201L;

	@ApiModelProperty(name = "手机号", required = true)
	@NotEmpty
    private String mobile;


	@ApiModelProperty(name = "手机国家码mobileCode", required = true)
	private String mobileCode;


    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }	    
}
