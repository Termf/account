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
@ApiModel(value="区块链地址人工审核请求")
@Getter
@Setter
public class ChainAddressAnalyzeRequest implements Serializable {


    private static final long serialVersionUID = 8841420241985322287L;

    public enum Direction {
        /**
         * 入金
         */
        DEPOSIT,
        /**
         * 出金
         */
        WITHDRAW
    }

    @ApiModelProperty("用户Id")
    @NotNull
    private Long userId;

    @ApiModelProperty("区块链地址")
    @NotEmpty
    private String chainAddress;

    @ApiModelProperty("入金/出金")
    @NotNull
    private Direction direction;

    @ApiModelProperty("渠道分析结果")
    @NotEmpty
    private String analyzeResult;

    @ApiModelProperty("币种")
    @NotEmpty
    private String coin;

    @ApiModelProperty("提交者")
    private String creator;

    @ApiModelProperty("关联的出/入金主键id")
    private String bizId;

    @ApiModelProperty("扫描渠道")
    private String channel;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
