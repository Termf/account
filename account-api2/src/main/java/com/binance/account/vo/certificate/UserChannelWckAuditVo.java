package com.binance.account.vo.certificate;

import com.binance.account.common.constant.WckConst;
import com.binance.account.common.enums.WckChannelStatus;
import com.binance.account.common.enums.WckStatus;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author mikiya.chen
 * @date 2020/3/3 4:59 下午
 */
@ApiModel("world-check 审核记录")
@Data
@NoArgsConstructor
public class UserChannelWckAuditVo {

    private String caseId;

    private Long userId;

    private String email;

    private String checkName;

    private String birthDate;

    private String nationality;

    private String caseSystemId;

    private String origin;

    private WckChannelStatus status;

    private List<UserChannelWckAuditVo.UserChannelWckAuditLogVo> auditLogs;

    private Date createTime;

    private Date updateTime;

    @Data
    public static class UserChannelWckAuditLogVo{
        private Long id;

        private String caseId;

        private Integer auditorSeq;

        private Long auditorId;

        private Boolean isValid;

        private String memo;

        private Long isPep;

        private Long sanctionsHits;

        private Date createTime;

        private String failReason;
    }

    public UserChannelWckAuditVo(Map<String, Object> rawData){
        this.setCaseId((String) rawData.get("case_id"));
        this.setUserId((Long) rawData.get("user_id"));
        this.setCaseSystemId((String) rawData.get("case_system_id"));
        this.setStatus(WckChannelStatus.of((Integer) rawData.get("status")));
        this.setCreateTime((Date) rawData.get("create_time"));
        this.setUpdateTime((Date) rawData.get("update_time"));
        this.setCheckName((String) rawData.get("check_name"));
        this.setBirthDate((String) rawData.get("birth_date"));
        this.setNationality((String) rawData.get("issuing_country"));
        this.setOrigin((String) rawData.get("origin"));
    }

}
