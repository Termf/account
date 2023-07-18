package com.binance.account.data.mapper.tag;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.tag.TagIndicator;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface TagIndicatorMapper {

    int insert(TagIndicator indicator);

    int insertbatch(List<TagIndicator> models);

    List<TagIndicator> selectByCondition(Map<String, Object> param);

    Long countByCondition(Map<String, Object> param);

    int deleteByPrimaryKey(@Param("id") String id);

    TagIndicator selectByTagIdAndUserId(@Param("userId") String userId, @Param("tagId") Long tagId);

    int updateByPrimaryKey(TagIndicator model);

    List<Map<String, Object>> selectTagInfoByUserid(Map<String, Object> param);

    Long countTagInfoByUserid(Map<String, Object> param);

    TagIndicator selectOneById(@Param("id") Long id);

    List<Long> selectUserIdsByTagIds(@Param("tagIds") List<Long> tagIds);

    List<Map<String, Long>> countBindTagByUserId(@Param("userIds") List<String> userIds);

    List<Map<String, Object>> selectTagNameByUserIds(@Param("userIds") List<Long> userIds);

    List<Long> selectByUserIdsAndTagIds(@Param("list") List<Map<String, Long>> list);

    int deleteByUIdAndTagId(@Param("userId") String uid,@Param("tagId") Long tagId);

}
