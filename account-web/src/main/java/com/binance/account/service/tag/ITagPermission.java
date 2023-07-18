package com.binance.account.service.tag;

import java.util.List;

import com.binance.account.vo.tag.response.TagCheckedCategoryResponse;
import com.binance.master.models.APIResponse;

/**
 * @author lufei
 * @date 2018/10/11
 */
public interface ITagPermission {

    APIResponse<String> savePermissionByRole(String roleId, List<Long> categoryIds);

    APIResponse<TagCheckedCategoryResponse> selectCheckedCategoryByRole(String roleId);

}
