package com.binance.account.vo.datamigration.request;

import com.binance.account.vo.certificate.CompanyCertificateVo;
import com.binance.account.vo.certificate.UserCertificateVo;
import com.binance.account.vo.security.UserSecurityLogVo;
import com.binance.account.vo.security.UserSecurityVo;
import com.binance.account.vo.user.UserInfoVo;
import com.binance.account.vo.user.UserIpVo;
import com.binance.account.vo.user.UserVo;
import com.binance.master.commons.ToString;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;

@ApiModel(description = "数据迁移Request", value = "数据迁移Request")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class DataMigrationRequest extends ToString {

    /**
     *
     */
    private static final long serialVersionUID = -1911707798978019116L;

    @NotNull
    private UserVo user;

    @NotNull
    private UserInfoVo userInfo;

    private List<UserIpVo> userIps;

    @NotNull
    private UserSecurityVo userSecurity;

    private List<UserSecurityLogVo> userSecurityLogs;

    private UserCertificateVo userCertificate;

    private CompanyCertificateVo companyCertificate;

}
