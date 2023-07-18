package com.binance.account.integration.tg;

import com.binance.infra.telegram.alarm.TeleGramApi;
import com.binance.infra.telegram.alarm.vo.TgTextMessage;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Log4j2
@Service
public class TelegramClient {

    @Autowired
    private TeleGramApi teleGramApi;

    public void sendTg(String chatId, String text, String roomId){
        APIRequest<TgTextMessage> textMessage = new APIRequest<>();
        TgTextMessage tgTextMessage = new TgTextMessage();
        tgTextMessage.setText(text);
        tgTextMessage.setChatId(chatId);
        tgTextMessage.setRoomId(roomId);
        tgTextMessage.setAppName("account");
        textMessage.setBody(tgTextMessage);
        APIResponse<Boolean> response = teleGramApi.sendText(textMessage);
        log.info("TelegramClient.sendTg.chatId:{},roomId:{},text:{},result:{}",chatId,roomId,text,JsonUtils.toJsonHasNullKey(response));
    }
}
