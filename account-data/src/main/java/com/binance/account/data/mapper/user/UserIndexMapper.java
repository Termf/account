package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserIndex;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@DefaultDB
public interface UserIndexMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserIndex record);

    int insertSelective(UserIndex record);

    UserIndex selectByPrimaryKey(Long userId);

    Long selectIdByEmail(String email);

    String selectEmailById(Long userId);

    int updateByPrimaryKeySelective(UserIndex record);

    int updateByPrimaryKey(UserIndex record);

    int registerByUserId(UserIndex record);

    Long maxUserId();

    int insertIgnore(UserIndex record);

    Long maxUserIdByMaxUserId(@Param("userId") Long userId);

    List<UserIndex> fuzzyQueryByEmail(@Param("email") String email);

    List<UserIndex> selectByUserIds(@Param("userIds")Collection<Long> userIds);
    
    List<UserIndex> selectByEmails(@Param("emails")Collection<String> emails);


    List<UserIndex> selectUnusedUserIndex();


}
