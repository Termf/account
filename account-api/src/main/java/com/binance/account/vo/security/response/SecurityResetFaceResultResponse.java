package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;


/**
 * @author liliang1
 * @date 2018-08-27 17:29
 */
@ApiModel("重置2FA 获取和同步Face++照片")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SecurityResetFaceResultResponse extends ToString {

    private static final long serialVersionUID = 7541014803842507724L;

    @ApiModelProperty("解析是否通过")
    private Boolean success;

    @ApiModelProperty("解析结描述")
    private String message;

    @ApiModelProperty("重置记录的Type")
    private String type;

    @ApiModelProperty("是否有回答问题环节")
    private String haveQuestion;

    private String resetId;

    private String resetStatus;

    private String faceStatus;

    /** face 当前业务的编号 */
    private String faceBizNo;

    /** face 查询faceID验证结果的标识 */
    private String faceBizId;

    /** face 相似度的值 */
    private String faceConfidence;

    /** face 备注信息 */
    private String faceRemark;

}
