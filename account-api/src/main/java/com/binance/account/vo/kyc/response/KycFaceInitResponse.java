package com.binance.account.vo.kyc.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("人脸识别流程信息")
@Setter
@Getter
public class KycFaceInitResponse extends KycFlowResponse {

    /**
	 * 
	 */
	private static final long serialVersionUID = -3858401194399831981L;

	@ApiModelProperty("人脸识别流程标识")
    private String transId;

    @ApiModelProperty("人脸识别类型")
    private String transType;
    
    @ApiModelProperty("face开关")
    private boolean kycFaceSwitch; 

}
