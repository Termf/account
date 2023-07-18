package com.binance.account.vo.reset.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetUserReleaseBody {
	private boolean success;
	private Long UserId;
}
