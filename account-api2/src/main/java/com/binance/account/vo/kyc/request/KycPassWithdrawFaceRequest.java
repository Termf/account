package com.binance.account.vo.kyc.request;

import com.binance.master.commons.ToString;
import lombok.Data;

import java.util.Date;

@Data
public class KycPassWithdrawFaceRequest extends ToString {

    private static final long serialVersionUID = -6976001023547167006L;

    private Long userId;
    private String transId;
    private String transType;
    private String refTransId;
    private String kycStatus;
    private Date kycPassTime;

}
