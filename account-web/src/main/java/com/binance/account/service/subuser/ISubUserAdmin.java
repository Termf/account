package com.binance.account.service.subuser;

import com.binance.account.vo.subuser.request.AssetSubUserToCommonRequest;
import com.binance.account.vo.subuser.request.BindingParentSubUserReq;
import com.binance.account.vo.subuser.request.ParentUserIdReq;
import com.binance.account.vo.subuser.request.SubUserIdReq;
import com.binance.account.vo.subuser.response.ParentUserSubUsersResp;
import com.binance.account.vo.subuser.response.SubUserParentUserResp;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * Created by Fei.Huang on 2018/10/11.
 */
public interface ISubUserAdmin {

    /**
     * 开启母子账号功能
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> enableSubUserFunction(APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 关闭母子账号功能
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> disableSubUserFunction(APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 绑定母子账号
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> bindParentSubUser(APIRequest<BindingParentSubUserReq> request) throws Exception;

    /**
     * 根据子账号获取母子账号列表
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<SubUserParentUserResp> queryBySubUserId(@RequestBody() APIRequest<SubUserIdReq> request) throws Exception;

    /**
     * 根据母账号获取母子账号列表
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<ParentUserSubUsersResp> queryByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 更新子账号备注
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Boolean> updateSubUserRemark(APIRequest<SubUserIdReq> request) throws Exception;

    /**
     * 查看是否绑定有子账号并返回子账号数量
     *
     * @param request
     * @return
     * @throws Exception
     */
    APIResponse<Long> countSubUsersByParentUserId(@RequestBody() APIRequest<ParentUserIdReq> request) throws Exception;

    /**
     * 资管子账号转成普通账号
     * @param request
     * @return
     */
    APIResponse<Void> assetSubUserToCommon(APIRequest<AssetSubUserToCommonRequest> request);
}