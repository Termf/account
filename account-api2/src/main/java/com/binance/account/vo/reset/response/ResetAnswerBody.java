package com.binance.account.vo.reset.response;

import com.binance.account.common.enums.AnswerCompleteStatus;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("2FA答题结果返回信息实体")
@Setter
@Getter
public class ResetAnswerBody extends ToString {
	private static final long serialVersionUID = 4494070917915800792L;

	@ApiModelProperty("用户答题状态:OK,当前答题完成,请继续;Fail,回答完毕,但是不正确;	TimeOut,答题超时;Succes,答题成功")
	private AnswerCompleteStatus answerComplete;
}
