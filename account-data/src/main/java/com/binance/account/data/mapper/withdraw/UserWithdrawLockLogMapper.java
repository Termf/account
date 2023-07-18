package com.binance.account.data.mapper.withdraw;

import com.binance.account.data.entity.withdraw.UserWithdrawLockLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@DefaultDB
public interface UserWithdrawLockLogMapper {
    int insert(UserWithdrawLockLog record);

    int insertSelective(UserWithdrawLockLog record);
    
    UserWithdrawLockLog selectByUniqueKey(@Param("tranId") Long tranId,@Param("type") String type,@Param("userId") Long userId);

    BigDecimal sumLockWithOperator(@Param("userId") Long userId, @Param("operator") String operator);
}