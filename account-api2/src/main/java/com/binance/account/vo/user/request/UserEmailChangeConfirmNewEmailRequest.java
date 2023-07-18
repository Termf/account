package com.binance.account.vo.user.request;

import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

@Data
public class UserEmailChangeConfirmNewEmailRequest {


    @NotBlank
    private String flowId;

    @NotBlank
    private String email;

    @NotBlank
    private String pwd;

    private String newSafePwd;

    private Long userId;


}
