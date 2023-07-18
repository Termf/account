package com.binance.account.vo.face.request;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-02-15 13:22
 */
@Setter
@Getter
public class FaceEmailRequest extends ToString {
    private static final long serialVersionUID = 3007907398292296690L;

    @ApiModelProperty("邮箱")
    @NotNull
    private String email;

    @ApiModelProperty("业务类型(对应人脸识别邮件中的type的值)")
    @NotNull
    private String type;

}
