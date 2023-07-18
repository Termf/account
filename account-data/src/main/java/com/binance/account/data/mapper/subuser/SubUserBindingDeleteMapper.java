package com.binance.account.data.mapper.subuser;

import com.binance.account.data.entity.subuser.SubUserBindingDelete;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface SubUserBindingDeleteMapper {
    int deleteByPrimaryKey(Long subUserId);

    int insert(SubUserBindingDelete record);

    int insertSelective(SubUserBindingDelete record);

    SubUserBindingDelete selectByPrimaryKey(Long subUserId);

    int updateByPrimaryKeySelective(SubUserBindingDelete record);

    int updateByPrimaryKey(SubUserBindingDelete record);
}