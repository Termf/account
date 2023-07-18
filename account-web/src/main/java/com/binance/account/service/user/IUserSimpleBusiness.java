package com.binance.account.service.user;

public interface IUserSimpleBusiness {

    public void sendMgsToFrontGroup(String routingKey, String userId, String eventType,String accountType,String tfaType);
}
