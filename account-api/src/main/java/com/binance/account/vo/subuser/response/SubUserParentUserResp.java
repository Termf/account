package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.ParentUserBaseDetailsVo;
import com.binance.account.vo.subuser.SubUserBaseDetailsVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * Created by Fei.Huang on 2018/10/12.
 */
@Data
@Builder
public class SubUserParentUserResp {

    @ApiModelProperty("子账号BaseDetails")
    private SubUserBaseDetailsVo subUserBaseDetails;

    @ApiModelProperty("母账号BaseDetails")
    private ParentUserBaseDetailsVo parentUserBaseDetails;
}