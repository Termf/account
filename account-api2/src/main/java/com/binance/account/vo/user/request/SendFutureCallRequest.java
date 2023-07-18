package com.binance.account.vo.user.request;

import com.google.common.collect.Maps;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.Map;

@ApiModel("SendFutureCallRequest")
@Data
public class SendFutureCallRequest {
    @NotNull
    private Long futureUserId;

    Map<String,Object> data = Maps.newHashMap();

    private String templateCodeEmail;

    private String templateCodeSms;

    private String templateCodeInbox;

    private String info;

}
