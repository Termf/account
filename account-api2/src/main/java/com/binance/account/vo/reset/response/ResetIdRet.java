package com.binance.account.vo.reset.response;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author liliang1
 * @date 2019-01-14 18:35
 */
@ApiModel("根据ID获取的重置信息")
@Setter
@Getter
public class ResetIdRet extends ToString {

    private static final long serialVersionUID = -2352754164706435760L;

    @ApiModelProperty("重置ID标识")
    private String id;

    @ApiModelProperty("当前答题序号")
    private Integer questionSeq;

    @ApiModelProperty("重置类型")
    private UserSecurityResetType type;

    @ApiModelProperty("当前状态")
    private UserSecurityResetStatus status;

    @ApiModelProperty("审核信息")
    private String auditMsg;

    @ApiModelProperty("审核时间")
    private Date auditTime;

    @ApiModelProperty("操作JUMIO上传的连接地址")
    private String jumioUrl;

}
