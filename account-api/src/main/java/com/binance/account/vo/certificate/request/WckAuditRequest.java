package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

@ApiModel("World Check人工审核")
@Getter
@Setter
public class WckAuditRequest extends ToString {

	private static final long serialVersionUID = -6461531779888220587L;

    @ApiModelProperty("id")
    @NotNull
    private Long kycId;

    @ApiModelProperty("第几次审核 1.一审 2.二审 3.三审 以此类推")
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

    @ApiModelProperty("强制终审")
    private Boolean forceFinal = false;
    
    @ApiModelProperty("是否pep")
    @NotNull
    private Long isPep;
    
    @ApiModelProperty("是否adverse")
    @NotNull
    private Long isAdverse;
}
