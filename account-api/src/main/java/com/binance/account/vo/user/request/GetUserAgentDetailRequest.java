package com.binance.account.vo.user.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@ApiModel("获取推荐人信息Request")
@Data
public class GetUserAgentDetailRequest implements Serializable {
    private static final long serialVersionUID = -1462456725948788724L;
    @ApiModelProperty(name = "agentId", required = true)
    @NotNull
    private Long agentId;

    @ApiModelProperty("排序字段")
    private String sort;

    @ApiModelProperty("排序")
    private String order;

    @ApiModelProperty("开始行")
    private Integer offset;

    @ApiModelProperty("一页要几条数据")
    private Integer rows;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
