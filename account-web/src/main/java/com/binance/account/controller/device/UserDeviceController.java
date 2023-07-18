package com.binance.account.controller.device;

import com.alibaba.fastjson.JSON;
import com.binance.account.api.UserDeviceApi;
import com.binance.account.common.constant.UserDeviceConst;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.validator.ValidateResult;
import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.device.UserDeviceHistory;
import com.binance.account.data.entity.device.UserDeviceProperty;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.device.IUserDevice;
import com.binance.account.service.device.impl.UserDeviceSearchService;
import com.binance.account.vo.device.request.CheckNewDeviceIpRequest;
import com.binance.account.vo.device.request.CompareDeviceRequestV2;
import com.binance.account.vo.device.request.FindMostSimilarUserDeviceRequest;
import com.binance.account.vo.device.request.IDRequest;
import com.binance.account.vo.device.request.ResendAuthorizeDeviceEmailRequest;
import com.binance.account.vo.device.request.UserDeviceAuthorizeRequest;
import com.binance.account.vo.device.request.UserDeviceDeleteRequest;
import com.binance.account.vo.device.request.UserDeviceHistoryQueryRequest;
import com.binance.account.vo.device.request.UserDeviceListRequest;
import com.binance.account.vo.device.request.UserDevicePropertyRequest;
import com.binance.account.vo.device.request.UserDeviceRequest;
import com.binance.account.vo.device.request.VerifyAuthDeviceCodeRequest;
import com.binance.account.vo.device.request.*;
import com.binance.account.vo.device.response.AddUserDeviceForQRCodeLoginResponse;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.device.response.CheckNewDeviceIpResponse;
import com.binance.account.vo.device.response.CheckUserDeviceResponse;
import com.binance.account.vo.device.response.CheckWithdrawDeviceResponse;
import com.binance.account.vo.device.response.FindMostSimilarUserDeviceResponse;
import com.binance.account.vo.device.response.ResendAuthorizeDeviceEmailResponse;
import com.binance.account.vo.device.response.UserDeviceAuthorizeResponse;
import com.binance.account.vo.device.response.UserDeviceHistoryVo;
import com.binance.account.vo.device.response.UserDevicePropertyResponse;
import com.binance.account.vo.device.response.UserDeviceVo;
import com.binance.account.vo.security.request.IdLongRequest;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.RedisCacheUtils;
import com.binance.master.validator.groups.Add;
import com.binance.master.validator.groups.Edit;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
public class UserDeviceController implements UserDeviceApi {

    @Autowired
    private IUserDevice iUserDevice;
    @Autowired
    private UserDeviceSearchService deviceSearchService;

    private static final String CACHE_USER_DEVICE_CONFIRM_RESULT = "USER_DEVICE_CONFIRM_RESULT_%s";

