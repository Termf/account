package com.binance.account.service.async;

import com.alibaba.fastjson.JSON;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import com.binance.messaging.api.msg.MsgApi;
import com.binance.messaging.api.msg.TwilioApi;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.messaging.api.msg.response.MsgResponse;
import com.binance.messaging.api.twilio.request.TwilioFeedBackRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by Fei.Huang on 2018/12/12.
 */
@Slf4j
@Component
public class MsgAsyncTask {
    @Resource
    protected UserInfoMapper userInfoMapper;
    @Resource
    private MsgApi msgApi;
    @Resource
    private TwilioApi twilioApi;

    @Resource
    protected IMsgNotification iMsgNotification;


    @Async("simpleRequestAsync")
    public void sendMsgTry(SendMsgRequest request, String remark, LanguageEnum languageEnum,
            TerminalEnum terminalEnum) {
        try {
            sendMsg(request, languageEnum, terminalEnum);
        } catch (Throwable e) {
            log.error(String.format("sendMsgTry failed, remark:%s, exception:", remark), e);
        }
    }

    @Async("simpleRequestAsync")
    public void sendTwilioFeedBack(TwilioFeedBackRequest request,LanguageEnum languageEnum,
            TerminalEnum terminalEnum) {
        try {
            sendTwilioFeedback(request, languageEnum, terminalEnum);
        } catch (Throwable e) {
            log.warn("sendTwilioFeedback failed:",e);
        }
    }


    /**
     * 发送消息
     *
     * @param request
     * @throws Exception
     */
    public void sendMsg(SendMsgRequest request, LanguageEnum languageEnum, TerminalEnum terminalEnum) {
        APIRequestHeader originRequest = new APIRequestHeader();
        if (null == languageEnum) {
            originRequest.setLanguage(WebUtils.getAPIRequestHeader().getLanguage());
        } else {
            originRequest.setLanguage(languageEnum);
        }
        if (null == terminalEnum) {
            originRequest.setTerminal(WebUtils.getAPIRequestHeader().getTerminal());
        } else {
            originRequest.setTerminal(terminalEnum);
        }

        // 根据用户id查找用户防钓鱼码
        request.setAntiPhishingCode(this.userInfoMapper.selectPhishingCode(Long.valueOf(request.getUserId())));
        APIResponse<MsgResponse> resp;
        try {
            resp = this.msgApi.sendMsg(APIRequest.instance(originRequest, request));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        log.info("UserBusiness.sendMsg Resp:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(resp)));
        if (resp.getStatus() == APIResponse.Status.ERROR) {
            // throw new BusinessException(GeneralCode.findByCode(resp.getCode()));
            throw new BusinessException(resp.getCode(), Objects.toString(resp.getErrorData(), ""));
        }
    }


    /**
     * 发送TwilioFeedback API
     *
     */
    public void sendTwilioFeedback(TwilioFeedBackRequest request, LanguageEnum languageEnum, TerminalEnum terminalEnum) {
        APIRequestHeader originRequest = new APIRequestHeader();
        if (null == languageEnum) {
            originRequest.setLanguage(WebUtils.getAPIRequestHeader().getLanguage());
        } else {
            originRequest.setLanguage(languageEnum);
        }
        if (null == terminalEnum) {
            originRequest.setTerminal(WebUtils.getAPIRequestHeader().getTerminal());
        } else {
            originRequest.setTerminal(terminalEnum);
        }

        try {
            this.twilioApi.twilioFeedBack(APIRequest.instance(originRequest, request));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
