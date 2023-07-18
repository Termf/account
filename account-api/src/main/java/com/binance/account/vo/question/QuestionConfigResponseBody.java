package com.binance.account.vo.question;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("答题配置信息")
@Setter
@Getter
public class QuestionConfigResponseBody extends ToString {

	private static final long serialVersionUID = 8996858328309592543L;

	@ApiModelProperty("当前第几次答题")
    private int count;

    @ApiModelProperty("总答题次数")
    private int maxCount;

    @ApiModelProperty("业务流程超时时间，mins")
    private long timeout;
    
    @ApiModelProperty("成功跳转路径")
    private String successPath;
    
    @ApiModelProperty("失败跳转路径")
    private String failPath;
}
