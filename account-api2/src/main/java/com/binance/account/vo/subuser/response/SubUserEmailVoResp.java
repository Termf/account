package com.binance.account.vo.subuser.response;

import java.io.Serializable;
import java.util.List;

import com.binance.account.vo.subuser.SubUserEmailVo;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "子账户邮箱集合Response", value = "子账户邮箱集合Response")
@Getter
@Setter
public class SubUserEmailVoResp implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6391454149614257353L;
	
	private List<SubUserEmailVo> result;
	
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }

}
