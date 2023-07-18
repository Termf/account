package com.binance.account.data.mapper.subuser;

import com.binance.account.data.entity.subuser.SubUserBinding;
import com.binance.master.annotations.DefaultDB;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@DefaultDB
public interface SubUserBindingMapper {


    int insert(SubUserBinding record);

    int updateBySubUserId(SubUserBinding record);

    int updateBySubUserIdSelective(SubUserBinding record);

    SubUserBinding selectBySubUserId(Long subUserId);

    List<SubUserBinding> getSubUserBindingsByParentUserId(Long parentUserId);

    List<SubUserBinding> getSubUserBindingsByPage(@Param("parentUserId") Long parentUserId, @Param("offset") Integer offset, @Param("limit") Integer limit);

    long countSubUsersByParentUserId(Long parentUserId);

    /**根据parentId查subUserId集合*/
    List<Long> selectSubUserIdsByParent(Long parentUserId);

    SubUserBinding selectByParentUserIdAndBrokerSubAccountId(@Param("parentUserId") Long parentUserId, @Param("brokerSubAccountId")Long brokerSubAccountId);

    SubUserBinding selectByParentUserIdAndSubUserId(@Param("parentUserId") Long parentUserId, @Param("subUserId")Long subUserId);


    int updateRemarkByParentIdAndSubUserId(SubUserBinding record);


    List<SubUserBinding> selectByParentUserIdAndSubUserIds(Map<String, Object> param);

    SubUserBinding selectByBrokerSubAccountId(Long brokerSubAccountId);

    int deleteBySubUserIdAndParentUserId(@Param("parentUserId") Long parentUserId,@Param("subUserId") Long subUserId);



    int updateSelectiveBySubUserIdAndParentUserId(SubUserBinding record);

    List<SubUserBinding> selectFutureSubUserByParent(Long parentUserId);


    long countSubUsersBySelective(Map<String, Object> param);


    List<SubUserBinding> getBrokerSubbindingInfoByPage(Map<String, Object> param);




}
