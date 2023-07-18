package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserSecurityLogMapper {
    int deleteByPrimaryKey(Long id);

    /**
     * WARNING: 使用此方法时，注意需要给所有的字段赋值（建议使用 insertSelective）
     */
    @Deprecated
    int insert(UserSecurityLog record);

    int insertSelective(UserSecurityLog record);

    UserSecurityLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(UserSecurityLog record);

    int updateByPrimaryKey(UserSecurityLog record);

    List<UserSecurityLog> getUserSecurityListByUserIdAndOperateType(@Param("userId") Long userId,
                                                                    @Param("operateType") String operateType, @Param("startRow") int startRow, @Param("pageSize") int pageSize);

    long getUserSecurityCountByUserIdAndOperateType(@Param("userId") Long userId,
                                                    @Param("operateType") String operateType);

    UserSecurityLog getLastLoginLogByUserId(Long userId);

    UserSecurityLog  getLastUpdatePwdByUserId(Long userId);

    int insertIgnore(UserSecurityLog record);

    int insertIgnoreId(UserSecurityLog record);

    /**获取分页列表*/
    List<UserSecurityLog> getLogPage(UserSecurityLog UserSecurityLog);

    /**获取总条数*/
    Long getLogPageTotal(UserSecurityLog userSecurityLog);

    /**根据ip查列表*/
	List<UserSecurityLog> getLogByIp(UserSecurityLog userSecurityLog);

	/**获取ip关联的用户数*/
	List<Map<String, Object>> getLogByIpCount(@Param("ips") List<String> ips);

	List<UserSecurityLog> getSecurityByUserIds(@Param("userIds") List<Long> userIds, 
							@Param("operateType") String operateType, 
							@Param("startOperateTime") Date startOperateTime,
							@Param("endOperateTime") Date endOperateTime, 
							@Param("offset") int offset, @Param("limit") int limit);

	Long getSecurityCountByUserIds(@Param("userIds") List<Long> userIds, 
							@Param("operateType") String operateType,
							@Param("startOperateTime") Date startOperateTime,
							@Param("endOperateTime") Date endOperateTime);

    UserSecurityLog getLastBindGoogleVerify(Long userId);


    List<UserSecurityLog> getUserSecurityListByUserIdAndOperateTypeList(@Param("userId") Long userId,
                                                                        @Param("operateTypeList") List<String> operateTypeList, @Param("startRow") int startRow, @Param("pageSize") int pageSize);
}
