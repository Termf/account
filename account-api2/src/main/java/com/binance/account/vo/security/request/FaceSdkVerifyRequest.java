package com.binance.account.vo.security.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-10-10 17:13
 */
@Setter
@Getter
public class FaceSdkVerifyRequest implements Serializable {

    private static final long serialVersionUID = 8025182168807666644L;

    @ApiModelProperty("业务标识(二维码的值)")
    @NotNull
    private String transId;

    /** SDK 验证检验码 */
    @ApiModelProperty("SDK验证检验码")
    @NotNull
    private String delta;

    @ApiModelProperty("最佳照片Base64串")
    @NotNull
    private String imageBest;

    @ApiModelProperty("背景照片Base64串")
    @NotNull
    private String imageEnv;

    @ApiModelProperty("动作1照片Base64串")
    private String imageAction1;

    @ApiModelProperty("动作2照片Base64串")
    private String imageAction2;

    @ApiModelProperty("动作3照片Base64串")
    private String imageAction3;

}
