package com.binance.account.vo.yubikey;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class StartAuthenticateResponse implements Serializable {

    private static final long serialVersionUID = -4427958831269693133L;

    private String requestId;

    private JSONObject creationOptions;
}
