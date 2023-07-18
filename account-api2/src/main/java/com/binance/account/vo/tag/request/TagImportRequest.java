package com.binance.account.vo.tag.request;

import java.util.List;

import org.hibernate.validator.constraints.NotEmpty;

import com.binance.account.vo.tag.TagImportVo;
import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author lufei
 * @date 2018/6/21
 */
@ApiModel("标签用户关系导入Request")
@Getter
@Setter
public class TagImportRequest extends ToString {
    private static final long serialVersionUID = 2488421712569404149L;
    @ApiModelProperty(value = "用户", required = true)
    @NotEmpty
    private String user;
    @ApiModelProperty(value = "标签或标签组ID", required = true)
    @NotEmpty
    private String xId;
    @ApiModelProperty(value = "标签或标签组", required = true)
    @NotEmpty
    private String type;
    @ApiModelProperty(value = "导入数据", required = true)
    @NotEmpty
    private List<TagImportVo> data;

}
