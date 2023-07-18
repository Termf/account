package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@ApiModel("GetBatchUserListResponse")
@Getter
@Setter
public class GetBatchUserListResponse implements Serializable {

    private static final long serialVersionUID = -4632444233103444485L;
    @ApiModelProperty(name = "userid")
    private Long total;

    @ApiModelProperty(name = "getUserResponseList")
    private List<GetUserResponse> getUserResponseList;
    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
