package com.binance.account.common.query;

import java.io.Serializable;
import java.util.Date;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuestionQuery implements Serializable {
	private static final long serialVersionUID = -2256538156470239808L;
	private Long userId;
    private String flowId;
    private String answerId;
    private String flowType;
    private String questionId;
	private Date startTime;
	private Date endTime;
	
	private Boolean groupByFlowId;
	private Integer offset;
	private Integer limit;
}
