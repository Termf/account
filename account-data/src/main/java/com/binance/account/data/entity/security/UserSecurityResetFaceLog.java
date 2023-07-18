package com.binance.account.data.entity.security;

import com.binance.master.utils.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author liliang1
 * @date 2018-08-27 15:19
 */
@Getter
@Setter
public class UserSecurityResetFaceLog implements Serializable {
    private static final long serialVersionUID = -5984141299824017766L;
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

    private String faceEnv;

    private String faceRemark;

    private String uuid;

    private String resultRequestId;

    /** 疑似合成脸分数 */
    private Double syntheticFaceConfidence;

    /** 疑似面具分数 */
    private Double maskConfidence;

    /** 疑似屏幕翻拍分数 */
    private Double screenReplayConfidence;

    /** 换脸攻击 SDK版本检测 0-否 1-是 */
    private Integer faceReplaced;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }

}