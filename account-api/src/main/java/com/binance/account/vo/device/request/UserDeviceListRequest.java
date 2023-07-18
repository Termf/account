package com.binance.account.vo.device.request;

import com.binance.master.commons.Page;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("用户设备列表查询Request")
@Getter
@Setter
public class UserDeviceListRequest extends Page implements Serializable {

    private static final long serialVersionUID = 4337520945072011095L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("新增设备时的来源 login,regist,withdraw")
    private String source;

    @ApiModelProperty("排除新增设备时的来源 login,regist,withdraw")
    private String excludeSource;

    @ApiModelProperty("终端类型")
    private String agentType;

    @ApiModelProperty("设备状态")
    private Integer status;

    @ApiModelProperty("是否展示已删除的设备 默认为false")
    private boolean showDeleted = false;

    @ApiModelProperty("查询参数-用于后台ES检索")
    private String searchParams;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
