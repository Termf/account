package com.binance.account.service.datamigration;

import com.binance.account.domain.bo.AccountMsgNotification;
import com.binance.account.domain.bo.MsgNotification;

public interface IMsgNotification {

    void send(MsgNotification msg);

    void sendNotification(String exchange,String routingKey,Object data);

    void send(AccountMsgNotification msg);

}
