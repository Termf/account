package com.binance.account.service.tag;

import java.util.List;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.tag.request.TagInfoEntityRequest;
import com.binance.account.vo.tag.request.TagPageRequest;
import com.binance.account.vo.tag.response.TagInfoResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.master.models.APIResponse;

public interface ITagInfo {

    APIResponse<TagResponse> saveTag(TagInfoEntityRequest request) throws Exception;

    APIResponse<TagResponse> modifyTag(TagInfoEntityRequest request) throws Exception;

    APIResponse<TagResponse> removeTag(String id) throws Exception;

    APIResponse<SearchResult<TagInfoResponse>> getTagList(TagPageRequest request) throws Exception;

    APIResponse<List<TagInfoResponse>> selectByCategoryNameAndPTagName(String categoryName, String pTagName);

}
