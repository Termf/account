package com.binance.account.service.apimanage;

public interface IOperateLogService {
	
	public void insert(String userId, String type, String result, String operation, String info);

}
