package com.binance.account.vo.user.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class UserEmailChangeInitFlowRequest {

    @NotNull
    private Long userId;

    @NotBlank
    private String email;

    private Integer availableType;//0： 老邮箱可用，1：老邮箱不可用


}
