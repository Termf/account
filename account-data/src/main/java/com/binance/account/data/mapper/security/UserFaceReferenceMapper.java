package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserFaceReference;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liliang1
 * @date 2018-09-14
 */
@DefaultDB
public interface UserFaceReferenceMapper {

    int insert(UserFaceReference record);

    UserFaceReference selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserFaceReference record);

    List<UserFaceReference> getListByUserIds(@Param("userIds") List<Long> userList);

    int updateRefImage(UserFaceReference newReference);
}