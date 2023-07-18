package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;

@ApiModel("GetBatchUserTypeListRequest")
@Getter
@Setter
public class GetBatchUserTypeListRequest implements Serializable {


    private static final long serialVersionUID = 5799634290555682819L;
    @ApiModelProperty("userIdList")
    @NotNull
    private List<Long> userIdList;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
