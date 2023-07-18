package com.binance.account.vo.kyc.response;


import com.binance.account.common.enums.KycFillInfoGender;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class FaceOcrSubmitResponse extends KycFlowResponse {

    /**
	 * 
	 */
	private static final long serialVersionUID = -7254659656697402979L;

	@ApiModelProperty("ocr 状态")
    private String status;

    @ApiModelProperty("提示语信息")
    private String message;
    
    @ApiModelProperty("姓名")
	private String name;

    @ApiModelProperty("证件号")
	private String idcardNumber;

    @ApiModelProperty("生日")
	private String birthday;

    @ApiModelProperty("性别")
	private KycFillInfoGender gender;
    
    @ApiModelProperty("地址")
	private String address;
    
    @ApiModelProperty("face流程id")
    private String transId;
    
    @ApiModelProperty("face流程类型")
    private String faceTransType;
    
    @ApiModelProperty("kyc流程")
    private String flowDefine;
}
