package com.binance.account.vo.user.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotBlank;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Created by yangyang on 2019/10/18.
 */
@Data
public class SnapshotShareConfigReq implements Serializable {

    @ApiModelProperty(required = false, notes = "id")
    private Integer id;

    @ApiModelProperty(required = true, notes = "图片")
    private String icon;

    @ApiModelProperty(required = true, notes = "语言")
    private String language;

    @ApiModelProperty(required = true, notes = "标题")
    private String title;

    @ApiModelProperty(required = true, notes = "内容")
    private String content;

    @ApiModelProperty(required = true, notes = "url")
    private String url;

    @ApiModelProperty(required = true, notes = "type")
    private Integer type;

    @ApiModelProperty(required = true, notes = "type")
    private Long userId;
}
