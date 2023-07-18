package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.ToString;

/**
 * CheckForbidCodeResponse
 * <p>
 * <p>Copyright (C) 上海比捷网络科技有限公司.</p>
 *
 * @author YueYouqian
 * @since 1.0
 */
@Data
@ToString
public class CheckForbidCodeResponse {

    @ApiModelProperty("用户Id")
    private Long userId;

    @ApiModelProperty("邮箱")
    private String email;

}
