package com.binance.account.data.entity.certificate;

import com.binance.account.common.enums.WckChannelStatus;
import lombok.Data;

import java.util.Date;

/**
 *  world-check 渠道用户审核进度
 * @author mikiya.chen
 * @date 2020/3/3 4:11 下午
 */
@Data
public class UserChannelWckAudit {

    private Long userId;

    private String caseId;

    private String caseSystemId;

    private String origin;

    private String checkName;

    private String birthDate;

    private String issuingCountry;

    private WckChannelStatus status;

    private Long firstAuditorId;

    private Long secondAuditorId;

    private Date createTime;

    private Date updateTime;

    private Boolean isDel;

}
