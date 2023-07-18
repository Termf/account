package com.binance.account.service.device;

import com.binance.account.data.entity.device.UserDevice;
import com.binance.account.data.entity.device.UserDeviceHistory;
import com.binance.account.data.entity.device.UserDeviceProperty;
import com.binance.account.data.entity.device.UserDeviceRelation;
import com.binance.account.data.entity.user.User;
import com.binance.account.vo.device.request.AddUserDeviceForQRCodeLoginRequest;
import com.binance.account.service.device.impl.UserDeviceComparator;
import com.binance.account.vo.device.request.AddUserDeviceForQRCodeLoginRequest;
import com.binance.account.vo.device.request.CheckNewDeviceIpRequest;
import com.binance.account.vo.device.request.ResendAuthorizeDeviceEmailRequest;
import com.binance.account.vo.device.request.UserDeviceAuthorizeRequest;
import com.binance.account.vo.device.request.UserDeviceDeleteRequest;
import com.binance.account.vo.device.request.VerifyAuthDeviceCodeRequest;
import com.binance.account.vo.device.response.AddUserDeviceForQRCodeLoginResponse;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.device.response.CheckNewDeviceIpResponse;
import com.binance.account.vo.device.response.CheckUserDeviceResponse;
import com.binance.account.vo.device.response.CheckWithdrawDeviceResponse;
import com.binance.account.vo.device.response.FindMostSimilarUserDeviceResponse;
import com.binance.account.vo.device.response.ResendAuthorizeDeviceEmailResponse;
import com.binance.account.vo.device.response.UserDeviceAuthorizeResponse;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import java.util.List;
import java.util.Map;

/**
 * 需求文档：http://confluence.fdgahl.cn/pages/viewpage.action?pageId=950761
 */
public interface IUserDevice {

    /**
     * 预校验，拒绝恶意的数据
     */
    void preCheck(Map<String, String> deviceInfo, Long userId, String agentType);
    /**
     * 校验设备指纹信息（参考文档：https://confluence.fdgahl.cn/pages/viewpage.action?pageId=950761）
     *  通过：更新设备信息
     *  不通过：发送认证邮件，申请授权新设备
     * @param userId
     * @param agentType
     * @param content
     * @return true or false, and devicePk if true
     */
    CheckUserDeviceResponse checkDevice(Long userId, String agentType, Map<String, String> content);

    /**
     * 验证提现的设备指纹信息
     */
    CheckWithdrawDeviceResponse checkWithdrawDevice(Long userId, String agentType, Map<String, String> content);

    /**
     * 新增设备
     * @param userId
     * @param agentType
     * @param source  操作来源
     * @param content
     * @return AddUserDeviceResponse
     */
    AddUserDeviceResponse addDevice(Long userId, String agentType, UserDevice.Status status, String source, Map<String, String> content);

    AddUserDeviceResponse addDevice(Long userId, String agentType, UserDevice.Status status, String source, Map<String, String> content, String flowId);

    /**
     * 记录提现时的设备历史
     */
    AddUserDeviceResponse addDeviceHistoryForWithdraw(Long userId, String agentType, Map<String, String> content);

    /**
     * 记录敏感操作时的设备
     * @param source  操作来源
     */
    AddUserDeviceResponse associateSensitiveDevice(Long userId, String agentType, String source, Map<String, String> content);

    /**
     * 验证通过设备
     * @param code      code
     * @return  授权结果
     */
    UserDeviceAuthorizeResponse authorizeDevice(APIRequest<UserDeviceAuthorizeRequest> request);

    /**
     * 删除过期的设备
     * @param userId
     */
    void clearExpiredDevice(Long userId);

    /**
     * 删除设备
     */
    void deleteDevices(APIRequest<UserDeviceDeleteRequest> request);

    /**
     * 查询设备详情
     */
    UserDevice getDevice(Long userId, Long devicePk);

