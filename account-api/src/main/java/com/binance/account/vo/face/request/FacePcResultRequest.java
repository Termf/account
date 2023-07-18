package com.binance.account.vo.face.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2018-12-11 16:03
 */
@ApiModel("PC端人脸识别结果")
@Setter
@Getter
public class FacePcResultRequest extends ToString {
    private static final long serialVersionUID = 684022176547457903L;

    @ApiModelProperty("face++的回调数据")
    @NotNull
    private String data;

    @ApiModelProperty("face++的回调数据的数字签名")
    @NotNull
    private String sign;
}
