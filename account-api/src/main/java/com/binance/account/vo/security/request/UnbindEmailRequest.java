package com.binance.account.vo.security.request;

import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("UnbindEmailRequest")
@Getter
@Setter
public class UnbindEmailRequest extends ToString {
    private static final long serialVersionUID = 7153266700355846634L;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("密码")
    @NotEmpty
    private String password;

    @ApiModelProperty("邮件验证码")
    @NotEmpty
    private String emailCode;

    @ApiModelProperty(readOnly = true, notes = "设备信息")
    private Map<String, String> deviceInfo;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
