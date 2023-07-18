package com.binance.account.vo.subuser;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Created by zhao chenkai on 2019/10/24.
 */
@ApiModel(description = "子账户交易历史详细", value = "子账户交易历史详细")
@Data
public class SubAccountTransferHistoryInfoVo extends ToString {

    private static final long serialVersionUID = 3081048536504474542L;

    private Integer id;
    private Long transactionId;
    private String fromUser;
    private String fromEmail;
    private String toUser;
    private String toEmail;
    private String asset;
    private BigDecimal amount;
    private Date createTime;
    private Long createTimeStamp;

}
