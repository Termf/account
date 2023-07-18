package com.binance.account.service.tag;

import java.util.List;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.tag.request.TagPageRequest;
import com.binance.account.vo.tag.response.TagCategoryResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.account.vo.tag.response.TagTreeResponse;
import com.binance.master.models.APIResponse;

public interface ITagCategory {

    APIResponse<TagResponse> saveCategory(String pid, String name) throws Exception;

    APIResponse<TagResponse> modifyCategory(String id, String pid, String name) throws Exception;

    APIResponse<TagResponse> removeCategory(String id) throws Exception;

    APIResponse<TagCategoryResponse> getTagCategoryById(String id) throws Exception;

    APIResponse<Object> getTree(String tid, List<String> roleIds) throws Exception;

    APIResponse<Object> getExtraInfo(String userId, String tagId) throws Exception;

    APIResponse<SearchResult<TagCategoryResponse>> getTagCategoryParentList(TagPageRequest request) throws Exception;

    APIResponse<List<TagTreeResponse>> getTagCategoryList(String pid) throws Exception;
}
