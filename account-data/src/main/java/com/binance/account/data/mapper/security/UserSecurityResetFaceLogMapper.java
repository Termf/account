package com.binance.account.data.mapper.security;

import com.binance.account.data.entity.security.UserSecurityResetFaceLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * @author liliang1
 * @date 2018-08-27 15:19
 */
@DefaultDB
public interface UserSecurityResetFaceLogMapper {

    /**
     * 创建一条新记录
     * @param record
     * @return
     */
    int insert(UserSecurityResetFaceLog record);

    /**
     * 根据ID查询
     * @param id
     * @return
     */
    UserSecurityResetFaceLog selectByPrimaryKey(Long id);

    /**
     * 修改记录
     * @param record
     * @return
     */
    int updateByPrimaryKeySelective(UserSecurityResetFaceLog record);

    /**
     * 修改记录
     * @param record
     * @return
     */
    int updateByPrimaryKey(UserSecurityResetFaceLog record);

    /**
     * 修改照片信息
     * @param record
     * @return
     */
    int saveFaceImage(UserSecurityResetFaceLog record);

    /**
     * 查询当前用户同一种操作类型在24小时内的记录次数
     * @param userId
     * @param resetType
     * @return
     */
    int getLogDailyTimes(@Param("userId") Long userId, @Param("resetType") int resetType);

    /**
     * 查询记录信息
     * @param userId
     * @param resetId
     * @param bizNo
     * @return
     */
    UserSecurityResetFaceLog selectByResetIdAndBizNo(@Param("userId") Long userId,
                                                     @Param("resetId") String resetId,
                                                     @Param("bizNo") String bizNo);

    /**
     * 查询记录信息列表
     * @param resetId
     * @param userId
     * @return
     */
    List<UserSecurityResetFaceLog> getByResetIdAndUserId(@Param("resetId")String resetId,
                                                         @Param("userId") Long userId);


    /**
     * 获取人脸识别失败的错误次数
     * @param userId
     * @param resetId
     * @param faceStatusFail
     * @return
     */
    int checkFaceFailCount(@Param("userId") Long userId,
                           @Param("resetId") String resetId,
                           @Param("faceStatus") String faceStatusFail);
}