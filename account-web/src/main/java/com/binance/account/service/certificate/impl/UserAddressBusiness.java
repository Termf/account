package com.binance.account.service.certificate.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.query.SearchResult;
import com.binance.account.common.query.UserAddressQuery;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.UserAddress;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.UserAddressMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.IUseRiskRating;
import com.binance.account.service.certificate.IUserAddress;
import com.binance.account.service.certificate.IUserCertificate;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.file.IFileStorage;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.security.request.UserIdRequest;
import com.binance.account.vo.user.UserAddressVo;
import com.binance.account.vo.user.UserKycApproveVo;
import com.binance.account.vo.user.request.AddressAuditRequest;
import com.binance.account.vo.user.request.UserAddressChangeStatusRequest;
import com.binance.account.vo.user.request.UserAddressRequest;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.SysType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.old.models.sys.SysConfig;
import com.binance.master.utils.DateUtils;
import com.binance.master.web.handlers.MessageHelper;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Log4j2
@Service
public class UserAddressBusiness implements IUserAddress {

    private static final String IMAGE_PATH = "/ADDRESS_IMG";
    private static final int MAX_DAILY_SUBMIT_LIMIT = 3;
    // 最大文件大小5M
    private static final long MAXSIZE = 5242880;

    @Resource
    private UserAddressMapper userAddressMapper;

    @Resource
    private UserIndexMapper userIndexMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private UserCommonBusiness userCommonBusiness;

    @Resource
    private UserKycMapper userKycMapper;

    @Resource
    private IFileStorage imageStorage;
    
    @Autowired
    private UserKycBusiness userKycBusiness;
    
    @Autowired
    private IUserCertificate iUserCertificate;
    
    @Resource
    private IMsgNotification iMsgNotification;

    @Resource
    protected ISysConfig iSysConfig;
    
    @Autowired
    private MessageHelper messageHelper;
    
    @Resource
    protected IUseRiskRating iUseRiskRating;

    @Override
    public APIResponse<SearchResult<UserAddressVo>> getList(@RequestBody() APIRequest<UserAddressQuery> request) {

        UserAddressQuery userAddressQuery = request.getBody();
        if (StringUtils.isNotBlank(userAddressQuery.getEmail())) {
            User user = userMapper.queryByEmail(userAddressQuery.getEmail());
            if (user != null) {
                userAddressQuery.setUserId(user.getUserId());
            } else {
                return APIResponse.getOKJsonResult(null);
            }
        }

        List<UserAddressVo> userAddressVos = new ArrayList<>();
        List<UserAddress> addressList = userAddressMapper.getList(userAddressQuery);
        if (addressList != null && !addressList.isEmpty()) {
            // 获得UserId <-> Email 映射
            Set<Long> userIds = addressList.stream().map(UserAddress::getUserId).collect(Collectors.toSet());
            List<UserIndex> userIndices = userIndexMapper.selectByUserIds(userIds);
            Map<Long, String> userEmailMapping = userIndices.stream().collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));

