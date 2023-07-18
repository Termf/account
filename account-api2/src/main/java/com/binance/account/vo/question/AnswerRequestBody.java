package com.binance.account.vo.question;

import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("问答模块-答案的请求")
@Setter
@Getter
public class AnswerRequestBody extends ToString {
	private static final long serialVersionUID = 7111988342296967718L;

	@ApiModelProperty("流程的ID")
	@NotNull
	private String flowId;
	@ApiModelProperty("问题id")
	@NotNull
	private Long questionId;
	@ApiModelProperty("答案")
	@NotNull
	private List<String> answers;	
	
	@ApiModelProperty("设备信息,中台封装")
	Map<String, String> deviceInfo;
}