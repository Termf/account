package com.binance.account.vo.user.request;

import java.io.Serializable;
import java.util.List;

import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

/**
 * @author mengjuan
 *
 */
@ApiModel(description = "批量修改用户返佣比例Request", value = "批量修改用户返佣比例Request")
@Getter
@Setter
public class UpdateUserAgentRewardListRequest implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 4812383048164475819L;
	
	//批量的只有userId和agentRewardRatio请求入参
	private List<UpdateUserAgentRewardRequest> agentList;

}
