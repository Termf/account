package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.SubAccountTransferHistoryInfoVo;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * Created by zhao chenkai on 2019/10/24.
 */
@ApiModel(description = "子账户交易历史详细Response", value = "子账户交易历史详细Response")
@Data
public class SubAccountTransferHistoryInfoResp extends ToString {

    private static final long serialVersionUID = -3557837018019539054L;

    private List<SubAccountTransferHistoryInfoVo> result;

    private Long count;

}
