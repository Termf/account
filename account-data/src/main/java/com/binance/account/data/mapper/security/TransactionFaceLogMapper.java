package com.binance.account.data.mapper.security;

import com.binance.account.common.enums.TransFaceLogStatus;
import com.binance.account.common.query.TransactionFaceQuery;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 *
 * @author liliang1
 * @date 2018-12-23
 */
@DefaultDB
public interface TransactionFaceLogMapper {

    int insert(TransactionFaceLog record);

    TransactionFaceLog selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(TransactionFaceLog record);

    /**
     * 根据业务编号获取对应记录
     * @param transId
     * @param transType
     * @return
     */
    TransactionFaceLog findByTransId(@Param("transId") String transId, @Param("transType") String transType);

    TransactionFaceLog findByUserIdTransId(@Param("userId") Long userId, @Param("transId") String transId, @Param("transType") String transType);

    /**
     * 查询用户的最新一笔记录
     * @param userId
     * @param transType
     * @param status
     * @return
     */
    TransactionFaceLog findLastByUserId(@Param("userId") Long userId,
                                        @Param("transType") String transType,
                                        @Param("status") TransFaceLogStatus status);

    /**
     * 查询多种类型中用户的最新一笔记录
     * @param userId
     * @param types
     * @param passed
     * @return
     */
    TransactionFaceLog findLastByUserIdMultipleType(@Param("userId") Long userId,
                                                    @Param("types") List<String> types,
                                                    @Param("status") TransFaceLogStatus passed);

    /**
     * 查询列表总条数
     * @param query
     * @return
     */
    long getTransactionFaceLogsCount(TransactionFaceQuery query);

    /**
     * 查询列表记录信息
     * @param query
     * @return
     */
    List<TransactionFaceLog> getTransactionFaceLogs(TransactionFaceQuery query);

    /**
     * 变更状态
     * @param faceLog
     * @return
     */
    int updateStatus(TransactionFaceLog faceLog);


}