package com.binance.account.vo.certificate.response;

import com.binance.account.vo.certificate.KycFormAddrVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

@Data
@ApiModel("批量获取用户表单填写的kyc地址返回")
public class KycFormAddrResponse {

    @ApiModelProperty("批量获取用户表单填写的kyc地址内容")
    private List<KycFormAddrVo> kycFormAddrVos;

}
