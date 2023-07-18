package com.binance.account.vo.kyc.request;

import javax.validation.constraints.NotNull;

import com.binance.master.commons.ToString;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

@ApiModel("face_ocr修改姓名请求")
@Getter
@Setter
public class UpdateFaceOcrNameRequest extends ToString{

	/**
	 * 
	 */
	private static final long serialVersionUID = 553221021882107727L;
	
	@ApiModelProperty("userId")
	@NotNull
	private Long userId;
	
	@ApiModelProperty("姓名")
	@NotNull
	private String name;

}
