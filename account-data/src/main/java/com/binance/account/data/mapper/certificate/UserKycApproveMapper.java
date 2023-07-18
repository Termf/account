package com.binance.account.data.mapper.certificate;

import com.binance.account.common.query.BaseQuery;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface UserKycApproveMapper {

    UserKycApprove selectByPrimaryKey(Long userId);

    int insert(UserKycApprove userKycApprove);

    int deleteByPrimaryKey(Long userId);

    List<UserKycApprove> getList(BaseQuery query);

    long getListCount(BaseQuery query);

    int updateSelective(UserKycApprove userKycApprove);

    /**
     * 根据jumioId获取
     * @return
     */
    UserKycApprove getByJumioId(@Param("userId")Long userId, @Param("jumioId")String id);

    List<UserKycApprove> getUserKycApproveList(UserKycApprove userKycApprove);

    int updateOcrResult(UserKycApprove userKycApprove);

    List<UserKycApprove> selectUnFillCertificateInfoDataByPage(Map<String, Object> param);

    int updateCertificateInfo(UserKycApprove userKycApprove);

    int updateMoveMsg(@Param("userId")Long userId, @Param("moveMsg")String moveMsg);

    List<UserKycApprove> selectKycDataMigration(@Param("moveMsg") String moveMsg,@Param("start") int start,@Param("rows") int rows);

    int updateFaceCheck(@Param("userId")Long userId, @Param("faceCheck")String faceCheck);

    List<UserKycApprove> selectFaceCheckList(@Param("faceCheck") String faceCheck, @Param("start") int start,@Param("rows") int rows, @Param("userId")Long userId);
}
