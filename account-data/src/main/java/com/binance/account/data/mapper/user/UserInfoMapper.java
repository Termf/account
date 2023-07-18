package com.binance.account.data.mapper.user;

import java.util.List;
import java.util.Map;

import com.binance.account.data.entity.user.RelationUserInfo;
import com.binance.account.data.entity.user.UserAgentReward;
import org.apache.ibatis.annotations.Param;
import com.binance.account.data.entity.user.UserConfig;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.master.annotations.DefaultDB;
import org.javasimon.aop.Monitored;

@DefaultDB
public interface UserInfoMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserInfo record);

    int insertSelective(UserInfo record);

    UserInfo selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserInfo record);

    int updateByPrimaryKey(UserInfo record);

    int insertIgnore(UserInfo record);

    List<Long> queryUserId(UserInfo record);

    List<UserInfo> selectJobDailyThreeField(Map<String,Object> map);

    Long countJobDailyThreeField();

    String selectPhishingCode(Long userId);

    List<Long> fuzzyQueryByRemark(@Param("remark") String remark);

    List<UserInfo> getEmptyAccount();

    int updateByTradeLevel(@Param("userId") Long userId, @Param("tradeLevel") Integer tradeLevel);

    int updateTradeAutoStatus(@Param("userId") Long userId, @Param("tradeAutoStatus") String tradeAutoStatus);

	Long selectAccountIdByUserId(Long userId);

	UserConfig selectLatestUserConfig(UserConfig uc);

    List<UserConfig> selectUserConfigList(Map<String, Object> map);

    int updateUserConfig(UserConfig uc);

    int insertUserConfig(UserConfig uc);

    int batchInsertIgnoreUserConfig(List<UserConfig> list);

    int updateUserConfigToTrue(@Param("userId")Long userId, @Param("configTypes")List<String> configTypes);

	List<UserInfo> selectUserInfoList(@Param("userIds") List<Long> userIds);

    /**从user_info中获取用户返佣集合*/
    List<UserInfo> getUserInfoAgentList(Map<String, Object> paramMap);

    /*

    *//**获取用户分佣列表*//*
    List<UserAgentReward> getUserAgentList(Map<String, Object> paramMap);*/

    /**条件查询返佣列表集合*/
    List<UserAgentReward> getUserAgentRewardList(Map<String, Object> paramMap);

    /**获取个数*/
    Long getUserAgentRewardCount(Map<String, Object> paramMap);

    /**批量获取Email*/
    List<Map<String,Object>> getEmailByUserIds(@Param("userIds") List<Long> userIds);

    /**获取个数*/
    Long getUserInfoRewardCount(Map<String, Object> paramMap);

    /**根据userId查UserAgentReward*/
    UserAgentReward selectUserAgentRewardByUserId(Long userId);

    /**批量修改用户分佣比例*/
	Integer updateUserInfoAgentReward(UserAgentReward agent);
    @Monitored
    Long countAgentNumber(Long agentId);

    @Monitored
    List<UserInfo> selectUserInfoByAgentId(Map<String, Object> paramMap);

    int updateThreeDailyWithdrawFiledNull(List<Long> userIds);

    int updateDailyWithdrawCapNull(List<Long> userIds);

    int updateDailyWithdrawCountLimitNull(List<Long> userIds);

    int updateAutoWithdrawAuditThresholdNull(List<Long> userIds);

    int insertOrUpdateUserConfig(UserConfig uc);

    Long countUserInfoByAgentIdAndReferral(Map<String,Object> param);

    Long countOldAgentNum(Long agentId);

    List<UserInfo> selectOldAgentByAgentIdAndReferral(Map<String, Object> userParam);

    UserInfo selectRootUserInfoByFutureUserId(@Param("futureUserId") Long futureUserId);

    /**
     * 只能美国站使用
     */
    @Monitored
    List<RelationUserInfo> selectUSParentGroup();

    /**
     * 只能美国站使用
     */
    @Monitored
    List<RelationUserInfo> selectUSMarginGroup();


    List<UserInfo> queryUserInfoByUserId(UserInfo record);


    int resetMarginUserId(@Param("userId") Long userId);


    UserInfo selectRootUserInfoByMarginUserId(@Param("marginUserId") Long marginUserId);

    UserInfo selectRootUserInfoByCardUserId(@Param("cardUserId") Long cardUserId);


    int resetMarginUserIdByRootUserId(UserInfo record);
    
    int deleteParentByUserId(Long userId);

    long countUserType0();

    List<UserInfo> selectUserType0(Map<String, Object> param);

    int updateUserType(UserInfo userInfo);
    List<Long> selectFixDeliveryUserIds();

}
