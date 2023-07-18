package com.binance.account.api;

import java.util.List;
import java.util.Map;

import com.binance.account.vo.tag.request.*;
import com.binance.account.vo.tag.response.TagUserStatusResponse;
import com.binance.account.vo.user.request.GetUserListRequest;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import com.binance.account.common.query.SearchResult;
import com.binance.account.vo.tag.response.TagCategoryResponse;
import com.binance.account.vo.tag.response.TagCheckedCategoryResponse;
import com.binance.account.vo.tag.response.TagIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoIndicatorResponse;
import com.binance.account.vo.tag.response.TagInfoResponse;
import com.binance.account.vo.tag.response.TagResponse;
import com.binance.account.vo.tag.response.TagTreeResponse;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

/**
 * @author lufei
 * @date 2018/6/21
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE)
@Api("用户标签")
@RequestMapping("/tag")
public interface UserTagApi {

    @ApiOperation("保存标签组")
    @PostMapping("/config/saveTagCategory")
    APIResponse<TagResponse> saveCategory(@RequestBody APIRequest<TagSaveCategoryRequest> request) throws Exception;

    @ApiOperation("修改标签组")
    @PostMapping("/config/modifyTagCategory")
    APIResponse<TagResponse> modifyCategory(@RequestBody APIRequest<TagModifyCategoryRequest> request) throws Exception;

    @ApiOperation("删除标签组")
    @PostMapping("/config/removeTagCategory")
    APIResponse<TagResponse> removeCategory(@RequestBody APIRequest<TagIdRequest> request) throws Exception;

    @ApiOperation("保存标签")
    @PostMapping("/config/saveTag")
    APIResponse<TagResponse> saveTag(@RequestBody APIRequest<TagInfoEntityRequest> request) throws Exception;

    @ApiOperation("修改标签")
    @PostMapping("/config/modifyTag")
    APIResponse<TagResponse> modifyTag(@RequestBody APIRequest<TagInfoEntityRequest> request) throws Exception;

    @ApiOperation("删除标签")
    @PostMapping("/config/removeTag")
    APIResponse<TagResponse> removeTag(@RequestBody APIRequest<TagIdRequest> request) throws Exception;

    @ApiOperation("保存用户标签关系")
    @PostMapping("/config/saveIndicator")
    APIResponse<TagResponse> saveIndicator(@RequestBody APIRequest<TagSaveIndicatorRequest> request) throws Exception;

    @ApiOperation("修改用户标签关系")
    @PostMapping("/config/modifyIndicator")
    APIResponse<TagResponse> modifyIndicator(@RequestBody APIRequest<TagModifyIndicatorRequest> request)
            throws Exception;

    @ApiOperation("删除用户标签关系")
    @PostMapping("/config/removeIndicator")
    APIResponse<TagResponse> removeIndicator(@RequestBody APIRequest<TagIdRequest> request) throws Exception;

    @ApiOperation("上传用户标签关系")
    @PostMapping("/config/uploadIndicator")
    APIResponse<TagResponse> uploadIndicator(@RequestBody APIRequest<TagImportRequest> request) throws Exception;

    @ApiOperation("保存角色标签权限")
    @PostMapping("/config/savePermissionByRole")
    APIResponse<String> savePermissionByRole(@RequestBody APIRequest<TagPermissionRequest> request) throws Exception;

    @ApiOperation("查询标签组信息")
    @PostMapping("/query/getTagCategoryById")
    APIResponse<TagCategoryResponse> getTagCategoryById(@RequestBody APIRequest<TagIdRequest> request) throws Exception;

    @ApiOperation("查询标签组树信息")
    @PostMapping("/query/getTagCategoryByPid")
    APIResponse<List<TagTreeResponse>> getTagCategoryList(@RequestBody APIRequest<TagPIdRequest> request)
            throws Exception;

    @ApiOperation("查询标签组父类信息")
    @PostMapping("/query/getTagCategoryParent")
    APIResponse<SearchResult<TagCategoryResponse>> getTagCategoryParentList(
            @RequestBody APIRequest<TagPageRequest> request) throws Exception;

    @ApiOperation("查询标签信息")
    @PostMapping("/query/getTagInfoById")
    APIResponse<TagInfoResponse> getTagInfoById(@RequestBody APIRequest<TagIdRequest> request) throws Exception;

    @ApiOperation("查询标签列表信息")
    @PostMapping("/query/getTagList")
    APIResponse<SearchResult<TagInfoResponse>> getTagList(@RequestBody APIRequest<TagPageRequest> request)
            throws Exception;

    @ApiOperation("查询用户标签关系列表")
    @PostMapping("/query/getIndicatorList")
    APIResponse<SearchResult<TagIndicatorResponse>> getIndicatorList(
            @RequestBody APIRequest<TagIndicatorQueryRequest> request) throws Exception;

    @ApiOperation("查询用户标签关系列表")
    @PostMapping("/query/getTagInfoByUserid")
    APIResponse<SearchResult<TagInfoIndicatorResponse>> getTagInfoByUserid(
            @RequestBody APIRequest<TagUserPageRequest> request) throws Exception;

    @ApiOperation("查询标签子树")
    @PostMapping("/query/getTree")
    APIResponse<Object> getTree(@RequestBody APIRequest<TagTIdRequest> request) throws Exception;

    @ApiOperation("查询标签树补充信息")
    @PostMapping("/query/getExtraInfo")
    APIResponse<Object> getExtraInfo(@RequestBody APIRequest<TagExtranInfoRequest> request) throws Exception;

    @ApiOperation("查询标签信息")
    @PostMapping("/query/selectByName")
    APIResponse<List<TagInfoResponse>> selectByName(@RequestBody APIRequest<TagNameRequest> request) throws Exception;

    @ApiOperation("查询标签子类信息")
    @PostMapping("/query/findChildTagInfoId")
    APIResponse<List<Long>> findChildTagInfoId(@RequestBody APIRequest<TagIdRequest> request) throws Exception;

    @ApiOperation("筛选标签信息")
    @PostMapping("/query/selectByCondition")
    APIResponse<List<TagIndicatorResponse>> selectByCondition(
            @RequestBody APIRequest<TagIndicatorConditionPageRequest> request) throws Exception;

    @ApiOperation("筛选标签信息总数")
    @PostMapping("/query/countByCondition")
    APIResponse<Long> countByCondition(@RequestBody APIRequest<TagIndicatorConditionPageRequest> request)
            throws Exception;

    @ApiOperation("根据标签组和父标签名称查询标签列表")
    @PostMapping("/query/selectByCategoryNameAndPTagName")
    APIResponse<List<TagInfoResponse>> selectByCategoryNameAndPTagName(
            @RequestBody APIRequest<TagCategoryNameAndPTagNameRequest> request);

    @ApiOperation("根据标签ID和用户ID查询用户标签关系")
    @PostMapping("/query/selectByTagIdAndUserId")
    APIResponse<TagIndicatorResponse> selectByTagIdAndUserId(@RequestBody APIRequest<TagExtranInfoRequest> request);

    @ApiOperation("根据角色ID查询标签权限")
    @PostMapping("/query/selectCheckedCategoryByRole")
    APIResponse<TagCheckedCategoryResponse> selectCheckedCategoryByRole(
            @RequestBody APIRequest<TagCheckedCategoryRequest> request);

    @ApiOperation("根据用户ID查询标签名称和用户状态")
    @PostMapping("/query/selectTagAndUserStatus")
    APIResponse<TagUserStatusResponse> selectTagAndUserStatus(@RequestBody APIRequest<GetUserListRequest> request) throws Exception;

    @ApiOperation("在yubikey marketing中添加用户")
    @PostMapping("/yubikeyMarketing/addUsers")
    APIResponse<Boolean> addUsersUnderYubikey(@RequestBody APIRequest<List<Long>> request) throws Exception;

    @ApiOperation("查询用户是否具有指定标签")
    @PostMapping("/yubikey/userIdMatchTag")
    APIResponse<Boolean> isUserIdMatchTag(@Validated  @RequestBody APIRequest<YubikeyMarketingRequest> request) throws Exception;

    @ApiOperation("批量增加用户标签关系")
    @PostMapping("/batch/userIdTagForDaily")
    APIResponse<List<SaveBatchAddUserTag>> batchAddUserIdTagForDaily(@Validated @RequestBody APIRequest<List<SaveBatchAddUserTag>> request) throws Exception;

    @ApiOperation("批量删除用户标签关系")
    @PostMapping("/batch/userIdMatchTag")
    APIResponse<List<String>> batchRemoveIndicator(@RequestBody APIRequest<Map<String,Long>> uIdAndTags) throws Exception;
}
