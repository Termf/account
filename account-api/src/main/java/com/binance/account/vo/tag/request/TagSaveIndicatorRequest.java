package com.binance.account.vo.tag.request;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.binance.account.vo.tag.TagDetailValueVo;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/6/21
 */
@ApiModel("保存用户标签关系Request")
@Getter
@Setter
public class TagSaveIndicatorRequest extends ToString {
    private static final long serialVersionUID = 7997462233131526510L;
    @ApiModelProperty(value = "用户ID", required = true)
    @NotEmpty
    private String userId;
    @ApiModelProperty(value = "标签ID", required = true)
    private String tagId;
    @ApiModelProperty(value = "备注")
    @NotEmpty
    private String remark;
    @ApiModelProperty(value = "值")
    @NotEmpty
    private String value;
    @ApiModelProperty(value = "用户")
    @NotEmpty
    private String user;
    @ApiModelProperty(value = "详情")
    private List<TagDetailValueVo> detail;

}
