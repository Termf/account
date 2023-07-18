package com.binance.account.data.mapper.security;

import com.binance.account.common.query.ResetModularQuery;
import com.binance.account.data.entity.security.UserSecurityReset;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserSecurityResetMapper {
    int deleteByPrimaryKey(String id);

    int insert(UserSecurityReset record);

    int insertSelective(UserSecurityReset record);

    UserSecurityReset selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(UserSecurityReset record);

    int updateByPrimaryKey(UserSecurityReset record);

    /**
     * 获取用户的最近一笔申请记录信息
     * @param userId
     * @param typeOrdinal
     * @return
     */
    UserSecurityReset getLastByUserId(@Param("userId") Long userId,
                                      @Param("typeOrdinal") Integer typeOrdinal);

    /**
     * 查询一段时间内已经包含有JUMIO标识的记录数
     * @param userId
     * @param typeOrdinal
     * @param startTime
     * @param endTime
     * @return
     */
    Long getDailyResetWithScanRefTimes(@Param("userId") Long userId,
                                       @Param("typeOrdinal") Integer typeOrdinal,
                                       @Param("startTime") Date startTime,
                                       @Param("endTime") Date endTime);

    /**
     * 查询长时间没有变化，状态处于待处理，并且不再人脸通过状态的数据(每次获取50条记录)
     * @param endTime
     * @return
     */
    List<UserSecurityReset> getLongTimeNoChangeList(@Param("endTime") Date endTime);

    /**
     * 查询出处于人脸识别状态的数据(每次查询100条)
     * @return
     */
    List<UserSecurityReset> getFaceFailStatusList(@Param("endTime") Date endTime);

    /**
     * 查询需要做人脸识别的邮件重置流程信息(每次查询50条记录)
     * 只查询JUMIO-通过的记录
     * @return
     */
    List<UserSecurityReset> getPendingFaceList(@Param("endTime") Date endTime);

    /**
     * 获取用户的某一类型的申请
     * @param userId
     * @param type
     * @return
     */
    Map<String, Long> getResetApplyTimes(@Param("userId") Long userId, @Param("typeOrdinal") int type);

    /**
     * 查询重置流程的列表信息
     * @param query
     * @return
     */
    List<UserSecurityReset> getResetList(ResetModularQuery query);

    /**
     * 查询重置流程的条数信息
     * @param query
     * @return
     */
    long getResetListCount(ResetModularQuery query);

    /**
     * 获取用户所有重置流程信息
     * @param userId
     * @return
     */
    List<UserSecurityReset> getUserAllReset(Long userId);

    /**
     * 查询证件号是否被别的用户占用
     * @param userId
     * @param idNumber
     * @param issuingCountry
     * @param documentType
     * @return
     */
    long haveResetNumberExistByOtherUser(@Param("userId") Long userId,
                                         @Param("idNumber") String idNumber,
                                         @Param("issuingCountry") String issuingCountry,
                                         @Param("documentType") String documentType);

    /**
     * 设置人脸识别状态信息
     * @param reset
     * @return
     */
    int updateFaceStatus(UserSecurityReset reset);

    /**
     * 保存从JUMIO获取到的数据
     * @param resetModel
     * @return
     */
    int updateJumioInfo(UserSecurityReset resetModel);

    /**
     * 检查需要同步JUMIO结果的数据
     * @param startTime
     * @param endTime
     * @return
     */
    List<UserSecurityReset> getNeedCheckJumioResults(@Param("startTime") Date startTime, @Param("endTime") Date endTime);

    /**
     * 删除jumio的初始化信息
     * @param reset
     * @return
     */
    int removeJumioInitScanRef(UserSecurityReset reset);
}