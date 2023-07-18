package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.agent.UserAgentLog;
import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.user.FutureInvitationLog;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface FutureInvitationLogMapper {
    int insert(@Param("invitationCode") String invitationCode);

    int enableInvitationCode(@Param("invitationCode") String invitationCode);

    FutureInvitationLog selectByPrimaryKey(@Param("invitationCode") String invitationCode);

}
