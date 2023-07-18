package com.binance.account.vo.security.request;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("根据ip查询的Request")
@Getter
@Setter
public class IpRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7771658933351015862L;

	@ApiModelProperty("ip地址")
    @NotNull
    private List<String> ips;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
