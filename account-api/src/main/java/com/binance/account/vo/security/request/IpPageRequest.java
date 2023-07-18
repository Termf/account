package com.binance.account.vo.security.request;

import java.io.Serializable;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.Page;
import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("根据ipPage查询的Request")
@Getter
@Setter
public class IpPageRequest extends Page implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3511939406162010921L;

	@ApiModelProperty("ip地址")
    @NotNull
    private String ip;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
