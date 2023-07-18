package com.binance.account.vo.certificate.request;

import com.binance.master.commons.ToString;
import com.binance.master.utils.StringUtils;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel(description = "保存用户身份认证信息Request", value = "保存用户身份认证信息Request")
@Getter
@Setter
public class SaveUserCertificateRequest extends ToString {
    /**
     *
     */
    private static final long serialVersionUID = 1115664723545924326L;
    
    //审核id
    private Long id;

    @ApiModelProperty(required = true, notes = "用户id")
    private Long userId;
    @ApiModelProperty(required = true, notes = "证件正面")
    private String front;
    @ApiModelProperty(required = true, notes = "证件反面")
    private String back;
    @ApiModelProperty(required = true, notes = "手持证件")
    private String hand;
    @ApiModelProperty(required = true, notes = "firstName")
    private String firstName;
    @ApiModelProperty(required = true, notes = "lastName")
    private String lastName;
    @ApiModelProperty(required = true, notes = "消息")
    private String message;
    @ApiModelProperty(required = true, notes = "最后审核人")
    private String lastAuditor;
    @ApiModelProperty(required = true, notes = "状态(0-审核中, 1-通过, 2-拒绝)")
    private Byte status;
    @ApiModelProperty(required = true, notes = "证件号码")
    private String number;
    @ApiModelProperty(required = true, notes = "类型")
    private Integer type;
    @ApiModelProperty(required = true, notes = "性别")
    private Integer sex;
    @ApiModelProperty(required = false, notes = "国家")
    private String country;
    @ApiModelProperty(required = true, notes = "版本号")
    private Integer version;
    
	public void setNumber(String number) {//证件号码转大写
		this.number = StringUtils.upperCase(number);
	}
    
    
}
