package com.binance.account.common.query;

import com.binance.account.common.enums.KycStatus;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Jumio 模块化查询模板
 * @author liliang1
 * @date 2018-11-14 16:54
 */
@Setter
public class UserKycModularQuery extends Pagination {

    private static final long serialVersionUID = -4258659746545008784L;

    private Long userId;
    private String email;
    private String kycStatus;
    private String checkStatus;
    private String fillCountry;
    private String scanReference;
    private String firstName;
    private String lastName;
    private Date startCreateTime;
    private Date endCreateTime;
    private String faceStatus;
    private String faceOcrStatus;

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return StringUtils.isEmpty(email) ? null : email;
    }

    public String getKycStatus() {
        return kycStatus;
    }

    public List<Integer> getStatusList() {
        if (StringUtils.isEmpty(kycStatus)) {
            return null;
        }
        KycStatus status = KycStatus.getByNmae(kycStatus);
        if (status != null) {
            return Arrays.asList(status.ordinal());
        }else {
            //获取不到的化，全部设置为审核中的状态
            return Arrays.asList(
                    KycStatus.jumioPassed.ordinal(),
                    KycStatus.jumioRefused.ordinal(),
                    KycStatus.wckWaiting.ordinal(),
                    KycStatus.wckPassed.ordinal(),
                    KycStatus.wckRefused.ordinal()
                    );
        }
    }

    public String getCheckStatus() {
        return StringUtils.isEmpty(checkStatus) ? null : checkStatus;
    }

    public String getFillCountry() {
        return StringUtils.isEmpty(fillCountry) ? null : fillCountry;
    }

    public String getScanReference() {
        return StringUtils.isEmpty(scanReference) ? null : scanReference;
    }

    public String getFirstName() {
        return StringUtils.isEmpty(firstName) ? null : firstName;
    }

    public String getLastName() {
        return StringUtils.isEmpty(lastName) ? null : lastName;
    }

    public Date getStartCreateTime() {
        return startCreateTime;
    }

    public Date getEndCreateTime() {
        return endCreateTime;
    }

    public String getFaceStatus() {
        return StringUtils.isEmpty(faceStatus) ? null : faceStatus;
    }
    
    public String getFaceOcrStatus() {
    	return StringUtils.isEmpty(faceOcrStatus) ? null : faceOcrStatus;
    }
}
