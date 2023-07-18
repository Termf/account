package com.binance.account.vo.security.response;

import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

/**
 * @author liliang1
 * @date 2018-08-29 16:08
 */
@ApiModel("Face++操作日志信息")
@Getter
@Setter
@NoArgsConstructor
public class SecurityResetFaceLogResponse extends ToString {
    private static final long serialVersionUID = -1859367294200790207L;

    private Long id;

    private Long userId;

    private String resetId;

    private Integer resetType;

    private Date createTime;

    private Date updateTime;

    private String clientIp;

    private String bizNo;

    private String bizId;

    private String faceStatus;

    private String faceConfidence;

    private String faceAction1;

    private String faceAction2;

    private String faceAction3;

    private String faceBest;

    private String faceRemark;

    private String uuid;

    private String resultRequestId;

    /** 疑似合成脸分数 */
    private Double syntheticFaceConfidence;

    /** 疑似面具分数 */
    private Double maskConfidence;

    /** 疑似屏幕翻拍分数 */
    private Double screenReplayConfidence;

}
