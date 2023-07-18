package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.UserAgentReward;
import com.binance.master.annotations.DefaultDB;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

@DefaultDB
public interface UserAgentRewardMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserAgentReward record);

    UserAgentReward selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(Map<String, Object> params);

    int updateByPrimaryKey(UserAgentReward record);

    int saveUserAgentReward(Map<String, Object> params);

    /**
     * 获取最后一个BatchId
     * @return
     */
    String getLastBatchId();

	/**条件查询UserAgentReward表*/
	List<UserAgentReward> getAgentRewardList(Map<String, Object> paramMap);

	/**统计个数*/
	Long getUserAgentRewardNum(Map<String, Object> paramMap);

	/**
	 * 根据批次号修改返佣比例
	 * @param map
	 * @return
	 */
	Integer updateAgentStatusByBatchId(Map<String, Object> map);

	/**
	 * 根据userId修改返佣比例
	 * @param params
	 * @return
	 */
	Integer updateAgentRewardByUserId(Map<String, Object> params);

	List<UserAgentReward> selectByUserIds(@Param("userIds") List<Long> userIds);

	/**
	 * 恢复分佣比例job
	 * @return
	 */
	Integer updateExpectTime();
	
	/**
	 * 查需要恢复分佣比例的用户
	 * @return
	 */
	List<UserAgentReward> selectIsRestoreInfo();

	List<UserAgentReward> selectByIds(@Param("ids") List<Long> ids);

	List<UserAgentReward> selectByBatchIds(@Param("batchIds") List<String> batchIds);
}