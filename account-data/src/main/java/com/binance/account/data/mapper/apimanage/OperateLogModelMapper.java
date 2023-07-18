package com.binance.account.data.mapper.apimanage;

import com.binance.account.data.entity.apimanage.OperateLogModel;
import com.binance.master.old.config.OldDB;

@OldDB
public interface OperateLogModelMapper {

    void insert(OperateLogModel model);

}
