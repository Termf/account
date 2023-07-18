package com.binance.account;

import com.binance.account.error.AccountErrorCode;
import com.binance.account.vo.user.ex.UserStatusEx;
import com.binance.account.vo.user.request.FutureUserAgentReq;
import com.binance.account.yubikey.WebAuthnHelper;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.utils.BitUtils;
import com.binance.master.utils.HashAlgorithms;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.google.common.io.BaseEncoding;
import com.yubico.webauthn.data.ByteArray;
import org.junit.Test;

import java.util.regex.Pattern;

/**
 * Created by Shining.Cai on 2018/09/12.
 **/
public class StaticTest {
    private static final String FUTURE_USER_AGEMTCODE_REGEX = "(?=.*[a-zA-Z])[a-zA-Z0-9]{3,16}";

    @Test
    public void testB(){
        System.out.println(Boolean.valueOf("1"));
        System.out.println(Boolean.valueOf("0"));
        System.out.println(Boolean.valueOf(""));
        System.out.println(Boolean.valueOf("true"));
        System.out.println(Boolean.valueOf(null));

        System.out.println(Math.abs(HashAlgorithms.FNVHash1("2275608692@qq.com") % 20));
        System.out.println(BitUtils.isTrue(1L, Constant.USER_MOBILE));

    }

    @Test
    public void testC() throws Exception {
        Long userId = 35020245L;
        ByteArray byteArray = new ByteArray(userId.toString().getBytes());
        System.out.println(WebAuthnHelper.byteArrayToString(byteArray));
        System.out.println(byteArray.getBase64());
        String  result = new String(BaseEncoding.base64Url().decode(WebAuthnHelper.byteArrayToString(byteArray)), "UTF-8");
        System.out.println(result);
    }
    @Test
    public void testD(){
        FutureUserAgentReq futureUserAgentReq = new FutureUserAgentReq();
        futureUserAgentReq.setFutureAgentCode("ss0");
        System.out.println("result="+Pattern.matches(FUTURE_USER_AGEMTCODE_REGEX, futureUserAgentReq.getFutureAgentCode()));
        System.out.println("result="+StringUtils.isNumeric(futureUserAgentReq.getFutureAgentCode()));
        if (!Pattern.matches(FUTURE_USER_AGEMTCODE_REGEX, futureUserAgentReq.getFutureAgentCode()) && !StringUtils.isNumeric(futureUserAgentReq.getFutureAgentCode())) {
            throw new BusinessException(AccountErrorCode.FUTURE_AGENT_CODE_ERROR);
        }
    }

    public static void main(String[] args) {
        UserStatusEx userStatusEx = new UserStatusEx(208328442145L);
        System.out.println("res="+JsonUtils.toJsonNotNullKey(userStatusEx));
    }
}
