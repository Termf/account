package com.binance.account.data.mapper.agent;

import com.binance.account.data.entity.agent.ApiAgentRewardConfig;
import com.binance.master.annotations.DefaultDB;
import com.github.pagehelper.Page;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@DefaultDB
public interface ApiAgentRewardConfigMapper {
    int delete(@Param("id") Long id, @Param("updateBy") String updateBy);

    int insert(ApiAgentRewardConfig record);

    int insertSelective(ApiAgentRewardConfig record);

    ApiAgentRewardConfig selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(ApiAgentRewardConfig record);

    int updateByPrimaryKey(ApiAgentRewardConfig record);

    /**
     * agentRewardCode且未删除的
     * @param agentRewardCode
     * @return
     */
    ApiAgentRewardConfig selectByAgentCode(String agentRewardCode);

    Page<ApiAgentRewardConfig> selectPage(ApiAgentRewardConfig query);

    /**
     * 已删除的code，也会查出
     * @param agentRewardCode
     * @return
     */
    ApiAgentRewardConfig ifAgentCodeExist(String agentRewardCode);

    List<ApiAgentRewardConfig> selectByAgentId(Long agentId);
}
