package com.binance.account.vo.kyc.request;

import javax.validation.constraints.NotNull;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * 
 * @author liufeng
 *
 */
@ApiModel("faceOcr审核请求")
@Getter
@Setter
public class FaceOcrAuthRequest extends KycFlowRequest{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8786620091998822943L;
	
	/**
	 * id card status
	 */
	@ApiModelProperty("status")
	@NotNull
	private String status;

	@ApiModelProperty("message")
    private String message;
    
	@ApiModelProperty("faceCheck")
    private String faceCheck;

	@ApiModelProperty("face")
    private String face;
    
	@ApiModelProperty("count")
    private int count;

	@ApiModelProperty("name")
    private String name;
    
	@ApiModelProperty("idcardNumber")
    private String idcardNumber;
    
	@ApiModelProperty("birthday")
    private String birthday;

}
