package com.binance.account.vo.user.response;

import java.io.Serializable;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * Created by yangyang on 2019/10/18.
 */
@Data
public class SnapshotShareConfigRes implements Serializable {

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

    /**
     * 1app 2agentCode
     */
    @ApiModelProperty(required = true, notes = "type")
    private Integer type;

    @ApiModelProperty(required = true, notes = "agentCode")
    private String agentCode;
}
