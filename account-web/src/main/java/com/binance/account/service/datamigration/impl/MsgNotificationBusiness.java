package com.binance.account.service.datamigration.impl;

import javax.annotation.Resource;

import com.binance.account.constants.enums.AccountSysTypeEnum;
import com.binance.account.domain.bo.AccountMsgNotification;
import com.binance.account.domain.bo.FrontPushEventType;
import com.binance.master.utils.LogMaskUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.master.constant.MQConstant;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class MsgNotificationBusiness implements IMsgNotification {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Override
    public void send(MsgNotification msg) {
        try {
            log.info("MsgNotificationBusiness.send:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
            String routingKey;
            switch (msg.getSysType()) {
                case PNK_ADMIN:
                    routingKey = MQConstant.ACCOUNT_PNK_ADMIN_USER_QUERY;
                    break;
                default:
                    routingKey = MQConstant.ACCOUNT_PNK_WEB_USER_QUERY;
                    break;
            }
            this.rabbitTemplate.convertAndSend(routingKey, msg);
            log.info("MsgNotificationBusiness.send done");
        } catch (Exception e) {
            log.error("send msg notification failed:", e);
        }
    }

    @Override
    public void sendNotification(String exchange,String routingKey,Object data) {
        try {
            log.info("sendNotification.send,exchange:{},routingKey:{},data:{}", exchange, routingKey,LogMaskUtils.maskJsonString(JSON.toJSONString(data)));
            this.rabbitTemplate.convertAndSend(exchange,routingKey, data);
            log.info("AccountMsgNotificationBusiness.send done");
        } catch (Exception e) {
            log.error("sendNotification send msg notification failed:", e);
        }
    }


    @Override
    public void send(AccountMsgNotification msg) {
        try {
            log.info("AccountMsgNotificationBusiness.send:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
            this.rabbitTemplate.convertAndSend(FrontPushEventType.FRONT_EXCHANGE,msg.getRoutingKey(), msg.getData());
            log.info("AccountMsgNotificationBusiness.send done");
        } catch (Exception e) {
            log.error("AccountMsgNotificationBusiness send msg notification failed:", e);
        }
    }



}
