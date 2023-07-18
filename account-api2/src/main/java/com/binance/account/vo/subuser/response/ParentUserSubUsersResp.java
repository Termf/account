package com.binance.account.vo.subuser.response;

import com.binance.account.vo.subuser.ParentUserBaseDetailsVo;
import com.binance.account.vo.subuser.SubUserBaseDetailsVo;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * Created by Fei.Huang on 2018/10/12.
 */
@Data
@Builder
public class ParentUserSubUsersResp {

    @ApiModelProperty("母账号UserId")
    private Long parentUserId;

    @ApiModelProperty("子账号数量")
    private Integer subUserCount;

    @ApiModelProperty("母账号BaseDetails")
    private ParentUserBaseDetailsVo parentUserBaseDetails;

    @ApiModelProperty("子账号BaseDetails")
    private List<SubUserBaseDetailsVo> subUserBaseDetailsList;

}