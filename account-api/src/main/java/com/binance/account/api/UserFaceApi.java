package com.binance.account.api;

import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.TransactionFaceQuery;
import com.binance.account.vo.face.FaceFlowInitResult;
import com.binance.account.vo.face.TransactionFaceLogVo;
import com.binance.account.vo.face.request.FaceEmailRequest;
import com.binance.account.vo.face.request.FaceInitRequest;
import com.binance.account.vo.face.request.FacePcResultRequest;
import com.binance.account.vo.face.request.FaceReferenceRequest;
import com.binance.account.vo.face.request.TransFaceAuditRequest;
import com.binance.account.vo.face.request.TransFaceInitRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.face.response.FaceSdkResponse;
import com.binance.account.vo.security.request.FaceSdkVerifyRequest;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.security.response.FaceReferenceResponse;
import com.binance.account.vo.user.request.GetUserListRequest;
import com.binance.master.configs.FeignConfig;
import com.binance.master.constant.Constant;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author liliang1
 * @date 2018-12-11 15:50
 */
@FeignClient(name = Constant.ACCOUNT_WEB_SERVICE, configuration = FeignConfig.class)
@Api("用户人脸识别的相关接口")
@RequestMapping("/userFace")
public interface UserFaceApi {

    /**
     * 用于初始化WEB PC端的人脸识别初始化
     *
     * @param request
     * @return
     */
    @ApiOperation("WEB端人脸识别初始化")
    @PostMapping("/web/faceInit")
    APIResponse<FaceInitResponse> facePcInit(@Validated @RequestBody APIRequest<FaceInitRequest> request);

    /**
     * 用于初始化手机SDK端的人脸识别
     *
     * @param request
     * @return
     */
    @ApiOperation("手机SDK人脸识别初始化")
    @PostMapping("/sdk/faceInit")
    APIResponse<FaceInitResponse> faceSdkInit(@Validated @RequestBody APIRequest<FaceInitRequest> request);


    /**
     * PC 端人脸识别验证结果信息
     *
     * @param request
     * @return 返回的时成功和失败跳转的地址
     */
    @ApiOperation("WEB端人脸识别验证结果")
    @PostMapping("/web/faceVerify")
    APIResponse<String> pcFaceVerify(@Validated @RequestBody APIRequest<FacePcResultRequest> request);

    /**
     * 手机端SDK的人脸识别结果验证
     *
     * @param request
     * @return
     */
    @ApiOperation("手机SDK人脸验证")
    @PostMapping("/sdk/faceVerify")
    APIResponse<FaceSdkResponse> appFaceSdkVerify(@Validated @RequestBody APIRequest<FaceSdkVerifyRequest> request);

    /**
     * 判断当前二维码是否能用于做人脸识别
     *
     * @param request
     * @return 当返回的APIResponse是OK时认为成功，否则认为失败
     */
    @ApiOperation("查询当前QRCODE是否能操作人脸识别")
    @PostMapping("/face/qrcode/valid")
    APIResponse<Void> faceSdkQrValid(@Validated @RequestBody APIRequest<String> request);

    /**
     * 判断业务的人脸识别是否已经通过
     *
     * @param request
     * @return
     */
    @ApiOperation("查看人脸识别是否已经通过")
    @PostMapping("/face/isPassed")
    APIResponse<Boolean> isFacePassed(@Validated @RequestBody APIRequest<FaceInitRequest> request);

    /**
     * 保存或者更新用户人脸对比检查照片信息
     * @param request
     * @return
     */
    @ApiOperation("保存更新用户人脸照片对比照片")
    @PostMapping("/save/checkFaceReference")
    APIResponse<Boolean> saveUserFaceReferenceCheckImage(@Validated @RequestBody APIRequest<FaceReferenceRequest> request);

    /**
     * 提币风控的人脸识别记录信息
     * @param request
     * @return
     */
    @ApiOperation("提现风控人脸识别记录列表")
    @PostMapping("/transaction/faceLogList")
    APIResponse<SearchResult<TransactionFaceLogVo>> getTransactionFaceLogs(@Validated @RequestBody APIRequest<TransactionFaceQuery> request);

    /**
     * 重发人脸识别通知邮件
     * @param request
     * @return
     */
    @ApiOperation("重发人脸识别邮件")
    @PostMapping("/resend/faceEmail")
    APIResponse<Void> resendFaceEmail(@Validated @RequestBody APIRequest<FaceInitRequest> request);

    /**
     * 通过邮箱和类型，重发人脸识别邮件
     * @param request
     * @return
     */
    @ApiOperation("通过邮箱和类型重发人脸识别邮件")
    @PostMapping("/resend/faceEmail/byEmail")
    APIResponse<Void> resendFaceEmailByEmail(@Validated @RequestBody APIRequest<FaceEmailRequest> request);

    /**
     * 获取用户当前的人脸对比照片信息
     * @param request
     * @return
     * @throws Exception
     */
    @ApiOperation("获取用户当前Face对比照片")
    @PostMapping("/getFaceReference")
    APIResponse<FaceReferenceResponse> getUserFaceReference(@RequestBody APIRequest<UserIdRequest> request);

    /**
     * 根据userId一次获取多个用户的人脸对比照信息
     * @param request
     * @return
     */
    @ApiOperation("获取对应用户的当前对比照片信息")
    @PostMapping("/getFaceReferenceByUserIds")
    APIResponse<List<FaceReferenceResponse>> getUserFaceReferenceByUserIds(@Validated @RequestBody APIRequest<GetUserListRequest> request);

    /**
     * 根据业务编号类型信息发起人脸识别流程
     * @param request
     * @return
     */
    @ApiOperation("发起人脸识别流程")
    @PostMapping("/init/transFlow")
    APIResponse<FaceFlowInitResult> initFaceFlowByTransId(@Validated @RequestBody APIRequest<TransFaceInitRequest> request);

    /**
     * 人脸识别照片检查失败需要重新上传照片
     * @param request
     * @return
     */
    @ApiOperation("由于人脸照片不能做人脸需要重传照片")
    @PostMapping("/image/re-upload")
    APIResponse<Void> faceImageErrorRedoUpload(@Validated @RequestBody APIRequest<TransFaceInitRequest> request);

    /**
     * 后台操作：人脸识别流程进行审核
     * @param request
     * @return
     */
    @ApiOperation("人脸识别流程审核")
    @PostMapping("/trans/audit")
    APIResponse<Void> transFaceAudit(@Validated @RequestBody APIRequest<TransFaceAuditRequest> request);
}
