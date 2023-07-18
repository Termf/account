package com.binance.account.vo.user.response;

import com.google.common.collect.Lists;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * Created by yangyang on 2019/10/24.
 */
@Data
public class SnapshotShareConfigsRes implements Serializable {

    private List<SnapshotShareConfigRes> resList = Lists.newArrayList();

    private String agentCode;

    private String uploadUrlUniqueKey;
}
