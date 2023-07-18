package com.binance.account.integration.message;

import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.service.security.IUserSecurity;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.futures.enums.FutureEmailTypeEnum;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.StringUtils;
import com.binance.messaging.api.msg.MsgApi;
import com.binance.messaging.api.msg.request.SendMsgRequest;
import com.binance.messaging.api.msg.response.MsgResponse;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
//import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;

/**
 * 提供可读性更强的发送邮件和发送短信的api封装，调用方不要懂很多细节
 * */
@Log4j2
@Service
public class MsgApiClient {
    @Resource
    private MsgApi msgApi;
    @Autowired
    private UserCommonBusiness userCommonBusiness;
    @Autowired
    private IUserSecurity userSecurityService;
    @Resource
    private UserInfoMapper userInfoMapper;
    /**
     * @param tplCode 模板代号
     * @param cellPhone 手机号（必填）
     * @param userId 用户的userid（必填）
     * @param  param 发送短信的模板当中的参数，具体根据模板的不同会有区别
     * */
    public void sendMsg(String tplCode, String cellPhone, Map<String, Object> param, String userId) {
        SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setTplCode(tplCode);
        requestMsg.setRecipient(cellPhone);
        requestMsg.setData(param);
        requestMsg.setUserId(userId);
        requestMsg.setMobileCode("86");
        requestMsg.setNeedIpCheck(false);
        requestMsg.setNeedSendTimesCheck(false);
        APIRequestHeader originRequest = new APIRequestHeader();
        originRequest.setLanguage(LanguageEnum.ZH_CN);
        originRequest.setTerminal(TerminalEnum.WEB);
        APIResponse<MsgResponse> resp = null;
        try {
            resp = msgApi.sendMsg(APIRequest.instance(originRequest, requestMsg));
            log.info("MessageService.sendMsg Resp:{}", JsonUtils.toJsonNotNullKey(resp));
            if (resp.getStatus() == APIResponse.Status.ERROR) {
                throw new BusinessException(GeneralCode.findByCode(resp.getCode()));
            }
        } catch (Exception e) {
            log.error("sendMsg failed,", e);
        }
    }
    /**
     * @param tplCode 模板代号（必填）
     * @param email 邮箱（必填）
     * @param userId 用户的userid（必填）
     * @param antiPhishingCode 用户的钓鱼码（必填）
     * @param  param 发送短信的模板当中的参数，具体根据模板的不同会有区别
     * */
    public void sendEmail(String tplCode, String email, String userId,
                          String antiPhishingCode, String language,
                          String terminal, Map<String, Object> param) throws Exception {
        final SendMsgRequest requestMsg = new SendMsgRequest();
        requestMsg.setRecipient(email);
        requestMsg.setUserId(userId);
        requestMsg.setTplCode(tplCode);
        requestMsg.setData(param);
        requestMsg.setAntiPhishingCode(antiPhishingCode);
        APIRequestHeader originRequest = new APIRequestHeader();
        originRequest.setLanguage(null != LanguageEnum.findByCode(language) ? LanguageEnum.findByCode(language) : LanguageEnum.findByLang(language));
        originRequest.setTerminal(TerminalEnum.findByCode(terminal));
        APIResponse<MsgResponse> resp = null;
        log.info("MessageService.sendEmail requestMsg:{}", JsonUtils.toJsonNotNullKey(requestMsg));
        resp = msgApi.sendMsg(APIRequest.instance(originRequest, requestMsg));
        log.info("MessageService.sendEmail Resp:{}", JsonUtils.toJsonNotNullKey(resp));
        if (resp.getStatus() == APIResponse.Status.ERROR) {
            throw new BusinessException(GeneralCode.findByCode(resp.getCode()));
        }
    }