            for (UserAddress address : addressList) {
                UserAddressVo userAddressVo = new UserAddressVo();
                userAddressVo.setId(address.getId());
                userAddressVo.setUserId(address.getUserId());
                userAddressVo.setEmail(userEmailMapping.get(address.getUserId()));
                userAddressVo.setStatus(UserAddressVo.Status.valueOf(String.valueOf(address.getStatus())));

                userAddressVo.setFirstName(address.getCheckFirstName());
                userAddressVo.setLastName(address.getCheckLastName());
                userAddressVo.setAddressFile(address.getAddressFile());
                userAddressVo.setStreetAddress(address.getStreetAddress());
                userAddressVo.setPostalCode(address.getPostalCode());
                userAddressVo.setCity(address.getCity());
                userAddressVo.setCountry(address.getCountry());

                userAddressVo.setSubmitCountDay(address.getDaySubmitCount());
                userAddressVo.setFailReason(address.getFailReason());
                userAddressVo.setSourceOfFund(address.getSourceOfFund());
                userAddressVo.setEstimatedTradeVolume(address.getEstimatedTradeVolume());
                userAddressVo.setApprover(address.getApprover());
                userAddressVo.setApproveTime(address.getApproveTime());
                userAddressVo.setCreateTime(address.getCreateTime());
                userAddressVo.setUpdateTime(address.getUpdateTime());
                userAddressVos.add(userAddressVo);
            }
        }

        SearchResult<UserAddressVo> searchResult = new SearchResult<>();
        searchResult.setRows(userAddressVos);
        searchResult.setTotal(userAddressMapper.getListCount(userAddressQuery));
        return APIResponse.getOKJsonResult(searchResult);
    }

    @Override
    public APIResponse<?> audit(@RequestBody() APIRequest<AddressAuditRequest> request) {
        AddressAuditRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();

        UserAddress userAddress = userAddressMapper.getById(userId, requestBody.getId());
        if (userAddress == null) {
            return APIResponse.getErrorJsonResult("操作失败！当前记录不存在");
        }

        User user = new User();
        user.setUserId(userId);
        user.setEmail(userIndexMapper.selectEmailById(userId));

        SysConfig riskRatingConfig = this.iSysConfig.selectByDisplayName("risk_rating_switch");
        if (riskRatingConfig != null && "ON".equalsIgnoreCase(riskRatingConfig.getCode())) {
            //判断风险等级。如果过高，则改变状态为拒绝
            if (requestBody.getStatus() == UserAddressVo.Status.PASSED && iUseRiskRating.checkRiskRating(userId, userAddress)) {
                requestBody.setStatus(UserAddressVo.Status.REFUSED);
                requestBody.setFailReason("We are unable to provide you with our services. We apologise for the inconvenience.");
            }
        }
        if (requestBody.getStatus() == UserAddressVo.Status.PASSED) {
            // 通过
            if (userAddress.getStatus() != UserAddress.Status.PENDING) {
                return APIResponse.getErrorJsonResult("操作失败！状态错误，请刷新后重试");
            }
            if (StringUtils.isBlank(userAddress.getAddressFile())) {
                return APIResponse.getErrorJsonResult("操作失败！缺少地址认证文件");
            }

            // 姓名有一个， 国家、城市、街道
            if (StringUtils.isAnyBlank(userAddress.getCheckFirstName(), userAddress.getCheckLastName(),
                    userAddress.getCountry(), userAddress.getCity(), userAddress.getStreetAddress())) {
                return APIResponse.getErrorJsonResult("操作失败！当前记录缺失审核信息，请直接拒绝");
            }
            log.info(String.format("通过用户地址认证:%s", requestBody.getUserId()));

            // 把之前通过和待审核的都标记成取消，只保留这个
            userAddressMapper.cancelPendingAndPassedExcept(userId, userAddress.getId());

            userAddress.setApprover(requestBody.getApprover());
            userAddress.setApproveTime(DateUtils.getNewUTCDate());
            if (StringUtils.isNotBlank(requestBody.getFailReason())) {
                userAddress.setFailReason(requestBody.getFailReason());
            }

            //法币逻辑为如果地址认证通过，kyc通过，则提升用户等级
            this.updateUserSecLevel(userId);

            // 更新身份信息USER_CERTIFICATION?

            // 发送邮件
            userCommonBusiness.sendEmailWithoutRequest(AccountConstants.USER_ADDRESS_VERIFIED_MAIL_TEMPLATE, user, null, "用户地址认证成功邮件", LanguageEnum.EN_US);
        } else if (requestBody.getStatus() == UserAddressVo.Status.REFUSED) {
            // 拒绝
            if (userAddress.getStatus() != UserAddress.Status.PENDING) {
                return APIResponse.getErrorJsonResult("操作失败！状态错误，请刷新后重试");
            }

            if (StringUtils.isNotBlank(requestBody.getFailReason())) {
                userAddress.setFailReason(requestBody.getFailReason());
            }
            userAddress.setApprover(requestBody.getApprover());
            userAddress.setApproveTime(DateUtils.getNewUTCDate());

            log.info(String.format("拒绝用户地址认证:%s", requestBody.getUserId()));

            // 发送邮件
            Map<String, Object> templateData = new HashMap<>();
            templateData.put("fail_reason", requestBody.getFailReason());
            userCommonBusiness.sendEmailWithoutRequest(AccountConstants.USER_ADDRESS_REJECT_MAIL_TEMPLATE, user, templateData, "拒绝用户地址认证邮件", LanguageEnum.EN_US);
        } else if (requestBody.getStatus() == UserAddressVo.Status.PENDING) {
            // 重置为待人工确认状态
            if (userAddress.getStatus() != UserAddress.Status.PASSED &&
                    userAddress.getStatus() != UserAddress.Status.REFUSED &&
                    userAddress.getStatus() != UserAddress.Status.CANCELLED) {
                return APIResponse.getErrorJsonResult("操作失败！状态错误，请刷新后重试");
            }
            log.info(String.format("重置用户地址认证:%s", requestBody.getUserId()));
        } else {
            log.error("提交的地址认证审核状态有误:{}", requestBody.getStatus());
            return APIResponse.getErrorJsonResult("操作失败！提交的审核状态有误");
        }

        userAddress.setStatus(UserAddress.Status.valueOf(requestBody.getStatus().name()));
        userAddress.setUpdateTime(new Date());

        userAddressMapper.updateStatus(userAddress);
        return APIResponse.getOKJsonResult(null);
    }

    @Override
    public APIResponse<?> submit(@RequestBody() APIRequest<UserAddressRequest> request) {
        UserAddressRequest requestBody = request.getBody();
        Long userId = requestBody.getUserId();
        if (requestBody.getUploadFile() == null || requestBody.getUploadFile().length == 0) {
            return APIResponse.getErrorJsonResult("请上传地址认证文件");
        }

        if (requestBody.getUploadFile().length > MAXSIZE) {
            return APIResponse.getErrorJsonResult(messageHelper.getMessage(AccountErrorCode.SUBMIT_ADDRESS_FILE_LARGE));
        }

        // check KYC status（one-in-two kyc update）
        UserKyc userKyc = userKycMapper.getLast(userId);
        if (userKyc == null || KycStatus.delete == userKyc.getStatus() || userKyc.getBaseInfo() == null ||
                StringUtils.isAllBlank(userKyc.getBaseInfo().getFirstName(), userKyc.getBaseInfo().getLastName())) {
            return APIResponse.getErrorJsonResult("请先提交身份认证");
        }

        // check throttling rate
        UserAddressQuery addressQuery = new UserAddressQuery();
        addressQuery.setUserId(userId);
        addressQuery.setStartCreateTime(DateUtils.getNewDateAddDay(-1));
        int daySubmitCount = userAddressMapper.getListCount(addressQuery);
        if (daySubmitCount >= MAX_DAILY_SUBMIT_LIMIT) {
            throw new BusinessException(GeneralCode.USER_KYC_UPLOAD_EXCEED_LIMIT_TODAY);
        }
        // checked any passed record
        addressQuery.setStatus(String.valueOf(UserAddress.Status.PASSED.ordinal()));
        int passedCount = userAddressMapper.getListCount(addressQuery);
        if (passedCount > 0) {
            return APIResponse.getErrorJsonResult("身份审核已通过");
        }

        // save the photo to FTP
        byte[] bytes = requestBody.getUploadFile();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        StringBuilder sb = new StringBuilder();
        sb.append(IMAGE_PATH).append(sdf.format(new Date())).append("/");
        String path = sb.toString();
        String fileExt = FilenameUtils.getExtension(requestBody.getOriginalFileName());

        String addressPath = String.format("%s%s_address_%d.%s", path, userId, new Random().nextInt(10000000), fileExt);

        try {
            imageStorage.save(bytes, addressPath);
        } catch (Exception e) {
            log.error("上传地址认证失败:{}, error={}", userId, e);
            return APIResponse.getErrorJsonResult("上传地址认证文件失败");
        }

        // 如果填了 地址信息，更新user_kyc表的baseInfo
        if (!StringUtils.isAnyBlank(requestBody.getStreet(), requestBody.getCity(), requestBody.getCountry(), requestBody.getPostalCode())) {
            UserKyc.BaseInfo baseInfo = userKyc.getBaseInfo();
            baseInfo.setAddress(requestBody.getStreet());
            baseInfo.setCity(requestBody.getCity());
            baseInfo.setCountry(requestBody.getCountry());
            baseInfo.setPostalCode(requestBody.getPostalCode());
            userKycMapper.updateByPrimaryKeySelective(userKyc);
        }

        // insert user address object
        UserAddress userAddress = new UserAddress();
        userAddress.setUserId(userId);
        UserIdRequest kycRequest = new UserIdRequest();
        kycRequest.setUserId(userId);
        APIResponse<UserKycApproveVo> response = userKycBusiness.getApproveUser(APIRequest.instance(kycRequest));
        if (response.getData() != null) {//kyc通过，pending状态
            userAddress.setStatus(UserAddress.Status.PENDING);  
        } else {//未通过，等待kyc通过状态
            userAddress.setStatus(UserAddress.Status.WAITING);
        }
        userAddress.setCheckFirstName(userKyc.getBaseInfo().getFirstName());
        userAddress.setCheckLastName(userKyc.getBaseInfo().getLastName());
        userAddress.setCountry(userKyc.getBaseInfo().getCountry());
        userAddress.setCity(userKyc.getBaseInfo().getCity());
        userAddress.setStreetAddress(userKyc.getBaseInfo().getAddress());
        userAddress.setPostalCode(userKyc.getBaseInfo().getPostalCode());
        userAddress.setDaySubmitCount(daySubmitCount + 1);
        userAddress.setAddressFile(addressPath);
        userAddress.setSourceOfFund(requestBody.getSourceOfFund());
        userAddress.setEstimatedTradeVolume(requestBody.getEstimatedTradeVolume());

        userAddress.setCreateTime(DateUtils.getNewUTCDate());
        // set previous to disabled
        if (userAddressMapper.insert(userAddress) > 0) {
            return APIResponse.getOKJsonResult(null);
        }
        return APIResponse.getErrorJsonResult("提交地址认证文件失败");
    }
    
    private void updateUserSecLevel(Long userId) {
        UserIdRequest request = new UserIdRequest();
        request.setUserId(userId);
        APIResponse<UserKycApproveVo> response = userKycBusiness.getApproveUser(APIRequest.instance(request));
        if (response.getData() != null) {
            iUserCertificate.updateSecurityLevel(userId, 2);

            // 修改用户等级消息通知 start
            Map<String, Object> dataMsg = new HashMap<>();
            dataMsg.put("userId", userId);
            dataMsg.put("level", 2);
            MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.SECURITY_LEVEL, dataMsg);
            log.info("iMsgNotification security level 2:{}", JSON.toJSONString(msg));
            this.iMsgNotification.send(msg);
        }

    }

    /**
     * 如果地址免审核白名单内国家，添加地址认证数据
     * @return 白名单命中
     *
     */
    public boolean checkCountryWhitelist(UserKyc userKyc, String jumioCountry) {
        SysConfig addressVerificationConfig = this.iSysConfig.selectByDisplayName("address_verification_switch");
        if (addressVerificationConfig != null && "ON".equalsIgnoreCase(addressVerificationConfig.getCode())) {
            UserAddressQuery userAddressQuery = new UserAddressQuery();
            userAddressQuery.setUserId(userKyc.getUserId());
            if (userAddressMapper.getListCount(userAddressQuery) > 0) {
                log.info("用户已经提交了地址认证，白名单不能跳过");
                return false;
            }

            SysConfig addressFreeCountries = this.iSysConfig.selectByDisplayName("address_verification_free_country");
            if (addressFreeCountries != null && addressFreeCountries.getCode() != null) {
                String[] countries = addressFreeCountries.getCode().split(";");
                boolean found = false;
                for (String country : countries) {
                    if (country.equalsIgnoreCase(jumioCountry)) {
                        found = true;
                        break;
                    }
                }
                if (found) {
                    log.info("国家在免地址审核列表, country={}, userId={}", jumioCountry, userKyc.getUserId());
                    UserAddress userAddress = new UserAddress();
                    userAddress.setUserId(userKyc.getUserId());
                    userAddress.setStatus(UserAddress.Status.PASSED);
                    userAddress.setCreateTime(DateUtils.getNewUTCDate());

                    if (userKyc.getBaseInfo() != null) {
                        userAddress.setCheckFirstName(userKyc.getBaseInfo().getFirstName());
                        userAddress.setCheckLastName(userKyc.getBaseInfo().getLastName());
                        userAddress.setCountry(userKyc.getBaseInfo().getCountry());
                        userAddress.setCity(userKyc.getBaseInfo().getCity());
                        userAddress.setStreetAddress(userKyc.getBaseInfo().getAddress());
                        userAddress.setPostalCode(userKyc.getBaseInfo().getPostalCode());
                    }
                    userAddress.setDaySubmitCount(1);
                    userAddress.setAddressFile("");
                    userAddress.setApprover("system");
                    userAddress.setFailReason("免审核自动通过");
                    userAddress.setApproveTime(DateUtils.getNewUTCDate());
                    return this.userAddressMapper.insert(userAddress) > 0;
                }
            }
        }
        return false;
    }
   
    /**
     * 如果kyc通过，则更新待kyc通过状态为pending状态
     * @return 白名单命中
     *
     */
    public void updateWaitingToPending(Long userId) {
        UserAddress userAddress = userAddressMapper.getLast(userId, UserAddress.Status.WAITING.ordinal());
        if(null != userAddress) {
            userAddress.setStatus(UserAddress.Status.PENDING); 
            userAddressMapper.updateStatus(userAddress);
        }
        
    }

    @Override
    public void updatePassedToExpired(UserAddressChangeStatusRequest request) {
        Long userId = request.getUserId();
        UserAddress userAddress = userAddressMapper.getLast(userId, UserAddress.Status.PASSED.ordinal());
        if (null != userAddress) {
            if (StringUtils.isNotBlank(request.getFailReason())) {
                userAddress.setFailReason(request.getFailReason());
            }
            userAddress.setStatus(UserAddress.Status.EXPIRED);
            userAddress.setUpdateTime(DateUtils.getNewUTCDate());
            userAddressMapper.updateStatus(userAddress);
        }else {
            throw new BusinessException(GeneralCode.SYS_ERROR, "获取用户通过的地址验证失败");
        }
    }
}
