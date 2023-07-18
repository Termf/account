package com.binance.account.vo.user.response;

import com.binance.account.vo.user.enums.UserTypeEnum;
import io.swagger.annotations.ApiModel;
import lombok.*;

@ApiModel(description = "UserTypeResponse", value = "UserTypeResponse")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class UserTypeResponse {
    private UserTypeEnum userType;
}