package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * @author mikiya.chen
 * @date 2020/3/4 10:08 上午
 */
@ApiModel("Channel用户 World Check人工审核")
@Getter
@Setter
public class WckChannelAuditRequest  extends ToString {

    private static final long serialVersionUID = -6461531779888220587L;

    @ApiModelProperty("caseId")
    @NotNull
    private String caseId;

    @ApiModelProperty("第几次审核 1.一审 2.二审 以此类推")
    @NotNull
    @Max(3)
    @Min(1)
    private Integer auditorSeq;

    @ApiModelProperty("审核人id")
    @NotNull
    private Long auditorId;

    @ApiModelProperty("审核状态 true.通过 false.不通过")
    @NotNull
    private Boolean isValid;

    @ApiModelProperty("审核评价")
    @Length(max = 2048)
    private String memo;

    @ApiModelProperty("是否pep")
    @NotNull
    private Long isPep;

    @ApiModelProperty("SanctionsHits选项值")
    @NotNull
    private Long SanctionsHits;

}
