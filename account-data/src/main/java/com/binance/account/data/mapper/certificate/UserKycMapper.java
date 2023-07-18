package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.BaseQuery;
import com.binance.account.common.query.UserKycModularQuery;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.Date;
import java.util.List;

@DefaultDB
public interface UserKycMapper {

    UserKyc getById(@Param("userId")Long userId,@Param("id")Long id);
    
    List<UserKyc> getByPrimaryKeys(@Param("kycIds") Collection<Long> kycIds);

    UserKyc getLast(Long userId);

    int saveBaseInfo(UserKyc userKyc);

    int updateByPrimaryKeySelective(UserKyc record);

    /**
     * 获取一天提交总数
     *
     * @return
     */
    int getDailySubmitCount(Long userId);

    /**
     * 获取未完成KYC记录
     *
     * @return
     */
    int getIncompleteCount(Long userId);

    /**
     * 获取所有KYC记录
     *
     * @return
     */
    List<UserKyc> getList(BaseQuery query);

    /**
     * 获取所有KYC记录
     *
     * @return
     */
    long getListCount(BaseQuery query);

    /**
     * 更新KYC记录
     *
     * @return
     */
    int updateStatus(UserKyc userKyc);

    /**
     * 根据jumioId获取
     * @return
     */
    UserKyc getByJumioId(@Param("userId")Long userId,@Param("jumioId")String id);

    /**
     * 保存jumio的关联ID
     * @param userKyc
     */
    int saveJumioId(UserKyc userKyc);

    /**
     * 查询个人认证模块化列表信息
     * @param query
     * @return
     */
    List<UserKyc> getModularUserKycList(UserKycModularQuery query);

    /**
     * 查询个人认证模块化列表条数
     * @param query
     * @return
     */
    long getModularUserKycListCount(UserKycModularQuery query);

    /**
     * 变更人脸识别信息
     * @param kyc
     * @return
     */
    int updateFaceStatus(UserKyc kyc);

    /**
     * 任务查询过期或者长时间无JUMIO的记录进行同步
     * @param startCreateTime
     * @param endUpdateTime
     * @return
     */
    List<UserKyc> getExpiredCheckData(@Param("startCreateTime") Date startCreateTime, @Param("endUpdateTime") Date endUpdateTime);

    /**
     * 保存证件ocr的状态结果
     * @param userKyc
     * @return
     */
    int saveFaceOcrStatus(UserKyc userKyc);
    
    /**
     * 修改basic 信息
     * @param record
     * @return
     */
    int updateBasicByPrimaryKey(UserKyc record);
	
	/**
     * 修改ocr结果
     * @param kyc
     * @return
     */
    int updateOcrResult(UserKyc kyc);

    /**
     * 变更用户的输入姓名
     * @param userKyc
     * @return
     */
    int updateFillName(UserKyc userKyc);
}
