package com.binance.account.data.mapper.tag;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.binance.account.data.entity.tag.TagCategory;
import com.binance.master.annotations.DefaultDB;

@DefaultDB
public interface TagCategoryMapper {

    int insert(TagCategory tagCategory);

    int deleteByPrimaryKey(@Param("id") String id);

    int updateByPrimaryKey(TagCategory tagCategory);

    List<TagCategory> selectByPid(@Param("pid") String pid);

    TagCategory selectByPrimaryKey(@Param("id") String id);

    /**
     * @param pid 标签类父ID
     * @param name 标签类名称
     * @return 标签类父ID下，包含的标签类
     */
    List<TagCategory> getByPidAndName(@Param("pid") String pid, @Param("name") String name);

    List<TagCategory> selectByName(@Param("name") String name);

}
