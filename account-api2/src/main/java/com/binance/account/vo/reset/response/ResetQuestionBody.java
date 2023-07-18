package com.binance.account.vo.reset.response;

import com.alibaba.fastjson.JSON;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

@ApiModel("重置2FA查询问题返回值")
@Getter
@Setter
public class ResetQuestionBody implements Serializable {
	private static final long serialVersionUID = -92081301337047762L;

	@ApiModelProperty("答题剩余秒数")
	private Long timeRemaining;
	@ApiModelProperty("问题数据")
	private List<ResetQuestion> questions = new LinkedList<>();

	@ApiModelProperty("当前是第几次答题")
	private int count;
	@ApiModelProperty("总的最大答题次数")
	private int maxCount;

	public void sortQuestions() {
		if (CollectionUtils.isEmpty(questions))
			return;
		Collections.sort(questions);
	}

	@Override
	public String toString() {
		return JSON.toJSONString(this);
	}
}
