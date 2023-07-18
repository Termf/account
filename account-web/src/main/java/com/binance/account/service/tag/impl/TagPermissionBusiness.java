package com.binance.account.service.tag.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Resource;

import com.binance.account.vo.tag.response.TagCheckedCategoryResponse;
import org.springframework.stereotype.Service;

import com.binance.account.data.entity.tag.TagPermission;
import com.binance.account.data.mapper.tag.TagPermissionMapper;
import com.binance.account.service.tag.ITagPermission;
import com.binance.master.models.APIResponse;
import org.springframework.util.CollectionUtils;

/**
 * @author lufei
 * @date 2018/10/11
 */
@Service
public class TagPermissionBusiness implements ITagPermission {

    @Resource
    private TagPermissionMapper permissionMapper;

    @Override
    public APIResponse<String> savePermissionByRole(String roleId, List<Long> categoryIds) {
        if(CollectionUtils.isEmpty(categoryIds)){
            permissionMapper.deleteByRoleId(roleId);
        }else{
            List<TagPermission> permissions = new ArrayList<>(categoryIds.size());
            for (Long categoryId : categoryIds) {
                TagPermission permission = new TagPermission();
                permission.setRoleId(roleId);
                permission.setCategoryId(categoryId);
                permissions.add(permission);
            }
            permissionMapper.deleteByRoleId(roleId);
            permissionMapper.insertBatch(permissions);
        }
        return APIResponse.getOKJsonResult("成功");
    }

    @Override
    public APIResponse<TagCheckedCategoryResponse> selectCheckedCategoryByRole(String roleId) {
        List<TagPermission> permissions = permissionMapper.selectByRoleId(roleId);
        List<Long> categoryIds = permissions.stream().map(p->p.getCategoryId()).collect(Collectors.toList());
        return APIResponse.getOKJsonResult(new TagCheckedCategoryResponse(categoryIds));
    }

}
