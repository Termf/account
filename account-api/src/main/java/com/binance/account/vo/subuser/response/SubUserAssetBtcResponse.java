package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.SubUserAssetBtcVo;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@ApiModel(description = "子账号列表及子账号BTC资产总值Response", value = "子账号列表及子账号BTC资产总值Response")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class SubUserAssetBtcResponse  extends ToString {

    private static final long serialVersionUID = -3764498602368957123L;

    private List<SubUserAssetBtcVo> result;

    private Long count;

}
