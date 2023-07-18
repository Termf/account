package com.binance.account.vo.user.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class UserEmailChangeLinkRequest {

    @NotBlank
    private String flowId;

    @NotNull
    private Long userId;

    private String sign;

}
