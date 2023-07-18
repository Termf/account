package com.binance.account.vo.yubikey;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class UserYubikeyVo implements Serializable {

    private static final long serialVersionUID = 8134279039195796994L;

    private Long id;

    private Long userId;

    private String origin;

    private String nickName;

    private String credentialId;

    private Long signatureCount;

    private Date createTime;

    private Date updateTime;

    private String email;

    private Boolean isLegacy;
}
