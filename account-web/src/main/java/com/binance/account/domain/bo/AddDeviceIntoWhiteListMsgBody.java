package com.binance.account.domain.bo;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class AddDeviceIntoWhiteListMsgBody {

    private String whitelistType = "USER_WHITELISTED_DEVICEPK";

    private String requestTime;

    private String remark;

    private Map<String, Object> value;

}
