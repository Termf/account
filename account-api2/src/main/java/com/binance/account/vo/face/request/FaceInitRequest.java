package com.binance.account.vo.face.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2018-12-07 17:47
 */
@ApiModel("初始化人脸识别的请求参数")
@Setter
@Getter
public class FaceInitRequest extends ToString {

    private static final long serialVersionUID = 3080647756853753538L;

    @ApiModelProperty("业务编号(对应人脸识别邮件中的id的值)")
    @NotNull
    private String transId;

    @ApiModelProperty("业务类型(对应人脸识别邮件中的type的值)")
    @NotNull
    private String type;

}
