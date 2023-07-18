package com.binance.account.vo.apimanage.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@ApiModel
@Getter
@Setter
public class SearchDeletedApiRequest extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = -7456891026580346802L;

    @ApiModelProperty
    private String userId;
    @ApiModelProperty
    private String email;
    @ApiModelProperty
    private String apiKey;
    @ApiModelProperty
    private String apiName;
    @ApiModelProperty
    private String ip;
    @ApiModelProperty
    private String ruleId;
    @ApiModelProperty("删除时间起始")
    private Date startTime;
    @ApiModelProperty("删除时间截至")
    private Date endTime;
    @ApiModelProperty
    private String sort;
    @ApiModelProperty
    private String order;
    @ApiModelProperty
    private Integer start;
    @ApiModelProperty
    private Integer offset;

}
