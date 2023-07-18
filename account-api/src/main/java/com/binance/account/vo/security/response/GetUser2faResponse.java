package com.binance.account.vo.security.response;

import com.binance.account.common.enums.SecurityKeyApplicationScenario;
import com.binance.master.enums.AuthTypeEnum;
import com.google.common.collect.Lists;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

@ApiModel("GetUser2faResponse")
@Data
public class GetUser2faResponse {


    private boolean ifDo2fa;

    private List<AuthTypeEnum> authTypeEnums = Lists.newArrayList();

    private List<SecurityKeyApplicationScenario> youbikeyEnums = Lists.newArrayList();
}
