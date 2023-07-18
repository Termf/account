package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserYubikey;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 该定义主要是为了区分前台用户和管理后台不同表
 */
public interface YubikeyMapper {

    int deleteByPrimaryKey(@Param("id") Long id, @Param("userId") Long userId);

    int insert(UserYubikey record);

    int insertSelective(UserYubikey record);

    UserYubikey selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserYubikey record);

    int updateByPrimaryKey(UserYubikey record);

    List<UserYubikey> getByUserId(@Param("userId") Long userId,
                                  @Param("origin") String origin,
                                  @Param("credentialId") String credentialId);

    int updateSignatureCountByCredentialId(UserYubikey userYubikey);

    /**
     * 直接根据凭证ID 查询对应记录
     * @param credential
     * @return
     */
    UserYubikey getByCredentialId(String credential);

    List<Long> findRegisteredUserIds(@Param("uids") List<Long> uids);
}
