package com.binance.account.api;

import com.binance.account.common.query.CompanyCertificateQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserCertificateListRequest;
import com.binance.account.vo.certificate.CompanyCertificateVo;
import com.binance.account.vo.certificate.UserCertificateVo;
import com.binance.account.vo.certificate.request.JumioIdNumberUseRequest;
import com.binance.account.vo.certificate.request.SaveCompanyCertificateRequest;
import com.binance.account.vo.certificate.request.SaveUserCertificateRequest;
import com.binance.account.vo.certificate.request.UserAuditCertificateResponse;
import com.binance.account.vo.certificate.request.UserDetectCertificateRequest;
import com.binance.account.vo.certificate.response.SaveUserCertificateResponse;
import com.binance.account.vo.certificate.response.UserDetectCertificateResponse;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.request.CompanyCertificateAuditRequest;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户身份证")
@RequestMapping("/userCertificate")
public interface UserCertificateApi {

    /**
     * 2018-12-06标识为不再使用接口，老版本的个人认证审核
     * @param request
     * @return
     * @throws Exception
     */
    @Deprecated
    @ApiOperation("根据用户Id查询用户身份证信息")
    @PostMapping("/getUserCertificateByUserId")
    APIResponse<UserCertificateVo> getUserCertificateByUserId(@RequestBody APIRequest<UserIdRequest> request)
            throws Exception;

    /**
     * 2018-12-06标识为不再使用接口，老版本的个人认证审核
     * @param request
     * @return
     * @throws Exception
     */
    @Deprecated
    @ApiOperation("保存用户身份认证信息")
    @PostMapping("/saveUserCertificate")
    APIResponse<SaveUserCertificateResponse> saveUserCertificate(
            @RequestBody() APIRequest<SaveUserCertificateRequest> request) throws Exception;

    /**
     * 2018-12-06标识为不再使用接口，老版本的个人认证审核
     * @param request
     * @return
     * @throws Exception
     */
    @Deprecated
    @ApiOperation("用户身份信息上传")
    @PostMapping("/uploadUserCertificate")
    APIResponse<SaveUserCertificateResponse> uploadUserCertificate(
            @RequestBody() APIRequest<SaveUserCertificateRequest> request) throws Exception;

    /**
     * 2018-12-06标识为不再使用接口，老版本的个人认证审核
     * @param request
     * @return
     * @throws Exception
     */
    @Deprecated
    @ApiOperation("用户身份认证验证")
    @PostMapping("/userDetectCertificate")
    APIResponse<UserDetectCertificateResponse> userDetectCertificate(
            @RequestBody() APIRequest<UserDetectCertificateRequest> request) throws Exception;

    /**
     * 2018-12-06标识为不再使用接口，老版本的个人认证审核
     * @param request
     * @return
     * @throws Exception
     */
    @Deprecated
    @ApiOperation("用户身份认证审核")
    @PostMapping("/userAuditCertificate")
    APIResponse<UserAuditCertificateResponse> userAuditCertificate(
            @RequestBody() APIRequest<SaveUserCertificateRequest> request) throws Exception;

    @ApiOperation("上传企业认证信息")
    @PostMapping("/uploadCompanyCertificate")
    APIResponse<JumioTokenResponse> uploadCompanyCertificate(
            @RequestBody() APIRequest<SaveCompanyCertificateRequest> request) throws Exception;

    @ApiOperation("审核企业认证信息")
    @PostMapping("/companyAuditCertificate")
    APIResponse<?> companyAuditCertificate(
            @RequestBody() APIRequest<CompanyCertificateAuditRequest> request) throws Exception;

    @ApiOperation("获取用户企业认证信息")
    @PostMapping("/getCompanyCertificate")
    APIResponse<CompanyCertificateVo> getCompanyCertificate(
            @RequestBody() APIRequest<UserIdRequest> request) throws Exception;

    @ApiOperation(notes = "获取企业认证审核列表", nickname = "getCompanyCertificateList", value = "获取企业认证审核列表")
    @PostMapping("/getCompanyCertificateList")
    APIResponse<SearchResult<CompanyCertificateVo>> getCompanyCertificateList(@RequestBody() APIRequest<CompanyCertificateQuery> request) throws Exception;

    @ApiOperation("修改企业认证信息")
    @PostMapping("/modifyCompanyCertificate")
    APIResponse<?> modifyCompanyCertificate(
            @RequestBody() APIRequest<CompanyCertificateVo> request) throws Exception;

    @ApiOperation("验证JUMIO的证件号是否被别的用户占用")
    @PostMapping("/idnumber/haveuse")
    APIResponse<Boolean> isJumioIdNumberUseByOtherUser(APIRequest<JumioIdNumberUseRequest> request);
    
    @ApiOperation("查询用户身份信息")
    @PostMapping("/list")
    APIResponse<SearchResult<UserCertificateVo>> listUserCertificate(
            @RequestBody() APIRequest<UserCertificateListRequest> request) throws Exception;

    @ApiOperation("企业认证信息审核通过重置为失败")
    @PostMapping("/refuseCompanyCertificate")
    APIResponse<?> refuseCompanyCertificate(
            @RequestBody() APIRequest<CompanyCertificateAuditRequest> request) throws Exception;

}
