package com.binance.account.data.mapper.user;

import com.binance.account.data.entity.user.ReCaptcha;
import com.binance.account.data.entity.user.User;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.RowBounds;

import java.util.Date;
import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserMapper {
    int insert(User record);

    int insertSelective(User record);

    User queryByEmail(@Param("email") String email);

    User queryById(Long userId);

    int updateByEmail(User record);


    int updateByEmailAndClearSafePassword(User record);


    Long queryUserStatusByEmail(@Param("email") String email);

    int updateUserStatusByEmail(User user);

    int updateByEmailSelective(User user);

    int insertIgnore(User user);

    List<Long> queryUserId(@Param("mask") Long mask, @Param("status") Long status, @Param("email") String email,
            @Param("startTime") Date startTime, @Param("endTime") Date endTime, @Param("emails") List<String> emailList, @Param("canEmailLike") boolean canEmailLike);

    List<Long> queryUserIdPage(@Param("page") RowBounds page);

    Long queryUserIdPageCount();

    List<User> queryUserByHavingStatusPage(@Param("page") RowBounds page, @Param("status") Long status);

    Long queryUserByHavingStatusCount(@Param("status") Long status);

    int deleteByEmail(String email);

    List<User> selectByUserIds(@Param("userIds") List<Long> userIds);

	int getTodayRegist(Map<String, Object> paramMap);

    /**多条件批量查询*/
    List<User> selectUserByUserIds(@Param("userIds") List<Long> subUserIds, @Param("email") String email,
                                   @Param("isSubUserEnabled") Integer isSubUserEnabled,
                                   @Param("limit") int limit, @Param("offset") int offset);

    Long selectCountSubUserIds(@Param("userIds") List<Long> subUserIds, @Param("email") String email,
            					@Param("isSubUserEnabled") Integer isSubUserEnabled);

    /**查询邮箱集合*/
	List<User> selectEmailListByUserIds(@Param("userIds") List<Long> subUserIds);

    /**保存reCaptcha响应结果*/
    Integer saveRecaptcha(ReCaptcha reCaptcha);

    /**查存在未删除的用户*/
	User queryByExistentEmail(@Param("email") String email);


    List<User> selectBindMobileUserList();

    List<User> getAllSubAccount();

    List<User> getAllSubAccountLastOneHour();



    List<User> selectUserPageByUserIds(Map<String, Object> paramMap);

    List<Long> selectSpecialUserId();

    List<Long> selectBrokerSubUserId();


    List<User> getAllParentAccount();


    Long countSubMarginUser(@Param("userIds") List<Long> subUserIds);

    List<User> selectByEmails(@Param("emails") List<String> emails);


    List<User> getNeedFixMarginUser();
    
    List<User> selectAllLVTSignedUser();

    List<Long> selectAllOneButtonUser();

    /**
     * 目前仅供 修复status job使用
     * @param email
     * @param statusBit
     * @return
     */
    int enableStatus(@Param("email") String email, @Param("statusBit") Long statusBit);


    /**
     * 修改stsatus带乐观锁
     * @param email
     * @param statusBit
     * @return
     */
    int enableStatusOptimisticLockUpdate(@Param("email") String email, @Param("statusBit") Long statusBit);
}
