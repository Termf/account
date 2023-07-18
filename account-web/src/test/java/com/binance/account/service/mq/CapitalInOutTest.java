package com.binance.account.service.mq;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.Application;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.master.enums.SysType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;


/**
 * @author zhenleisun
 * @date 2019-09-03 9:52
 */
@RunWith(MockitoJUnitRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.NONE)
public class CapitalInOutTest {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Test
    public void testCapitalMq() {
        JSONObject item = new JSONObject();
        item.put("userId", 35000001);
        item.put("status", 5);
        item.put("coin","BTC");
        item.put("id","223155");
        item.put("txId","2981838551");
        item.put("targetAddress","TVpAuk29C6h1kuYfNPv6hYA7jhqrbpiioe");
        item.put("sourceAddress","1KKeAXxvK2utHqWnfRkbnryRDYaDC2jUYT");

        MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.USER_CHARGE_CRYPTO, item);

        System.out.println(JSONObject.toJSONString(msg));

        rabbitTemplate.convertAndSend("exchange.pnk.user.query.test", msg);
    }
}
