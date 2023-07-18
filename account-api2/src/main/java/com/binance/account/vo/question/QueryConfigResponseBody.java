package com.binance.account.vo.question;

import java.util.LinkedList;
import java.util.List;

import com.binance.account.common.enums.QuestionSceneEnum;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@ApiModel("问答模块-问题配置查询响应体")
@Setter
@Getter
public class QueryConfigResponseBody extends ToString {

	private static final long serialVersionUID = 2356082054816772686L;
	private List<Body> body = new LinkedList<>();
	
	@Builder
	@Setter
	@Getter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class Body {
		@ApiModelProperty("场景")
		private QuestionSceneEnum scene;
		@ApiModelProperty("哪套题")
		private String group;
		@ApiModelProperty("匹配规则")
		private String rules;
		@ApiModelProperty("问题id")
		private List<Question> questions;
	}
}
