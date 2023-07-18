package com.binance.account.vo.user.response;

import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

@ApiModel("GetBatchUserTypeListResponse")
@Getter
@Setter
public class GetBatchUserTypeListResponse implements Serializable {

    private static final long serialVersionUID = -757758207626542672L;
    @ApiModelProperty(name = "userid")
    private Long userId;

    @ApiModelProperty(name = "账号")
    private String email;

    @ApiModelProperty(name = "userType")
    private String userType;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
