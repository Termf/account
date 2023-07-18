package com.binance.account.utils;

import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlBlockHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.fastjson.JSONObject;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Service
@Slf4j
public class CustomUrlBlockHandler implements UrlBlockHandler {
    @Override
    public void blocked(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, BlockException e) throws IOException {
        httpServletResponse.setHeader("Content-Type","application/json;charset=UTF-8");

        log.warn("BlockException occur, path={}", httpServletRequest.getRequestURI());
        APIResponse apiResponse =  APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.GW_TOO_MANY_REQUESTS.getCode(), "Too many requests. Please try again later.");
        httpServletResponse.getWriter().write(JSONObject.toJSONString(apiResponse));
    }
}
