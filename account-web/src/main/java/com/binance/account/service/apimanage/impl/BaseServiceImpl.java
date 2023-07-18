package com.binance.account.service.apimanage.impl;

import com.binance.account.service.apimanage.IBaseService;
import com.binance.account.utils.MatchboxReturnUtils;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIRequest.VoidBody;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.TrackingUtils;
import com.binance.master.utils.WebUtils;
import com.binance.sysconf.service.SysConfigVarCacheService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

@Slf4j
public abstract class BaseServiceImpl implements IBaseService {

    @Autowired
    private SysConfigVarCacheService sysConfigVarCacheService;

    @Override
    public <T> APIRequest<T> newAPIRequest(T body) {
        return this.newAPIRequest(body, null);
    }

    @Override
    public <T> APIRequest<T> newAPIRequest(T body, String language) {
        APIRequest<T> request = new APIRequest<>();
        request.setBody(body);
        if (StringUtils.isBlank(language)) {
            request.setLanguage(LanguageEnum.EN_US);
        } else {
            LanguageEnum lang = LanguageEnum.findByLang(language);
            if (lang == null) {
                request.setLanguage(LanguageEnum.EN_US);
            } else {
                request.setLanguage(lang);
            }
        }
        request.setTerminal(TerminalEnum.WEB);
        request.setTrackingChain(TrackingUtils.getTrackingChain());
        return request;
    }

    @Override
    public APIRequest<VoidBody> newVoidAPIRequest() {
        return this.newVoidAPIRequest(null);
    }

    @Override
    public APIRequest<VoidBody> newVoidAPIRequest(String language) {
        return this.newAPIRequest(new VoidBody(), language);
    }

    @Override
    public <T> T getAPIRequestResponse(APIResponse<T> response) {
        if (APIResponse.Status.OK.equals(response.getStatus())) {
            return response.getData();
        } else {
            Object errorData = response.getErrorData();
            if (errorData == null) {
                errorData = response.getSubData();
            }
            MatchboxReturnUtils.processMbxErrorMsg(errorData.toString(), "调用撮合失败返回");
            throw new BusinessException(GeneralCode.findByCode(response.getCode()), errorData.toString());
        }
    }

    @Override
    public String getHttpBasePath() {
        String exch_domain = WebUtils.getHeader(Constant.BASE_URL);

        String exchDomain = sysConfigVarCacheService.getValue("email_domain");
        String[] exchDomains = exchDomain.split(";");
        boolean flag = false;
        for (String domain : exchDomains) {
            if (domain.equals(exch_domain)) {
                flag = true;
                log.info("获取到域名正确：{}", exch_domain);
                break;
            }
        }

        if (flag) {
            if (!exch_domain.contains("https") && exch_domain.startsWith("http")) {
                exch_domain = exch_domain.replaceFirst("http", "https");
                return exch_domain;
            }
            return exch_domain;
        } else {
            log.info("获取到域名不正确：{}", exch_domain);

            if ("http://www.binance.cloud/".equals(exch_domain)) {
                // 中国用户访问域名
                exch_domain = sysConfigVarCacheService.getValue("china_exch_domain");
                if (!exch_domain.endsWith("/")) {
                    exch_domain = exch_domain + "/";
                }
                log.info(".cloud访,获取DB中国用户域名：{}", exch_domain);
            } else {
                // 对外的网站域名
                exch_domain = sysConfigVarCacheService.getValue("exch_domain");
                if (!exch_domain.endsWith("/")) {
                    exch_domain = exch_domain + "/";
                }
                log.info("从DB获取正确默认域名：{}", exch_domain);
            }

            return exch_domain;
        }
    }
}
