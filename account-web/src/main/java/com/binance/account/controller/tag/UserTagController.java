package com.binance.account.controller.tag;

import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.binance.account.vo.tag.request.*;
import com.binance.account.vo.tag.response.TagUserStatusResponse;
import com.binance.account.vo.tag.response.TagUserStatusVo;
import com.binance.account.vo.user.request.GetUserListRequest;
import org.javasimon.aop.Monitored;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UserTagApi;
import com.binance.account.common.query.SearchResult;
import com.binance.account.service.tag.ITagCategory;
import com.binance.account.service.tag.ITagImport;
import com.binance.account.service.tag.ITagIndicator;
import com.binance.account.service.tag.ITagInfo;
import com.binance.account.service.tag.ITagPermission;
import com.binance.account.vo.tag.response.TagCategoryResponse;
import com.binance.account.vo.tag.response.TagCheckedCategoryResponse;
import com.binance.account.vo.tag.response.TagIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.account.vo.tag.response.TagTreeResponse;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

/**
 * @author lufei
 * @date 2018/6/25
 */
@RestController
public class UserTagController implements UserTagApi {

    @Resource
    private ITagCategory tagCategory;
    @Resource
    private ITagImport tagImport;
    @Resource
    private ITagIndicator tagIndicator;
    @Resource
    private ITagInfo tagInfo;
    @Resource
    private ITagPermission tagPermission;

    @Override
    public APIResponse<TagResponse> saveCategory(@RequestBody APIRequest<TagSaveCategoryRequest> request)
            throws Exception {
        TagSaveCategoryRequest body = request.getBody();
        return tagCategory.saveCategory(body.getPid(), body.getName());
    }

    @Override
    public APIResponse<TagResponse> modifyCategory(@RequestBody APIRequest<TagModifyCategoryRequest> request)
            throws Exception {
        TagModifyCategoryRequest body = request.getBody();
        return tagCategory.modifyCategory(body.getId(), body.getPid(), body.getName());
    }

    @Override
    public APIResponse<TagResponse> removeCategory(@RequestBody APIRequest<TagIdRequest> request) throws Exception {
        TagIdRequest body = request.getBody();
        return tagCategory.removeCategory(body.getId());
    }

    @Override
    public APIResponse<TagResponse> saveTag(@RequestBody APIRequest<TagInfoEntityRequest> request) throws Exception {
        TagInfoEntityRequest body = request.getBody();
        return tagInfo.saveTag(body);
    }

    @Override
    public APIResponse<TagResponse> modifyTag(@RequestBody APIRequest<TagInfoEntityRequest> request) throws Exception {
        TagInfoEntityRequest body = request.getBody();
        return tagInfo.modifyTag(body);
    }

    @Override
    public APIResponse<TagResponse> removeTag(@RequestBody APIRequest<TagIdRequest> request) throws Exception {
        TagIdRequest body = request.getBody();
        return tagInfo.removeTag(body.getId());
    }

    @Override
    public APIResponse<TagResponse> saveIndicator(@RequestBody APIRequest<TagSaveIndicatorRequest> request)
            throws Exception {
        TagSaveIndicatorRequest body = request.getBody();
        return tagIndicator.saveIndicator(body.getUser(), body.getUserId(), body.getTagId(), body.getRemark(),
                body.getValue(), body.getDetail());
    }

    @Override
    public APIResponse<TagResponse> modifyIndicator(@RequestBody APIRequest<TagModifyIndicatorRequest> request)
            throws Exception {
        TagModifyIndicatorRequest body = request.getBody();
        return tagIndicator.modifyIndicator(body.getUser(), body.getId(), body.getTagId(), body.getRemark(),
                body.getValue(), body.getDetail());
    }

    @Override
    public APIResponse<TagResponse> removeIndicator(@RequestBody APIRequest<TagIdRequest> request) throws Exception {
        TagIdRequest body = request.getBody();
        return tagIndicator.removeIndicator(body.getId());
    }

    @Override
    public APIResponse<TagResponse> uploadIndicator(@RequestBody APIRequest<TagImportRequest> request)
            throws Exception {
        TagImportRequest body = request.getBody();
        return tagImport.upload(body.getUser(), body.getXId(), body.getType(), body.getData());
    }

    @Override
    public APIResponse<String> savePermissionByRole(@RequestBody APIRequest<TagPermissionRequest> request)
            throws Exception {
        TagPermissionRequest body = request.getBody();
        return tagPermission.savePermissionByRole(body.getRoleId(), body.getCategoryIds());
    }

    @Override
    public APIResponse<TagCategoryResponse> getTagCategoryById(@RequestBody APIRequest<TagIdRequest> request)
            throws Exception {
        TagIdRequest body = request.getBody();
        return tagCategory.getTagCategoryById(body.getId());
    }

    @Override
    public APIResponse<List<TagTreeResponse>> getTagCategoryList(@RequestBody APIRequest<TagPIdRequest> request)
            throws Exception {
        TagPIdRequest body = request.getBody();
        return tagCategory.getTagCategoryList(body.getPid() + "");
    }

    @Override
    public APIResponse<SearchResult<TagCategoryResponse>> getTagCategoryParentList(
            @RequestBody APIRequest<TagPageRequest> request) throws Exception {
        TagPageRequest body = request.getBody();
        return tagCategory.getTagCategoryParentList(body);
    }

