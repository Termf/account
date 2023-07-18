package com.binance.account.vo.user.response;

import com.binance.master.utils.StringUtils;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@ApiModel("审核区块链结果")
@AllArgsConstructor
@Getter
@Setter
@NoArgsConstructor
public class AuditChainResponse implements Serializable {

    private static final long serialVersionUID = -7058165941087359664L;
    private Long totalCount;
    private Long stopServiceCount;
    private Long exemptCount;
    private Long refuseCount;
    private Long pendingCount;
    private Long refundCount;

    @Override
    public String toString() {
        return StringUtils.objectToString(this);
    }
}
