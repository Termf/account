package com.binance.account.vo.subuser.response;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * Created by yangyang on 2019/7/19.
 */
@ApiModel(description = "子账户交易历史Response", value = "子账户交易历史Response")
@Data
public class SubAccountTransferResp{

    // 转出方邮箱
    String from;
    // 转入方邮箱
    String to;
    // 资产名称
    String asset;
    // 资产数量
    String qty;
    // 划转时间
    Long time;

}
