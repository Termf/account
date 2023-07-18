package com.binance.account.service.certificate.impl;

import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.common.enums.CertificateType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.Jumio;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.mapper.certificate.JumioMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.service.country.impl.CountryBusiness;
import com.binance.account.service.security.impl.FaceBusiness;
import com.binance.account.service.user.impl.UserBusiness;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.country.CountryVo;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioDocumentType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.messaging.common.utils.UUIDUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * 为了解决bean循环引用的问题添加的工具类
 */
@Component
@Log4j2
public class UserKycHelper implements ApplicationContextAware {

    private static UserBusiness userBusiness;
    private static UserKycApproveMapper userKycApproveMapper;
    private static ApolloCommonConfig apolloCommonConfig;
    private static JumioMapper jumioMapper;
    private static FaceBusiness faceBusiness;
    private static JumioBusiness jumioBusiness;
    private static CountryBusiness countryBusiness;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        UserKycHelper.userBusiness = applicationContext.getBean(UserBusiness.class);
        UserKycHelper.userKycApproveMapper = applicationContext.getBean(UserKycApproveMapper.class);
        UserKycHelper.apolloCommonConfig = applicationContext.getBean(ApolloCommonConfig.class);
        UserKycHelper.jumioMapper = applicationContext.getBean(JumioMapper.class);
        UserKycHelper.faceBusiness = applicationContext.getBean(FaceBusiness.class);
        UserKycHelper.jumioBusiness = applicationContext.getBean(JumioBusiness.class);
        UserKycHelper.countryBusiness = applicationContext.getBean(CountryBusiness.class);
    }

    /**
     * kyc 通过后创建c2c 账户
     * @param userId
     */
    public static void createFiatAccount(Long userId, boolean ignoreKycCountryCheck) {
    	String uuid = StringUtils.isNotBlank(TrackingUtils.getTrackingChain())?TrackingUtils.getTrackingChain(): UUIDUtils.getId();
        AsyncTaskExecutor.execute(() -> {
            try {
            	TrackingUtils.putTracking("FIAT_ACCOUNT",uuid);
            	log.info("中国用户,开通法币账户.启动异步线程创建. userId:{}",userId);
                //三秒后再调用，主要方式上一个事务和下一个事务冲突的问题
                Thread.sleep(3000);
                UserIdRequest request = new UserIdRequest();
                request.setUserId(userId);
                userBusiness.createFiatAccount(APIRequest.instance(request), ignoreKycCountryCheck);
            }catch (Exception e) {
                log.error("kyc 通过后开启C2C账户异常, userId:{} ", userId, e);
            }finally {
				TrackingUtils.removeTracking();
				TrackingUtils.removeTraceId();
			}
        });
    }

    /**
     * 清除kyc信息的缓存
     * @param userId
     */
    public static void clearKycCountryCache(Long userId) {
        String redisKey = String.format(AccountConstants.KYC_COUNTRY_CACHE_PRE, userId);
        RedisCacheUtils.del(redisKey);
    }

    public static UserKycCountryResponse loadUserKycInfo(Long userId) {
        UserKycCountryResponse response = new UserKycCountryResponse();
        // default
        response.setUserId(userId);
        response.setCertificateType(CertificateType.UNVERIFIED.getCode());
        response.setCountryCode(null);
        String redisKey = String.format(AccountConstants.KYC_COUNTRY_CACHE_PRE, userId);
        try {
            // step1: 从redis 缓存获取，获取不到再从库表考虑
            UserKycCountryResponse redisCache = RedisCacheUtils.get(redisKey, UserKycCountryResponse.class);
            if (redisCache != null) {
                return redisCache;
            }
            // step2: 缓存获取不到时，从根据数据查询来获取
            UserKycApprove userKycApprove = userKycApproveMapper.selectByPrimaryKey(userId);
            if (userKycApprove == null) {
                // 当前用户就没有通过KYC, 做Redis缓存(120s)后返回默认值,
                RedisCacheUtils.set(redisKey, response, apolloCommonConfig.getKycCountryCacheTimes());
                return response;
            }
            // 如果存在有值，获取类型和JUMIO的认证国籍
            CertificateType certificateType = CertificateType.getByCode(userKycApprove.getCertificateType());
            response.setCertificateType(certificateType.getCode());
            // 用户输入的国籍
            String fillCountry = (userKycApprove.getBaseInfo() == null
                    || StringUtils.isBlank(userKycApprove.getBaseInfo().getCountry())) ? null
                    : userKycApprove.getBaseInfo().getCountry();
            Jumio jumio = null;
            if (Objects.equals(CertificateType.COMPANY, certificateType) && StringUtils.isNotBlank(fillCountry)) {
                // 企业用户的数据，直接返回的是用户填写的国籍码
                response.setCountryCode(fillCountry);
            } else if (StringUtils.isNotBlank(userKycApprove.getJumioId())) {
                // 个人认证 和 当企业认证手提国籍为空时，返回证件上的国籍
                jumio = jumioMapper.selectByPrimaryKey(userId, userKycApprove.getJumioId());
                if (jumio != null && StringUtils.isNotBlank(jumio.getIssuingCountry())) {
                    response.setCountryCode(jumio.getIssuingCountry());
                    response.setBirthday(jumio.getDob());
                }
            }else {
                // 查询用户最后一次的jumio信息
                JumioInfoVo jumioInfoVo = jumioBusiness.getLastByUserId(userId);
                if (jumioInfoVo != null && JumioStatus.PASSED.equals(jumioInfoVo.getStatus())) {
                    CountryVo countryVo = countryBusiness.getCountryByAlpha3WithCache(jumioInfoVo.getIssuingCountry());
                    if (countryVo != null) {
                        response.setCountryCode(countryVo.getCode());
                    }
                    response.setBirthday(jumioInfoVo.getDob());
                    response.setIdNumber(jumioInfoVo.getNumber());
                }
            }
            if (StringUtils.isBlank(response.getCountryCode()) && StringUtils.isNotBlank(fillCountry)) {
                response.setCountryCode(fillCountry);
            }
            // 添加姓名
            UserKycApprove.BaseInfo baseInfo = userKycApprove.getBaseInfo();
            if (baseInfo != null) {
                response.setFirstName(baseInfo.getFirstName());
                response.setLastName(baseInfo.getLastName());
                response.setCity(baseInfo.getCity());
                response.setAddress(baseInfo.getAddress());
                response.setFillFirstName(baseInfo.getFirstName());
                response.setFillMiddleName(baseInfo.getMiddleName());
                response.setFillLastName(baseInfo.getLastName());
                if (baseInfo.getDob() != null) {
                    response.setFillBirthday(DateUtils.formatter(baseInfo.getDob(), DateUtils.SIMPLE_PATTERN));
                }
            }
            // 添加证件类型和证件号
            if (Objects.equals(CertificateType.USER, certificateType)) {
                if ("CN".equalsIgnoreCase(response.getCountryCode())) {
                    // 中国的用户的话，优先考虑从face ocr 中获取
                    FaceIdCardOcrVo ocrVo = faceBusiness.getFaceIdCardOcr(userId);
                    if (ocrVo != null && IdCardOcrStatus.PASS.equals(ocrVo.getStatus())) {
                        response.setIdNumber(ocrVo.getIdcardNumber());
                        response.setDocumentType(JumioDocumentType.ID_CARD.name());
                        response.setCanDoC2C(true);
                        response.setBirthday(idCardOcrBirthday(ocrVo.getBirthYear(), ocrVo.getBirthMonth(), ocrVo.getBirthDay()));
                    }
                }
                if (jumio != null && StringUtils.isBlank(response.getIdNumber())) {
                    response.setIdNumber(jumio.getNumber());
                    response.setDocumentType(jumio.getDocumentType());
                }
            }
            RedisCacheUtils.set(redisKey, response, apolloCommonConfig.getKycCountryCacheTimes());
            return response;
        } catch (Exception e) {
            log.error("获取用户KYC国籍异常: userId:{}", userId, e);
            throw e;
        }
    }

    public static String idCardOcrBirthday(String year, String month, String day) {
        if (StringUtils.isAnyBlank(year, month, day)) {
            return null;
        }
        StringBuilder sb = new StringBuilder(year).append("-");
        if (month.length() < 2) {
            sb.append("0").append(month);
        }else {
            sb.append(month);
        }
        sb.append("-");
        if (day.length() < 2) {
            sb.append("0").append(day);
        }else {
            sb.append(day);
        }
        return sb.toString();
    }
}
