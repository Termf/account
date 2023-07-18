package com.binance.account.vo.subuser;

import com.binance.account.vo.user.response.BaseDetailResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.Date;

/**
 * Created by Fei.Huang on 2018/10/12.
 */
@Data
public class ParentUserBaseDetailsVo extends BaseDetailResponse {

    @ApiModelProperty(name = "母账号UserId")
    private Long parentUserId;

    @ApiModelProperty(name = "旗下子账号数量")
    private Long subUserCount;

    @ApiModelProperty(name = "母账号创建时间")
    private Date createTime;

}