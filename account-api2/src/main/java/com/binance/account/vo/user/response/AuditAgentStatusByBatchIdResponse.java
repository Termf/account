package com.binance.account.vo.user.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class AuditAgentStatusByBatchIdResponse implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3236938909921921042L;

	@ApiModelProperty(name = "描述")
    private String desc;
    
    @ApiModelProperty(name = "内容")
    private String data;

    private List<Long> affectUserIds;
}
