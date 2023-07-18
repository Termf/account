package com.binance.account.service.certificate;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.binance.account.Application;
import com.binance.account.service.certificate.executor.TestEnv;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import com.binance.account.service.kyc.KycApiTransferAdapter;
import com.binance.account.vo.certificate.KycDetailResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;


@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class UserKycTest extends TestEnv {

    @Resource
    private UserKycBusiness userKycBusiness;
    @Resource
    private KycApiTransferAdapter kycApiTransferAdapter;

    @Test
    public void testCurrentStatus() {
        KycDetailResponse response = userKycBusiness.getCurrentKycStatus(350462089l);
        log.info("response \n ", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
    }

    @Test
    public void testGetKycCounty() throws Exception {
        UserKycCountryResponse response = kycApiTransferAdapter.getKycCountry(350608324L);
        log.info("{}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
    }
}
