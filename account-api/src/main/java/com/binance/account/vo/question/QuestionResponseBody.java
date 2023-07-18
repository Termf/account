package com.binance.account.vo.question;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.CollectionUtils;

import com.binance.account.vo.reset.response.ResetQuestion;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("问题与选项")
@Getter
@Setter
public class QuestionResponseBody extends ToString {
	private static final long serialVersionUID = -92081301337047762L;

	@ApiModelProperty("答题剩余秒数")
	private Long timeRemaining;
	@ApiModelProperty("问题数据")
	private List<ResetQuestion> questions = new LinkedList<>();

	@ApiModelProperty("当前是第几次答题")
	private int count;
	@ApiModelProperty("总的最大答题次数")
	private int maxCount;
	@ApiModelProperty("超时失败跳转")
	private String failPath;

	public void sortQuestions() {
		if (CollectionUtils.isEmpty(questions))
			return;
		Collections.sort(questions);
	}
}