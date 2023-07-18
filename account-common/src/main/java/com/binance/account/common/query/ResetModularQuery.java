package com.binance.account.common.query;

import com.binance.account.common.enums.UserSecurityResetStatus;
import com.binance.account.common.enums.UserSecurityResetType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author liliang1
 * @date 2019-01-28 12:02
 */
@Setter
@Getter
public class ResetModularQuery extends Pagination {

    private static final long serialVersionUID = 6324553968375762753L;

    private Long userId;
    private String email;
    private String status;
    private UserSecurityResetType type;
    private String jumioStatus;
    private String idNumber;
    private String faceStatus;
    private Integer certificateType;
    private Date startCreateTime;
    private Date endCreateTime;

    public List<Integer> getStatusList() {
        if (StringUtils.isEmpty(status)) {
            return null;
        }
        UserSecurityResetStatus resetStatus = UserSecurityResetStatus.getByName(status);
        if (resetStatus == null) {
            return Arrays.asList(
                    UserSecurityResetStatus.jumioPassed.ordinal(),
                    UserSecurityResetStatus.jumioRefused.ordinal(),
                    UserSecurityResetStatus.facePending.ordinal(),
                    UserSecurityResetStatus.jumioPending.ordinal(),
                    UserSecurityResetStatus.FIE.ordinal(),
                    UserSecurityResetStatus.JRFR.ordinal(),
                    UserSecurityResetStatus.JRFP.ordinal(),
                    UserSecurityResetStatus.JPFR.ordinal(),
                    UserSecurityResetStatus.JPFP.ordinal()
            );
        }else {
            return Arrays.asList(resetStatus.ordinal());
        }
    }

}
