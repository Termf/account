package com.binance.account.data.mapper.user;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.binance.master.annotations.DefaultDB;

/**
 * @author lufei
 * @date 2018/8/7 查询user、user_info、user_security表，为了速度（不按照分表规则查询，会搜索20张表），直接查询user_0、user_1...表
 */
@DefaultDB
public interface UserSearchMapper {

    List<Long> queryUser(@Param("table") String table, @Param("param") Map<String, Object> param);

    List<Long> queryUserInfo(@Param("table") String table, @Param("param") Map<String, Object> param);

    List<Long> queryUserSecurity(@Param("table") String table, @Param("param") Map<String, Object> param);

}
