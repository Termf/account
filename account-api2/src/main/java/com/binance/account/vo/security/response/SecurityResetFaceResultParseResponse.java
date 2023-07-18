package com.binance.account.vo.security.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author liliang1
 * @date 2018-08-30 10:10
 */
@ApiModel("Face++验证结果解析结果")
@Getter
@Setter
@NoArgsConstructor
public class SecurityResetFaceResultParseResponse implements Serializable {

    private static final long serialVersionUID = 7292230541349873357L;

    @ApiModelProperty("解析是否通过")
    private Boolean success;

    @ApiModelProperty("解析结描述")
    private String message;

    @ApiModelProperty("记录ID")
    private String resetId;

    @ApiModelProperty("重置记录的Type")
    private String type;

    @ApiModelProperty("是否有回答问题环节")
    private String haveQuestion;


}