    @Override
    public APIResponse<TagInfoResponse> getTagInfoById(@RequestBody APIRequest<TagIdRequest> request) throws Exception {
        TagIdRequest body = request.getBody();
        return tagIndicator.getTagInfoById(body.getId());
    }

    @Override
    public APIResponse<SearchResult<TagInfoResponse>> getTagList(@RequestBody APIRequest<TagPageRequest> request)
            throws Exception {
        TagPageRequest body = request.getBody();
        return tagInfo.getTagList(body);
    }

    @Override
    public APIResponse<SearchResult<TagIndicatorResponse>> getIndicatorList(
            @RequestBody APIRequest<TagIndicatorQueryRequest> request) throws Exception {
        TagIndicatorQueryRequest body = request.getBody();
        return tagIndicator.getIndicatorList(body);
    }



    @Override
    public APIResponse<SearchResult<TagInfoIndicatorResponse>> getTagInfoByUserid(
            @RequestBody APIRequest<TagUserPageRequest> request) throws Exception {
        TagUserPageRequest body = request.getBody();
        return tagIndicator.getTagInfoByUserid(body.getTagUserid(), body.getPosition(), body.getSize());
    }

    @Override
    public APIResponse<Object> getTree(@RequestBody APIRequest<TagTIdRequest> request) throws Exception {
        TagTIdRequest body = request.getBody();
        return tagCategory.getTree(body.getTid(), body.getRoleIds());
    }

    @Override
    public APIResponse<Object> getExtraInfo(@RequestBody APIRequest<TagExtranInfoRequest> request) throws Exception {
        TagExtranInfoRequest body = request.getBody();
        return tagCategory.getExtraInfo(body.getUserId(), body.getTagId());
    }

    @Override
    public APIResponse<List<TagInfoResponse>> selectByName(@RequestBody APIRequest<TagNameRequest> request)
            throws Exception {
        TagNameRequest body = request.getBody();
        return tagIndicator.selectByName(body.getTagName());
    }

    @Override
    public APIResponse<List<Long>> findChildTagInfoId(@RequestBody APIRequest<TagIdRequest> request) throws Exception {
        TagIdRequest body = request.getBody();
        return tagIndicator.findChildTagInfoId(Long.parseLong(body.getId()));
    }

    @Override
    public APIResponse<List<TagIndicatorResponse>> selectByCondition(
            @RequestBody APIRequest<TagIndicatorConditionPageRequest> request) throws Exception {
        TagIndicatorConditionPageRequest body = request.getBody();
        return tagIndicator.selectByCondition(body);
    }

    @Override
    public APIResponse<Long> countByCondition(@RequestBody APIRequest<TagIndicatorConditionPageRequest> request)
            throws Exception {
        TagIndicatorConditionPageRequest body = request.getBody();
        return tagIndicator.countByCondition(body);
    }

    @Override
    public APIResponse<List<TagInfoResponse>> selectByCategoryNameAndPTagName(
            @RequestBody APIRequest<TagCategoryNameAndPTagNameRequest> request) {
        TagCategoryNameAndPTagNameRequest body = request.getBody();
        return tagInfo.selectByCategoryNameAndPTagName(body.getCategoryName(), body.getPTagName());
    }

    @Override
    public APIResponse<TagIndicatorResponse> selectByTagIdAndUserId(
            @RequestBody APIRequest<TagExtranInfoRequest> request) {
        TagExtranInfoRequest body = request.getBody();
        return tagIndicator.selectByTagIdAndUserId(body.getUserId(), Long.parseLong(body.getTagId()));
    }

    @Override
    public APIResponse<TagCheckedCategoryResponse> selectCheckedCategoryByRole(
            @RequestBody APIRequest<TagCheckedCategoryRequest> request) {
        TagCheckedCategoryRequest body = request.getBody();
        return tagPermission.selectCheckedCategoryByRole(body.getRoleId());
    }

    @Override
    @Monitored
    public APIResponse<TagUserStatusResponse> selectTagAndUserStatus(
            @RequestBody APIRequest<GetUserListRequest> request) throws Exception {
        GetUserListRequest body = request.getBody();
        List<TagUserStatusVo> vos = tagIndicator.selectTagAndUserStatus(body);
        TagUserStatusResponse response = new TagUserStatusResponse();
        response.setUserStatusVoList(vos);
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<Boolean> addUsersUnderYubikey(@RequestBody APIRequest<List<Long>> request) throws Exception {
        return APIResponse.getOKJsonResult(tagIndicator.addUsersUnderYubikey(request.getBody()));
    }

    @Override
    public APIResponse<Boolean> isUserIdMatchTag(@RequestBody APIRequest<YubikeyMarketingRequest> request) throws Exception {
        return APIResponse.getOKJsonResult(tagIndicator.isUserIdMatchTag(request.getBody()));
    }

    @Override
    public APIResponse<List<SaveBatchAddUserTag>> batchAddUserIdTagForDaily(@RequestBody APIRequest<List<SaveBatchAddUserTag>> request) throws Exception {
        return APIResponse.getOKJsonResult(tagIndicator.batchSaveIndicator(request.getBody()));
    }

    @Override
    public APIResponse<List<String>> batchRemoveIndicator(@RequestBody APIRequest<Map<String,Long>> uIdAndTags) throws Exception {
        return APIResponse.getOKJsonResult(tagIndicator.batchDeleteIndicator(uIdAndTags.getBody()));
    }

}
