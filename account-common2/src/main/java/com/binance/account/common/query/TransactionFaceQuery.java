package com.binance.account.common.query;

import com.binance.account.common.enums.TransFaceLogStatus;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

/**
 * @author liliang1
 * @date 2018-12-21 17:43
 */
@Setter
@Getter
public class TransactionFaceQuery extends Pagination {

    private Long userId;

    private String transType;

    private String transId;

    private String email;

    private TransFaceLogStatus status;

    private Date startTime;

    private Date endTime;


    public String getTransType() {
        return transType == null || transType.trim().equals("") ? null : transType;
    }

    public String getTransId() {
        return transId == null || transId.trim().equals("") ? null : transId;
    }

    public String getEmail() {
        return email == null || email.trim().equals("") ? null : email;
    }
}
