package com.binance.account.vo.apimanage.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

@ApiModel
@Getter
@Setter
public class SearchApiRequest extends ToString {

    /**
     * 
     */
    private static final long serialVersionUID = -7456891026580346802L;

    @ApiModelProperty
    private String userId;
    @ApiModelProperty
    private List<String> userIds;
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
    @ApiModelProperty
    private Date startTime;
    @ApiModelProperty
    private Date endTime;
    @ApiModelProperty
    private String sort;
    @ApiModelProperty
    private String order;
    @ApiModelProperty
    private Integer start;
    @ApiModelProperty
    private Integer offset;
    @ApiModelProperty("是否前端调用")
    private boolean frontEnd=false;
    @ApiModelProperty("是否包含未认证状态 如果false，则(status!=1 or apiEmailVerify)")
    private boolean includeUnconfirmed=true;
    @ApiModelProperty("是否增加默认当天时间区间限制")
    private boolean defaultTimeLimit=true;

}
