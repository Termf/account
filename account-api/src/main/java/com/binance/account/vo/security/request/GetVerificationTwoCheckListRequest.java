package com.binance.account.vo.security.request;

import com.binance.account.vo.security.enums.BizSceneEnum;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.HashMap;

@ApiModel("GetVerificationTwoCheckListRequest")
@Getter
@Setter
public class GetVerificationTwoCheckListRequest implements Serializable {
    private static final long serialVersionUID = -1751077711097825690L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("业务场景")
    @NotNull
    private BizSceneEnum bizScene;

    @ApiModelProperty("流程Id-新邮箱验证场景需要")
    private String flowId;

    @ApiModelProperty("设备信息")
    private HashMap<String, String> deviceInfo;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
