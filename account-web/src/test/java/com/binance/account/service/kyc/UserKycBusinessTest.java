package com.binance.account.service.kyc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.user.request.KycAuditRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import javax.annotation.Resource;

@Slf4j
public class UserKycBusinessTest extends BaseTest {

    @Resource
    private UserKycBusiness userKycBusiness;


    @Test
    public void testLoadKycInfo()throws Exception {
        UserKycCountryResponse response = userKycBusiness.getKycCountry(350607355L);
        log.info(" \n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));

        response = userKycBusiness.getKycCountry(350603686L);
        log.info(" \n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
//
//        UserKycHelper.clearKycCountryCache(350568931L);
//
//        response = userKycBusiness.getKycCountry(350568931L);
//        log.info("\n{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
    }

    @Test
    public void testAudit() throws Exception {
        KycAuditRequest request = new KycAuditRequest();
        request.setStatus(KycStatus.passed);
        request.setId(391512050712256513L);
        request.setUserId(350569254L);
        APIResponse response = userKycBusiness.audit(APIRequest.instance(request));
        log.info(JSON.toJSONString(response));

    }

}
