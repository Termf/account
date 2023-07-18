package com.binance.account.vo.user;

import java.io.Serializable;
import java.util.Date;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author zhaochenkai
 */
@Data
@ApiModel("做市商账号信息")
public class MarketMakerUserVo implements Serializable {

    private static final long serialVersionUID = 7497323299480701200L;

    private Long userId;
    private String email;
    private String remark;
    private Date createTime;

}
