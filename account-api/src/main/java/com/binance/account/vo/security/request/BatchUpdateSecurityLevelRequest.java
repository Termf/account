package com.binance.account.vo.security.request;

import java.io.Serializable;
import java.util.List;

import javax.validation.constraints.NotNull;

import com.binance.account.vo.security.UserSecurityLevelVo;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@ApiModel("批量设置用户安全级别Request")
@Data
public class BatchUpdateSecurityLevelRequest implements Serializable {

    private static final long serialVersionUID = 5575181272479275430L;

    @ApiModelProperty("用户ID及安全等级列表")
    @NotNull
    private List<UserSecurityLevelVo> list;
}
