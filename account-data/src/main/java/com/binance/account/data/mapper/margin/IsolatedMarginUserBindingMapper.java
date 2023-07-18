package com.binance.account.data.mapper.margin;

import com.binance.account.data.entity.margin.IsolatedMarginUserBinding;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface IsolatedMarginUserBindingMapper {

    int deleteByIsolatedMarginUserId(Long IsolatedMarginUserId);

    int insert(IsolatedMarginUserBinding record);

    int updateByIsolatedMarginUserId(IsolatedMarginUserBinding record);

    int updateByIsolatedMarginUserIdSelective(IsolatedMarginUserBinding record);

    IsolatedMarginUserBinding selectByIsolatedMarginUserId(Long IsolatedMarginUserId);

    List<IsolatedMarginUserBinding> getIsolatedMarginUserBindingsByRootUserId(Long rootUserId);

    long countIsolatedMarginUsersByRootUserId(Long rootUserId);

    /**根据RootUserId查subUserId集合*/
    List<Long> selectisolatedMarginUserIdsByRootUserId(Long rootUserId);


    IsolatedMarginUserBinding selectisolatedMarginUserIdsByRootUserIdAndIsolatedMarginUserId(@Param("rootUserId") Long rootUserId,@Param("isolatedMarginUserId") Long isolatedMarginUserId);


}