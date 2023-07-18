package com.binance.account.utils;

import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.ErrorCode;
import com.binance.master.web.handlers.MessageHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.WebUtils;

import javax.annotation.Resource;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.text.MessageFormat;
import java.util.Locale;

/**
 * 由于common 中的MessageHelp存在有没有HtppServletRequest的异常情况，WebUtils.getHttpServletRequest() 存在空指针异常风险
 * @author liliang1
 * @date 2019-01-04 10:30
 */
@Log4j2
@Component
public class MessageUtils {

    public static final String LANG = "lang";
    @Resource
    private MessageHelper messageHelper;

    public String getMessage(ErrorCode errorCode, Object... params) {
        String key = errorCode.getClass().getName() + "." + errorCode;
        return getMessage(key, params);
    }

    public String getMessage(String key, Object... params) {
        // 国际化暂时只支持中英文，默认为英文
        Locale locale = Locale.US;
        if (StringUtils.equals("cn", getLanguage())) {
            locale = Locale.CHINA;
        }
        return getMessage(key, locale, params);
    }

    public String getI18nMessage(String key, Object... params) {
        Locale locale = ObjectUtils.defaultIfNull(Locale.forLanguageTag(getLanguage()), Locale.US);
        if ("cn".equals(locale.getLanguage())) {
            locale = Locale.CHINA;
        }
        return getI18nMessage(key, locale, params);
    }

    private String getI18nMessage(String key, Locale locale, Object... params) {
        String msg;
        try {
            msg = messageHelper.getMessage(key, locale);
            if (params != null) {
                msg = MessageFormat.format(msg, params);
            }
        } catch (NoSuchMessageException e) {
            msg = key;
        } catch (Throwable e) {
            log.error("国际化异常：", e);
            msg = key;
        }
        return msg;
    }

    public String getMessage(String key, Locale locale, Object... params) {
        if (locale != Locale.CHINA) {
            locale = Locale.US;
        }
        return getI18nMessage(key, locale, params);
    }

    public String getMessage(String key, LanguageEnum language, Object... params) {
        // 国际化暂时只支持中英文，默认为英文
        Locale locale = Locale.US;
        if (language != null && language == LanguageEnum.ZH_CN) {
            locale = Locale.CHINA;
        }
        return getMessage(key, locale, params);
    }

    public String getLanguage() {
        HttpServletRequest request = getHttpServletRequest();
        if (request != null) {
            String language = request.getHeader(LANG);
            if (StringUtils.isBlank(language)) {
                language = request.getParameter(LANG);
            }
            if (StringUtils.isBlank(language)) {
                language = getCookieValue(request, LANG);
            }
            if (StringUtils.isBlank(language) || StringUtils.equals(language, "undefined")) {
                language = "en";
            }
            return language;
        }
        return "en";
    }

    private HttpServletRequest getHttpServletRequest() {
        if (RequestContextHolder.getRequestAttributes() != null) {
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            return attributes.getRequest();
        }
        return null;
    }

    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie cookie = WebUtils.getCookie(request, cookieName);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
}
