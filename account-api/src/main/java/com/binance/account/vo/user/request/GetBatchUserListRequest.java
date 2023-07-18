package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

@ApiModel("GetBatchUserListRequest")
@Getter
@Setter
public class GetBatchUserListRequest implements Serializable {


    private static final long serialVersionUID = 5799634290555682819L;
    @ApiModelProperty("userIdList")
    private List<Long> userIdList;

    @ApiModelProperty(value = "开始时间", required = false)
    private Date startTime;

    @ApiModelProperty(value = "截至时间", required = false)
    private Date endTime;

    @ApiModelProperty(value = "sort", required = false)
    private String sort;

    @ApiModelProperty(value = "order, 传asc或者desc", required = false)
    private String order;

    @ApiModelProperty(value = "页码", required = false)
    private Integer page;

    @ApiModelProperty(value = "每页几行", required = false)
    private Integer rows;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