    public void sendEnableSubAccountEmail(Long userId, String language, String terminal) throws Exception{
        User user = userCommonBusiness.checkAndGetUserById(userId);
        String tplCode = AccountConstants.SUB_ACCOUNT_ENABLE_NOTIFICATION;
        String antiPublishCode = "";
        try {
            APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            apiRequest.setBody(userIdRequest);
            APIResponse<UserSecurityVo> result = userSecurityService.getUserSecurityByUserId(apiRequest);
            antiPublishCode = result.getData().getAntiPhishingCode();
            log.info("getUserSecurityByUserId userId:{}, result:{}", userId, result);
        } catch (Exception e) {
            log.error("getUserSecurityByUserId error, userId:{}, msg:", userId, e);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        sendEmail(tplCode,user.getEmail(),userId.toString(),antiPublishCode,language,terminal,Maps.newHashMap());
    }

    public void sendCreateFuturePushEmail(Long userId, String email, String language, String terminal, String emailCode) throws Exception{
        String antiPublishCode = "";
        try {
            APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            apiRequest.setBody(userIdRequest);
            APIResponse<UserSecurityVo> result = userSecurityService.getUserSecurityByUserId(apiRequest);
            antiPublishCode = result.getData().getAntiPhishingCode();
            log.info("getUserSecurityByUserId userId:{}, result:{}", userId, result);
        } catch (Exception e) {
            log.error("getUserSecurityByUserId error, userId:{}, msg:", userId, e);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        sendEmail(emailCode,email,userId.toString(),antiPublishCode,language,terminal,Maps.newHashMap());
    }

    public void sendFutureClosePositionEmail(Long userId, String symbol, FutureEmailTypeEnum futureEmailTypeEnum, BigDecimal makePrice,BigDecimal totalMarginBalance) throws Exception{
        User user = userCommonBusiness.checkAndGetUserById(userId);
        String tplCode = null;
        if(FutureEmailTypeEnum.LIQUIDATION==futureEmailTypeEnum){
            tplCode = AccountConstants.FUTURE_LIQUIDATION_CALL;
        }
        if(FutureEmailTypeEnum.ADL==futureEmailTypeEnum){
            tplCode = AccountConstants.FUTURE_ADL_CALL;
        }
        if(FutureEmailTypeEnum.DELIVERY_LIQUIDATION==futureEmailTypeEnum){
            tplCode = AccountConstants.DELIVERY_LIQUIDATION_CALL;
        }
        if(FutureEmailTypeEnum.DELIVERY_ADL==futureEmailTypeEnum){
            tplCode = AccountConstants.DELIVERY_ADL_CALL;
        }
        if(StringUtils.isBlank(tplCode)){
            log.info("sendFutureClosePositionEmail:skip because of empty tplcode userId={}",userId);
            return;
        }
        String antiPublishCode = "";
        String language="";
        Map params=Maps.newHashMap();
        try {
            APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            apiRequest.setBody(userIdRequest);
            APIResponse<UserSecurityVo> result = userSecurityService.getUserSecurityByUserId(apiRequest);
            antiPublishCode = result.getData().getAntiPhishingCode();
            language=getUserLastLoginLanguage(userId);
            params.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
            params.put("symbol", symbol);
            params.put("email", user.getEmail());
            params.put("mark_price", makePrice);
            params.put("balance", totalMarginBalance);
            if(FutureEmailTypeEnum.DELIVERY_LIQUIDATION==futureEmailTypeEnum){
                params.put("asset", (StringUtils.isNotBlank(symbol) && symbol.contains("USD_")) ? symbol.substring(0,symbol.indexOf("USD_")) : "BTC");
            }
            log.info("sendFutureClosePositionEmail userId:{}", userId);
        } catch (Exception e) {
            log.error("sendFutureClosePositionEmail error, userId:{}, msg:", userId, e);
            throw new BusinessException(GeneralCode.USER_NOT_EXIST);
        }
        sendEmail(tplCode,user.getEmail(),userId.toString(),antiPublishCode,language,TerminalEnum.WEB.getCode(),params);
    }

    public String getUserLastLoginLanguage(Long userId) throws Exception {
        UserConfig uc = new UserConfig();
        uc.setUserId(userId);
        uc.setConfigType("userLastLoginLanguage");
        UserConfig result=userInfoMapper.selectLatestUserConfig(uc);
        if(null==result){
            return null;
        }
        return result.getConfigName();
    }

    public void sendFutureMarginCall(Long userId ,String symbol,String tplCode) throws Exception{
        User user = userCommonBusiness.checkAndGetUserById(userId);

        String antiPublishCode = "";
        String language="";
        Map params=Maps.newHashMap();
        try {
            APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            apiRequest.setBody(userIdRequest);
            APIResponse<UserSecurityVo> result = userSecurityService.getUserSecurityByUserId(apiRequest);
            antiPublishCode = result.getData().getAntiPhishingCode();
            language=getUserLastLoginLanguage(userId);
            params.put("email", user.getEmail());
            params.put("symbol", symbol);
            log.info("sendFutureMarginCallnEmail userId:{}", userId);
            sendEmail(tplCode,user.getEmail(),userId.toString(),antiPublishCode,language,TerminalEnum.WEB.getCode(),params);
        } catch (Exception e) {
            log.error("sendFutureMarginCallEmail error, userId:{}, msg:", userId, e);
        }

    }

    public void sendFutureFundingRate(Long userId ,String symbol,String rate,String amount,String time,String tplCode) throws Exception{
        User user = userCommonBusiness.checkAndGetUserById(userId);

        String antiPublishCode = "";
        String language="";
        Map params=Maps.newHashMap();
        try {
            APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            apiRequest.setBody(userIdRequest);
            APIResponse<UserSecurityVo> result = userSecurityService.getUserSecurityByUserId(apiRequest);
            antiPublishCode = result.getData().getAntiPhishingCode();
            language=getUserLastLoginLanguage(userId);
            params.put("email", user.getEmail());
            params.put("symbols", symbol);
            params.put("rates", rate);
            params.put("amounts", amount);
            params.put("time", time);
            log.info("sendFutureFundingRateEmail userId:{}", userId);
            sendEmail(tplCode,user.getEmail(),userId.toString(),antiPublishCode,language,TerminalEnum.WEB.getCode(),params);
        } catch (Exception e) {
            log.error("sendFutureFundingRateEmail error, userId:{}, msg:", userId, e);
        }

    }

    public void sendFutureCall(Long userId ,Map params,String tplCode) throws Exception{
        User user = userCommonBusiness.checkAndGetUserById(userId);

        String antiPublishCode = "";
        String language="";
        try {
            APIRequest<UserIdRequest> apiRequest = new APIRequest<>();
            UserIdRequest userIdRequest = new UserIdRequest();
            userIdRequest.setUserId(userId);
            apiRequest.setBody(userIdRequest);
            APIResponse<UserSecurityVo> result = userSecurityService.getUserSecurityByUserId(apiRequest);
            antiPublishCode = result.getData().getAntiPhishingCode();
            language=getUserLastLoginLanguage(userId);
            params.put("email", user.getEmail());
            if(null == params.get("time")) {
                params.put("time", DateUtils.formatterUTC(DateUtils.getNewUTCDate(), DateUtils.EMAIL_TITLE_UTC));
            }
            log.info("sendFutureCallnEmail userId:{}", userId);
            sendEmail(tplCode,user.getEmail(),userId.toString(),antiPublishCode,language,TerminalEnum.WEB.getCode(),params);
        } catch (Exception e) {
            log.error("sendFutureCallEmail error, userId:{}, msg:", userId, e);
        }

    }
}
