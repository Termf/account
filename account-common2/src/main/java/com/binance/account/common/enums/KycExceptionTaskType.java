package com.binance.account.common.enums;

public enum KycExceptionTaskType {

	JUMIO_INIT_FACE("JUMIO_INIT_FACE", "JUMIO成功后初始化face流程异常", 30 * 60);

	private String code;

	private String message;
	//延期时间（秒）
	private int delay;

	private KycExceptionTaskType(String code, String message, int delay) {
		this.code = code;
		this.message = message;
		this.delay = delay;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public int getDelay() {
		return delay;
	}

	public void setDelay(int delay) {
		this.delay = delay;
	}

}
