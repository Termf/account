package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.BrokerSubbindingInfoVo;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("GetSubBindingInfoByPageResp")
@Data
public class GetSubBindingInfoByPageResp {
    private List<BrokerSubbindingInfoVo> result= Lists.newArrayList();

    private Long total=0L;
}
