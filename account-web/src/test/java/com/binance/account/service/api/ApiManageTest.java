package com.binance.account.service.api;

import com.binance.account.Application;
import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.account.data.mapper.apimanage.ApiModelMapper;
import com.binance.account.service.apimanage.impl.ApiManageServiceImpl;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.UUID;

/**
 * @Author: mingming.sheng
 * @Date: 2020/5/7 11:00 上午
 */
@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class ApiManageTest {
    @Resource
    ApiModelMapper apiModelMapper;

    @Test
    public void testMapper() throws Exception {
        ApiModel apiModel = new ApiModel();
        apiModel.setUuid("ad0a9cd5ddab478fa36b6a6d5111111");
        apiModel.setUserId("350536844");
        apiModel.setApiKey("testtest");
        apiModel.setKeyId(120);
        apiModel.setApiName("test-api");
        // apiModel.setSecretKey(CryptoUtils.encryptAESToString(apiKeyVo.getSecretKey(), this.aesPass));
        apiModel.setSecretKey("xxxxxxxxxx");
        apiModel.setTradeIp("10.0.0.1");
        apiModel.setWithdrawIp("10.0.0.1");
        apiModel.setStatus(1);
        apiModel.setEnableWithdrawStatus(false);
        apiModel.setRuleId(String.valueOf(ApiManageServiceImpl.ApiManagerUtils.TRADE));
        apiModel.setInfo("account-service创建");
        apiModel.setApiEmailVerify(false);
        apiModel.setCreateEmailSendTime(DateUtils.getNewUTCDate());
        apiModel.setCreateTime(DateUtils.getNewUTCDate());
        apiModel.setType("HMAC_SHA256");
        try {
            long id = apiModelMapper.insertWithId(apiModel);
            System.out.println(id);
            System.out.println(apiModel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