    @Override
    public APIResponse<AddUserDeviceResponse> addDevice(@Validated @RequestBody APIRequest<UserDeviceRequest> request) {
        UserDeviceRequest body = request.getBody();
        ValidateResult rs = body.validate();
        if (!rs.isOk()){
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), rs.getMessage());
        }
        AddUserDeviceResponse response;
        UserDevice.Status status = body.getStatus() == null ?
                UserDevice.Status.AUTHORIZED : UserDevice.Status.values()[body.getStatus()];
        if (UserDeviceConst.SOURCE_WITHDRAW.equals(body.getSource())){
            response = iUserDevice.addDeviceHistoryForWithdraw(body.getUserId(), body.getAgentType(), body.getContent());
        }else {
            response = iUserDevice.addDevice(body.getUserId(), body.getAgentType(), status, body.getSource(), body.getContent());
        }

        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<AddUserDeviceResponse> associateSensitiveDevice(@Validated @RequestBody APIRequest<UserDeviceRequest> request) {
        UserDeviceRequest body = request.getBody();
        ValidateResult rs = body.validate();
        if (!rs.isOk()){
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), rs.getMessage());
        }
        AddUserDeviceResponse response = iUserDevice.associateSensitiveDevice(body.getUserId(), body.getAgentType(), body.getSource(), body.getContent());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<CheckUserDeviceResponse> checkDevice(@Validated @RequestBody APIRequest<UserDeviceRequest> request) {
        UserDeviceRequest body = request.getBody();
        ValidateResult ck = body.validate();
        if (!ck.isOk()){
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), ck.getMessage());
        }
        CheckUserDeviceResponse rs = iUserDevice.checkDevice(body.getUserId(), body.getAgentType(), body.getContent());
        if (rs.isValid()){
            return APIResponse.getOKJsonResult(rs);
        }else {
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), "当前设备未授权");
        }
    }

    @Override
    public APIResponse<CheckWithdrawDeviceResponse> checkWithdrawDevice(@RequestBody APIRequest<UserDeviceRequest> request) {
        UserDeviceRequest body = request.getBody();
        ValidateResult rs = body.validate();
        if (!rs.isOk()){
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), rs.getMessage());
        }
        CheckWithdrawDeviceResponse response = iUserDevice.checkWithdrawDevice(body.getUserId(), body.getAgentType() ,body.getContent());
        return APIResponse.getOKJsonResult(response);
    }

    @Override
    public APIResponse<UserDeviceAuthorizeResponse> authorizeDevice(@Validated @RequestBody APIRequest<UserDeviceAuthorizeRequest> request) {
        UserDeviceAuthorizeRequest body = request.getBody();
        // 先从缓冲中取结果，解决重复点击的问题
        String cacheKey = String.format(CACHE_USER_DEVICE_CONFIRM_RESULT, body.getCode());
        UserDeviceAuthorizeResponse cache = RedisCacheUtils.get(cacheKey, UserDeviceAuthorizeResponse.class);
        if (cache!=null){
            log.info("authorizeDevice have been done, code:{}, data:{}", body.getCode(), JSON.toJSONString(cache));

            if(!iUserDevice.checkIfPassLoginYubikey(cache.getUserId())){
                throw new BusinessException(AccountErrorCode.PLEASE_VERFIY_YUBIKEY_FIRST);
            }
            cache.setRetry(true);
            return APIResponse.getOKJsonResult(cache);
        }

        UserDeviceAuthorizeResponse rs = iUserDevice.authorizeDevice(request);
        if (rs.isValid()) {
            RedisCacheUtils.set(cacheKey, rs, Constant.HOUR_HALF);
            return APIResponse.getOKJsonResult(rs);
        } else if (rs.isNeedAnswerQuestion()) {
            return APIResponse.getOKJsonResult(rs);
        } else {
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL, GeneralCode.SYS_VALID.getCode(), "授权失败，链接已过期");
        }
    }

    @Override
    public APIResponse<List<UserDeviceVo>> listDevice(@Validated @RequestBody APIRequest<UserDeviceListRequest> request) {
        UserDeviceListRequest body = request.getBody();
        List<UserDevice> list = iUserDevice.listDevice(body.getUserId(), body.getAgentType(), body.getStatus(),
                body.getSource(), body.getExcludeSource(), body.isShowDeleted(), null, null);
        if (CollectionUtils.isNotEmpty(list)) {
            List<UserDeviceVo> userDeviceVos = new ArrayList<>(list.size());
            for (UserDevice userDevice : list) {
                UserDeviceVo userDeviceVo = new UserDeviceVo();
                BeanUtils.copyProperties(userDevice, userDeviceVo);
                userDeviceVo.setStatus(userDevice.getStatus().ordinal());
                userDeviceVos.add(userDeviceVo);
            }
            return APIResponse.getOKJsonResult(userDeviceVos);
        }
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<SearchResult<UserDeviceVo>> pageDevice(@Validated @RequestBody APIRequest<UserDeviceListRequest> request) {
        SearchResult<UserDeviceVo> result = new SearchResult<>();
        UserDeviceListRequest body = request.getBody();
        try {
            List<UserDevice> userDevices = iUserDevice.listDevice(body.getUserId(), body.getAgentType(),
                    body.getStatus(), body.getSource(), body.getExcludeSource(), body.isShowDeleted(), body.getOffset(), body.getLimit());
            Long count = iUserDevice.countDevice(body.getUserId(), body.getAgentType(),
                    body.getStatus(), body.getSource(), body.getExcludeSource(), body.isShowDeleted());
            List<UserDeviceVo> userDeviceVos = new ArrayList<>();
            if(!userDevices.isEmpty()) {
                for (UserDevice log : userDevices) {
                    UserDeviceVo logVo = new UserDeviceVo();
                    BeanUtils.copyProperties(log, logVo);
                    userDeviceVos.add(logVo);
                }
            }
            result.setTotal(count);
            result.setRows(userDeviceVos);
            return APIResponse.getOKJsonResult(result);
        } catch (Exception e) {
            log.error("pageDevice error-->",e);
            return APIResponse.getErrorJsonResult(APIResponse.Type.GENERAL,e);
        }
    }

    @Override
    public APIResponse<SearchResult> listDeviceForAdmin(@RequestBody APIRequest<UserDeviceListRequest> request) {
        UserDeviceListRequest body = request.getBody();
        SearchResult<UserDevice> result = new SearchResult<>();
        if (deviceSearchService.isSearchSwitchOn()){
            result = deviceSearchService.listDevice(body.getUserId(), body.getAgentType(), body.getSource(), body.isShowDeleted(), body.getSearchParams());
        } else {
            if (body.getUserId()==null){
                return APIResponse.getErrorJsonResult("userId cannot be null");
            }
            List<UserDevice> list = iUserDevice.listDevice(body.getUserId(), body.getAgentType(), body.getStatus(), body.getSource(), body.getExcludeSource(), body.isShowDeleted(), null, null);
            result.setRows(list);
            result.setTotal(list.size());
        }
        return APIResponse.getOKJsonResult(result);
    }

    @Override
    public APIResponse<UserDeviceVo> getDevice(@Validated @RequestBody APIRequest<IdLongRequest> request) {
        IdLongRequest body = request.getBody();
        UserDevice userDevice = iUserDevice.getDevice(body.getUserId(), body.getId());

        if (userDevice != null) {
            UserDeviceVo userDeviceVo = new UserDeviceVo();
            BeanUtils.copyProperties(userDevice, userDeviceVo);
            return APIResponse.getOKJsonResult(userDeviceVo);
        }
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<List<UserDeviceHistoryVo>> listHistory(@Validated @RequestBody APIRequest<UserDeviceHistoryQueryRequest> request) {
        UserDeviceHistoryQueryRequest body = request.getBody();
        List<UserDeviceHistory> list = iUserDevice.listDeviceHistory(body.getUserId(), body.getDevicePk());
        if (CollectionUtils.isNotEmpty(list)) {
            List<UserDeviceHistoryVo> userDeviceHistoryVos = new ArrayList<>(list.size());
            for (UserDeviceHistory userDeviceHistory : list) {
                UserDeviceHistoryVo userDeviceHistoryVo = new UserDeviceHistoryVo();
                BeanUtils.copyProperties(userDeviceHistory, userDeviceHistoryVo);
                userDeviceHistoryVos.add(userDeviceHistoryVo);
            }
            return APIResponse.getOKJsonResult(userDeviceHistoryVos);
        }
        return APIResponse.getOKJsonResult();
    }

    @Override
    public APIResponse<Void> deleteDevice(@Validated @RequestBody APIRequest<UserDeviceDeleteRequest> request) {
        log.info("删除设备:{}", request.toString());
        iUserDevice.deleteDevices(request);
        return APIResponse.getOKJsonResult(null);
    }

    @Override
    public APIResponse<?> listDeviceProperties(@RequestBody APIRequest<UserDevicePropertyRequest> request) {
        UserDevicePropertyRequest body = request.getBody();
        List<UserDeviceProperty> list = iUserDevice.getDevicePropertyConfig(body.getAgentType(), body.getStatus());
        List<UserDevicePropertyResponse> propertyResponses = new ArrayList<>(list.size());
        for (UserDeviceProperty property:list){
            propertyResponses.add(buildDevicePropertyResponse(property));
        }
        return APIResponse.getOKJsonResult(propertyResponses);
    }

    @Override
    public APIResponse<?> addDeviceProperty(@Validated(value = Add.class) @RequestBody APIRequest<UserDevicePropertyRequest> request) {
        Long id = iUserDevice.addDeviceProperty(buildDeviceProperty(request.getBody()));
        return APIResponse.getOKJsonResult(id);
    }

    @Override
    public APIResponse<?> editDeviceProperty(@Validated(value = Edit.class) @RequestBody APIRequest<UserDevicePropertyRequest> request) {
        iUserDevice.editDeviceProperty(buildDeviceProperty(request.getBody()));
        return APIResponse.getOKJsonResult(null);
    }

    @Override
    public APIResponse<?> deleteDeviceProperty(@Validated @RequestBody APIRequest<IDRequest> request) {
        iUserDevice.deleteDeviceProperty(request.getBody().getId());
        return APIResponse.getOKJsonResult(null);
    }

    @Override
    public APIResponse<?> specialCache() {
        return APIResponse.getOKJsonResult(iUserDevice.getSpecialUserAuthList());
    }

    @Override
    public APIResponse<?> getSwitch() {
        return APIResponse.getOKJsonResult("on");
    }

    @Override
    public APIResponse<List> listRelation(Long devicePk) {
        return APIResponse.getOKJsonResult(iUserDevice.listRelation(devicePk));
    }

    private UserDeviceProperty buildDeviceProperty(UserDevicePropertyRequest userDevicePropertyRequest){
        UserDeviceProperty property = new UserDeviceProperty();
        property.setId(userDevicePropertyRequest.getId());
        property.setAgentType(userDevicePropertyRequest.getAgentType());
        property.setStatus(userDevicePropertyRequest.getStatus());
        property.setPropertyKey(userDevicePropertyRequest.getPropertyKey());
        property.setPropertyName(userDevicePropertyRequest.getPropertyName());
        property.setPropertyRule(userDevicePropertyRequest.getPropertyRule());
        property.setPropertyWeight(userDevicePropertyRequest.getPropertyWeight());
        return property;
    }

    private UserDevicePropertyResponse buildDevicePropertyResponse(UserDeviceProperty property){
        UserDevicePropertyResponse response = new UserDevicePropertyResponse();
        response.setId(property.getId());
        response.setStatus(property.getStatus());
        response.setAgentType(property.getAgentType());
        response.setPropertyKey(property.getPropertyKey());
        response.setPropertyName(property.getPropertyName());
        response.setPropertyRule(property.getPropertyRule());
        response.setPropertyWeight(property.getPropertyWeight());
        return response;
    }


    @Override
    public APIResponse<FindMostSimilarUserDeviceResponse> findMostSimilarUserDevice(@Validated @RequestBody APIRequest<FindMostSimilarUserDeviceRequest> request) {
        FindMostSimilarUserDeviceRequest body = request.getBody();
        return APIResponse.getOKJsonResult(iUserDevice.findMostSimilarDevice(body.getUserId(), body.getContent(), body.getAgentType()));
    }

    @Override
    public APIResponse<Map<String, Object>> compareDeviceV2(@Validated @RequestBody APIRequest<CompareDeviceRequestV2> request) {
        return APIResponse.getOKJsonResult(JSON.parseObject(JSON.toJSONString(iUserDevice.compareV2(request.getBody().getContent1(), request.getBody().getContent2(), request.getBody().getAgentType()))));
    }

    @Override
    public APIResponse<ResendAuthorizeDeviceEmailResponse> resendAuthorizeDeviceEmail(@Validated @RequestBody APIRequest<ResendAuthorizeDeviceEmailRequest> request) throws Exception {
        ResendAuthorizeDeviceEmailResponse resendAuthorizeDeviceEmailResponse=iUserDevice.resendAuthorizeDeviceEmail(request);
        return APIResponse.getOKJsonResult(resendAuthorizeDeviceEmailResponse);
    }

    @Override
    public APIResponse<Boolean> verifyAuthDeviceCode(@Validated @RequestBody APIRequest<VerifyAuthDeviceCodeRequest> request) throws Exception {
        Boolean resp=iUserDevice.verifyAuthDeviceCode(request);
        return APIResponse.getOKJsonResult(resp);
    }

    @Override
    public APIResponse<TerminalEnum> getUserLastLoginDevice(@RequestBody @Validated APIRequest<Long> request)throws Exception{
        TerminalEnum terminal = iUserDevice.getUserLastLoginDevice(request.getBody());
       return APIResponse.getOKJsonResult(terminal);
    }

    @Override
    public APIResponse<CheckNewDeviceIpResponse> checkNewDeviceIp(@Validated @RequestBody APIRequest<CheckNewDeviceIpRequest> request) {
        CheckNewDeviceIpResponse checkNewDeviceIpResponse=iUserDevice.checkNewDeviceIp(request);
        return APIResponse.getOKJsonResult(checkNewDeviceIpResponse);
    }

    @Override
    public APIResponse<AddUserDeviceForQRCodeLoginResponse> addDeviceForQRCodeLogin(@Validated @RequestBody APIRequest<AddUserDeviceForQRCodeLoginRequest> request) {
        APIResponse<AddUserDeviceForQRCodeLoginResponse> apiResponse=iUserDevice.addDeviceForQRCodeLogin(request);
        return apiResponse;
    }
}
