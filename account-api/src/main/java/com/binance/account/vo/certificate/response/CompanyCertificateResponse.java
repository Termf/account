package com.binance.account.vo.certificate.response;
 
import java.util.Date;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
 
@ApiModel(description = "查询企业认证信息Response", value = "查询企业认证信息Response")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class CompanyCertificateResponse extends ToString{
 	/**
	 * 
	 */
	private static final long serialVersionUID = -928898506568698325L;
 	
	@ApiModelProperty(name = "用户ID")
	private Long userId;
 	
	@ApiModelProperty(name = "公司名称")
    private String companyName;
 	
	@ApiModelProperty(name = "公司地址")
    private String companyAddress;
 	@ApiModelProperty(name = "公司所在国家")
    private String companyCountry;
 	
 	@ApiModelProperty(name = "申请人名字")
    private String applyerName;
 	
 	@ApiModelProperty(name = "申请人邮箱")
    private String applyerEmail;
 	
 	@ApiModelProperty(name = "状态")
    private Integer status;
 	
 	@ApiModelProperty(name = "info")
    private String info;
 	
 	@ApiModelProperty(name = "创建时间")
    private Date insertTime;
 	
 	@ApiModelProperty(name = "修改时间")
    private Date updateTime;
}