package com.binance.account.vo.certificate;

import com.binance.account.common.constant.WckConst;
import com.binance.account.common.enums.WckStatus;
import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.Map;

import javax.validation.constraints.NotNull;

import lombok.Data;

import java.util.Date;
import java.util.List;
import lombok.NoArgsConstructor;

/**
 * Created by Shining.Cai on 2018/09/10.
 **/
@ApiModel("world-check 审核记录")
@Data
@NoArgsConstructor
public class UserWckAuditVo {


    private Long kycId;

    private Long userId;

    private String email;

    private String userName;

    /** jumio解析出的名称 */
    private String userNameFromJumio;

    private String birthDate;

    private String nationality;

    private String countryLocation;

    private String cardType;

    private String cardNumber;

    private String caseSystemId;

    private WckStatus status;

    private List<UserWckAuditLogVo> auditLogs;

    private Date createTime;

    private Date updateTime;

    @Data
    public static class UserWckAuditLogVo{
        private Long id;

        private Long kycId;

        private Integer auditorSeq;

        private Long auditorId;

        private Boolean isValid;

        private String memo;
        
        private Long isPep;
        
        private Long isAdverse;

        private Date createTime;
    }

    public UserWckAuditVo(Map<String, Object> rawData){
        this.setKycId((Long) rawData.get("kyc_id"));
        this.setUserId((Long) rawData.get("user_id"));
        this.setCaseSystemId((String) rawData.get("case_system_id"));
        this.setStatus(WckStatus.of((Integer) rawData.get("status")));
        this.setCreateTime((Date) rawData.get("create_time"));
        this.setUpdateTime((Date) rawData.get("update_time"));
        this.setNationality((String) rawData.get("issuing_country"));
        String rawLogsStr = (String) rawData.get("raw_logs");
        if (StringUtils.isNotBlank(rawLogsStr)){
            String[] rawLogs = rawLogsStr.split(WckConst.SEPARATOR_ROW);
            List<UserWckAuditVo.UserWckAuditLogVo> logVos = new ArrayList<>();
            for (String rawLog:rawLogs){
                String[] cols = rawLog.split(WckConst.SEPARATOR_COL);
                UserWckAuditLogVo logVo = new UserWckAuditLogVo();
                logVo.setAuditorSeq(Integer.valueOf(cols[0]));
                logVo.setAuditorId(Long.valueOf(cols[1]));
                logVo.setIsValid("1".equals(cols[2]));
                logVo.setMemo(cols[3]);
                logVo.setIsPep(StringUtils.isNotBlank(cols[4]) ? Long.valueOf(cols[4]) : null);
                logVo.setIsAdverse(StringUtils.isNotBlank(cols[5]) ? Long.valueOf(cols[5]) : null);
                logVos.add(logVo);
            }
            this.setAuditLogs(logVos);
        }

    }
}
