package com.binance.account.vo.security.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("用户Id和主键Id的Request")
@Getter
@Setter
public class UserIdAndIdRequest implements Serializable{

	 /**
	 * 
	 */
	private static final long serialVersionUID = 4645914440131569488L;
	
	@ApiModelProperty("用户Id")
	@NotNull
	private Long userId;
	
	@ApiModelProperty("主键Id")
	@NotNull
	private Long id;

	@Override
    public String toString() {
        return StringUtils.objectToString(this);
    }	    
}
