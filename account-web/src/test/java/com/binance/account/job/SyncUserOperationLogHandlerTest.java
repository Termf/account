package com.binance.account.job;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.data.mapper.useroperation.UserOperationLogMapper;
import com.binance.account.service.kyc.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.text.ParseException;

/**
 * @author szlong
 */
@SpringBootTest
@Slf4j
public class SyncUserOperationLogHandlerTest extends BaseTest {
    @Autowired
    private SyncUserOperationLogHandler syncUserOperationLogHandler;
    @Autowired
    private UserOperationLogMapper userOperationLogMapper;

    @Test
    public void testSend() throws ParseException {
        JSONObject jsonObject = new JSONObject();
//        jsonObject.put("start", new Date());
        jsonObject.put("end", DateUtils.parseDate("2018-05-14 14:41:41", "yyyy-MM-dd HH:mm:ss"));
        jsonObject.put("userId", 20);
        jsonObject.put("batchSize", 100);
        syncUserOperationLogHandler.execute(jsonObject.toJSONString());
    }

    @Test
    public void test() {
        for (int i = 0; i < 20; i++) {
            log.info("{}", i);
            userOperationLogMapper.page(0, 10, null, null, 20L + i);
            log.info("{} end", i);
        }
    }

}
