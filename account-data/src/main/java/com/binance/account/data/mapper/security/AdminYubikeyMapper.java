package com.binance.account.data.mapper.security;

import com.binance.account.common.query.WebAuthnAdminQuery;
import com.binance.account.data.entity.security.UserYubikey;
import com.binance.master.annotations.DefaultDB;

import java.util.List;

@DefaultDB
public interface AdminYubikeyMapper extends YubikeyMapper {
    List<UserYubikey> getAll();

    long adminGetPageListCount(WebAuthnAdminQuery query);

    List<UserYubikey> adminGetPageList(WebAuthnAdminQuery query);

    // 公共方法在父类

}
