package com.binance.account.vo.reset.response;

import java.util.Date;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResetBigDataLogResponseBody {
	private List<Body> body;
	
	@Getter
	@Setter
	@ToString
	public static class Body{
		private Long id;
		private Long userId;
		private String transId;
		private Double score; // 放大100倍存储
		private Date batchTime;
		private Date createTime;
		// ---扩展字段----
		private String resetType;
		private String email;
		private String protectedMode;
	}
}