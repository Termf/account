package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserSecurity;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserSecurityMapper {
    int deleteByPrimaryKey(Long userId);

    int insert(UserSecurity record);

    int insertSelective(UserSecurity record);

    UserSecurity selectByPrimaryKey(Long userId);

    int updateByPrimaryKeySelective(UserSecurity record);

    int updateByPrimaryKey(UserSecurity record);

    /**
     * 解绑google验证(会更新unbind_time)
     * 
     * @param security
     * @return
     */
    int updateAuthKeyByEmail(UserSecurity security);

    int updateDeregisterYubikeyTimeByUserId(Long userId);

    /**
     * 解绑手机验证(会更新unbind_time)
     * 
     * @param security
     * @return
     */
    int updateMobileByUserId(UserSecurity security);

	int resetLoginFailedNum(Long userId);

	int insertIgnore(UserSecurity security);

	List<Long> queryUserId(UserSecurity security);

	int updateSecurityByUserId(UserSecurity security);

	int updateBindInfoByUserId(UserSecurity security);

	String selectAntiPhishingCode(Long userId);

	List<Long> selectUserIdByMobileCode(String mobileCode);

	List<UserSecurity> selectUserSecurityList(@Param("userIds") List<Long> userIds);

	UserSecurity queryByMobile(@Param("mobile") String mobile);

	UserSecurity queryByMobileAndMobileCode(@Param("mobile") String mobile,@Param("mobileCode") String mobileCode);


	List<UserSecurity> selectUserSecurityByUserIds(@Param("userIds") List<Long> userIds);

	/** 修改禁用标识 */
	Integer updateStatusByUserId(Map<String, Object> paramMap);

	List<Long> selectRecentUpdateUserId(@Param("recentTime") Date recentTime);

	String selectMobileByUserId(Long userId);

	/**
	 * 变更提现是否需要做人脸识别的标识
	 *
	 * @param userId
	 * @param toStatus   修改后的状态
	 * @param fromStatus 修改前的状态(当为空时不做判断)
	 * @return
	 */
	int updateWithdrawSecurityFaceStatusByUserId(@Param("userId") Long userId, @Param("toStatus") Integer toStatus,
			@Param("fromStatus") Integer fromStatus);

	/**
	 * 更新用户的reset失败次数和保护状态
	 * 
	 * @param userId          用户id
	 * @param protectedStatus 保护状态枚举ordinary()
	 * @return
	 */
	int updateProtectedMode(@Param("userId") Long userId,
							@Param("protectedStatus") Integer protectedStatus);

	List<UserSecurity> selectUserSecurityPageByUserIds(Map<String, Object> paramMap);

	Long selectYubikeyEnabledScenarios(Long userId);


	UserSecurity queryByEmail(@Param("email") String email);


	int updateUnBindEmailByUserId(UserSecurity security);


}
