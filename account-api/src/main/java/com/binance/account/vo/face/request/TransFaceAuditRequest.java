package com.binance.account.vo.face.request;

import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotNull;

/**
 * @author liliang1
 * @date 2019-03-18 14:22
 */
@Setter
@Getter
public class TransFaceAuditRequest extends ToString {
    private static final long serialVersionUID = 791451116034903894L;

    @ApiModelProperty("用户ID")
    @NotNull
    private Long userId;

    @ApiModelProperty("业务编号")
    @NotNull
    private String transId;

    @ApiModelProperty("人脸业务类型")
    @NotNull
    private String transType;

    @ApiModelProperty("审核状态, 只能是通过或拒绝这两种")
    @NotNull
    private TransFaceLogStatus status;

    @ApiModelProperty("审核意见")
    @Length(max = 200, message = "fail reason max length 200 char.")
    private String failReason;
}
