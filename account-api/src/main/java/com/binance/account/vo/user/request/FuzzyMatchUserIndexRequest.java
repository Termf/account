package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import java.io.Serializable;

@ApiModel("模糊匹配用户index信息Request")
@Data
public class FuzzyMatchUserIndexRequest implements Serializable {

    private static final long serialVersionUID = -5931038408918894777L;
    @ApiModelProperty("email")
    @NotEmpty
    private String email;

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
