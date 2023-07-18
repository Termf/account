package com.binance.account.data.mapper.apimanage;

import com.binance.account.data.entity.apimanage.ApiDeletedModel;
import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface ApiDeletedModelMapper {

    public void insert(ApiDeletedModel apiDeletedModel);

    public List<ApiDeletedModel> loadByParams(Map<String, Object> params);

    public long countByParams(Map<String, Object> params);

    List<ApiDeletedModel> loadAllApikey();

    void updateApikey(ApiDeletedModel apiModel);
}
