package com.binance.account.vo.reset.response;

import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("重置2FA问题配置接口返回类")
@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResetQuestionConfigBody {
	private boolean success;
}
