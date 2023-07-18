package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import java.io.Serializable;
import java.util.List;

/**
 * @author yewei
 * @date 2018/11/02
 */
@ApiModel("批量获取用户信息email_Request")
@Getter
@Setter
public class GetUserIdListRequest implements Serializable {

    private static final long serialVersionUID = 4460737869899157395L;

    @ApiModelProperty(name = "emails", required = true)
    @NotEmpty
    private List<String> emails;

}