    /**
     * 查询设备列表
     * @param userId user.id
     * @param agentType 终端类型
     * @param status 设备状态
     * @param source 设备来源
     * @param excludeSource 排除来源
     * @param showDeleted   是否显示已删除的设备
     * @return List<UserDevice>
     */
    List<UserDevice> listDevice(Long userId, String agentType, Integer status, String source, String excludeSource,
                                boolean showDeleted, Integer offset, Integer limit);

    /**
     * count设备
     * @param userId user.id
     * @param agentType 终端类型
     * @param status 设备状态
     * @param source 设备来源
     * @param excludeSource 排除来源
     * @param showDeleted   是否显示已删除的设备
     * @return List<UserDevice>
     */
    Long countDevice(Long userId, String agentType, Integer status, String source, String excludeSource,
                     boolean showDeleted);

    /**
     * 查询指定设备的变更历史
     * @param userId
     * @return
     */
    List<UserDeviceHistory> listDeviceHistory(Long userId, Long devicePk);

    /**
     *
     * @param agentType
     * @param statusCode
     * @return
     */
    List<UserDeviceProperty> getDevicePropertyConfig(String agentType, Byte statusCode);

    Long addDeviceProperty(UserDeviceProperty property);

    void editDeviceProperty(UserDeviceProperty property);

    void deleteDeviceProperty(Long id);

    /**
     * 发送授权邮件
     */
    void sendAuthEmail(User user, String agentType, Map<String, String> content, String customDeviceAuthorizeUrl,
            String customForbiddenLink, String callback);

    /**
     * 发送授权邮件
     */
    void sendAuthEmailForNewProcess(User user, String agentType, Map<String, String> content, String customDeviceAuthorizeUrl,
                       String customForbiddenLink, String callback);


    /**
     * 在登录的时候缓存设备验证信息
     */
    void cacheDeviceAuthForLogin(User user, String agentType, Map<String, String> content, String customDeviceAuthorizeUrl,
                                    String customForbiddenLink, String callback);

    /**
     * 是否开启了严格模式（当设备信息不存在时，强制ip验证）
     */
    boolean isStrictMode(String agentType);

    /**
     * 判断该版本是否需要验证设备
     */
    boolean checkVersion(String agentType, String version);

    /**
     * 有效的最小属性数目
     */
    int getMinPropertyCount(String agentType);

    /**
     * 查询待授权的特殊用户设备
     */
    List<Map<String, Object>> getSpecialUserAuthList();

    /**
     * 保存关联的设备信息
     * @param userId 用户id
     * @param devicePk
     * @param relatedDeviceIds
     */
    void updateRelatedDevice(Long userId, Long devicePk, String relatedDeviceIds);

    /**
     * 查询关联的设备
     */
    List<UserDeviceRelation> listRelation(Long devicePk);

    /**
     * 判断设备是否在黑设备列表中
     * @param deviceInfo
     * @return
     */
    boolean isDeviceInBlackList(Map<String, String> deviceInfo, String clientType);


    /**
     * 判断是否新设备
     * 
     * @param userId
     * @param candidate
     * @param agentType
     * @return
     */
    FindMostSimilarUserDeviceResponse findMostSimilarDevice(Long userId, Map<String, String> candidate, String agentType);

    UserDeviceComparator.DeviceComparisonResult compareV2(Map<String, String> content1, Map<String, String> content2, String agentType);

    ResendAuthorizeDeviceEmailResponse resendAuthorizeDeviceEmail(APIRequest<ResendAuthorizeDeviceEmailRequest> request);

    Boolean verifyAuthDeviceCode(APIRequest<VerifyAuthDeviceCodeRequest> request);


    TerminalEnum getUserLastLoginDevice(Long userId) throws Exception;

    boolean checkNewDisableLogicVersion(String agentType, String version);


    boolean isOldAppVersion(String agentType, String version);



    public Boolean checkIfPassLoginYubikey(Long userId);


    public CheckNewDeviceIpResponse checkNewDeviceIp(APIRequest<CheckNewDeviceIpRequest> checkNewDeviceIpRequest);


    APIResponse<AddUserDeviceForQRCodeLoginResponse> addDeviceForQRCodeLogin(APIRequest<AddUserDeviceForQRCodeLoginRequest> apiRequest);








}
