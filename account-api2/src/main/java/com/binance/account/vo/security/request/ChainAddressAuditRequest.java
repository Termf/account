package com.binance.account.vo.security.request;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author alex
 */
@ApiModel(value="区块链地址人工审核结果")
@Getter
@Setter
public class ChainAddressAuditRequest implements Serializable {

    private static final long serialVersionUID = -8383490162332468298L;

    @ApiModelProperty("Id")
    @NotNull
    private Long id;

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("用户Email")
    private String email;

    @ApiModelProperty("审核状态")
    @NotEmpty
    private String status;

    @ApiModelProperty("类型： 0 入金 1 出金")
    private String type;

    @ApiModelProperty("审核人")
    private String auditor;

    @ApiModelProperty("币种")
    private String coin;
    
    @ApiModelProperty("资金退款区块链地址")
    private String refundAddress;

    @ApiModelProperty("资金退款区块链地址标签")
    private String refundAddressTag;

    @ApiModelProperty("备注")
    private String comment;

    @ApiModelProperty("排序字段")
    private String sort;
    @ApiModelProperty("排序")
    private String order;

    private Integer offset;

    private Integer limit;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
