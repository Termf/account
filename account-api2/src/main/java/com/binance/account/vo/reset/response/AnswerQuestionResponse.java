package com.binance.account.vo.reset.response;

import com.binance.account.common.enums.AnswerCompleteStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("答题结果")
@Setter
@Getter
public class AnswerQuestionResponse extends ToString {

    private static final long serialVersionUID = 6281854078943090768L;

    @ApiModelProperty("答题结果状态")
    private AnswerCompleteStatus status;

    @ApiModelProperty("当前答题的次数")
    private int count;

    @ApiModelProperty("当前答题的允许最大次数")
    private int maxCount;

    @ApiModelProperty("当成功或者失败后当跳转地址")
    private String gotoPath;

    public AnswerQuestionResponse() {
        super();
    }

    public AnswerQuestionResponse(AnswerCompleteStatus status, int count, int maxCount) {
        this.status = status;
        this.count = count;
        this.maxCount = maxCount;
    }
}
