package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.UserAddressQuery;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author alex
 */
@DefaultDB
public interface UserAddressMapper {

    int insert(UserAddress record);

    List<UserAddress> getList(UserAddressQuery userAddressQuery);

    int getListCount(UserAddressQuery userAddressQuery);

    UserAddress getById(@Param("userId")Long userId, @Param("id")Long id);
    /**
     * 更新状态记录
     *
     * @return
     */
    int updateStatus(UserAddress userAddress);

    /**
     * 取消已通过和待审核的状态，除了id
     * @param userId
     * @param id
     * @return
     */
    int cancelPendingAndPassedExcept(@Param("userId")Long userId, @Param("id")Long id);

    /**
     * 获得最后一条状态为status的地址
     * @param userId
     * @param status
     * @return
     */
    UserAddress getLast(@Param("userId")Long userId, @Param("status")Integer status);
}