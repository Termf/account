package com.binance.account.data.mapper.apimanage;

import com.binance.account.data.entity.apimanage.ApiModel;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface ApiModelMapper {

    long insertWithId(ApiModel apiModel);

    void deleteById(@Param("id") Long id);

    List<ApiModel> selectApiList(Map<String, Object> param);

    long selectApiListCount(Map<String, Object> param);

    ApiModel selectModelByUuid(@Param("uuid") String uuid, @Param("userId")String userId);

    ApiModel selectByWithdrawVerifycode(@Param("withdrawVerifycode") String withdrawVerifycode, @Param("userId")String userId);

    List<ApiModel> getApiByMap(Map<String, Object> param);

    public void updateByPrimaryKey(ApiModel apiModel);

    void updateApikeyVerifyCode(@Param("withdrawVerifycode") String withdrawVerifycode, @Param("id") Long id);

    public void updateEmailVerifyStatus(@Param("apiEmailVerify") boolean apiEmailVerify, @Param("id") Long id,
                                        @Param("userId") String userId);

    public void updateForApiWithdraw(Map<String, Object> param);

    List<ApiModel> loadAllApikey();

    void updateApikey(ApiModel apiModel);

    List<ApiModel> loadApikeyWhichisTradeEnabled();


    List<ApiModel> selectByUserIds(List<Long> subUserIds);

    public void updateRuleIdByPrimaryKey(ApiModel apiModel);

    ApiModel selectByApiKey(@Param("apiKey") String apiKey);

}
