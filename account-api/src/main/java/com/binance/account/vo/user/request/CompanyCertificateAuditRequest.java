package com.binance.account.vo.user.request;

import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

@ApiModel(description = "企业认证审核", value = "企业认证审核")
@Getter
@Setter
public class CompanyCertificateAuditRequest extends ToString {

    @ApiModelProperty(required = true, notes = "id")
    @NotNull
    private Long id;

    @ApiModelProperty(required = true, notes = "userId")
    @NotNull
    private Long userId;

    @ApiModelProperty(required = true, notes = "审核状态")
    @NotNull
    private CompanyCertificateStatus status;

    @ApiModelProperty(required = false, notes = "失败原因")
    @Length(max = 1000, message = "refused message too long")
    private String info;

    @ApiModelProperty(required = false, notes = "公司全称")
    private String companyName;

    @ApiModelProperty(required = false, notes = "公司注册信息")
    private String companyAddress;

    @ApiModelProperty(required = false, notes = "法人姓名")
    private String applyerName;

    @ApiModelProperty(required = false, notes = "申请人邮箱")
    private String applyerEmail;

    @ApiModelProperty(required = false, notes = "证件号码")
    private String number;

    @ApiModelProperty(required = false, notes = "是否重做Jumio")
    private Integer redoJumio;

}
