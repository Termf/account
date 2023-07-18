package com.binance.account.service.tag;

import java.util.List;
import java.util.Map;

import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.tag.TagDetailDefine;
import com.binance.account.vo.tag.TagDetailValueVo;
import com.binance.account.vo.tag.TagDetailVo;
import com.binance.account.vo.tag.request.SaveBatchAddUserTag;
import com.binance.account.vo.tag.request.TagIndicatorConditionPageRequest;
import com.binance.account.vo.tag.request.TagIndicatorQueryRequest;
import com.binance.account.vo.tag.request.YubikeyMarketingRequest;
import com.binance.account.vo.tag.response.TagIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.account.vo.tag.response.TagUserStatusVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.master.models.APIResponse;

/**
 * @author lufei
 * @date 2018/5/8
 */
public interface ITagIndicator {

    APIResponse<TagResponse> saveIndicator(String user, String userId, String tagId, String remark, String value,
            List<TagDetailValueVo> detail) throws Exception;

    APIResponse<TagResponse> modifyIndicator(String user, String id, String tagId, String remark, String value,
            List<TagDetailValueVo> detail) throws Exception;

    APIResponse<TagResponse> removeIndicator(String id) throws Exception;

    APIResponse<TagInfoResponse> getTagInfoById(String id) throws Exception;

    APIResponse<SearchResult<TagIndicatorResponse>> getIndicatorList(TagIndicatorQueryRequest request) throws Exception;

    APIResponse<SearchResult<TagInfoIndicatorResponse>> getTagInfoByUserid(String tagUserid, Integer position,
            Integer size) throws Exception;

    APIResponse<List<TagInfoResponse>> selectByName(String tagName);

    APIResponse<List<Long>> findChildTagInfoId(Long id);

    APIResponse<List<TagIndicatorResponse>> selectByCondition(TagIndicatorConditionPageRequest param);

    APIResponse<Long> countByCondition(TagIndicatorConditionPageRequest param);

    APIResponse<TagIndicatorResponse> selectByTagIdAndUserId(String userId, Long tagId);

    List<TagDetailVo> tagDetailDefineToDetails(TagDetailDefine detailDefine);

    List<TagUserStatusVo> selectTagAndUserStatus(GetUserListRequest request);

    boolean addUsersUnderYubikey(List<Long> userIds);

    boolean isUserIdMatchTag(YubikeyMarketingRequest request);

    List<SaveBatchAddUserTag> batchSaveIndicator(List<SaveBatchAddUserTag> uIds);


    List<String> batchDeleteIndicator(Map<String,Long> uIdAndTags);
}
