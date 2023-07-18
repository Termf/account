package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import java.io.Serializable;

@ApiModel("模糊匹配用户info信息Request")
@Data
public class FuzzyMatchUserInfoRequest implements Serializable {

    private static final long serialVersionUID = -2325745609789914481L;
    @ApiModelProperty("remark")
    @NotEmpty
    private String remark;

    @ApiModelProperty("开始行")
    private Integer offset;

    @ApiModelProperty("一页要几条数据(不能超过500)")
    @Max(500)
    private Integer rows;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
