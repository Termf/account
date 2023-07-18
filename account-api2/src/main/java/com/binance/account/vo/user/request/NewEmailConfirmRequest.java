package com.binance.account.vo.user.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;

@Data
public class NewEmailConfirmRequest {
    @NotBlank
    private String flowId;
    @NotNull
    private Long userId;
    @NotBlank
    private String email;
    @NotBlank
    private String pwd;

    @NotBlank
    private String newSafePwd;

    @NotBlank
    private String sign;
}
