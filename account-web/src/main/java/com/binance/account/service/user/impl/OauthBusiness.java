package com.binance.account.service.user.impl;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import javax.annotation.Resource;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.common.constant.UserConst;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.domain.bo.MsgNotification;
import com.binance.account.domain.bo.OauthRedisDto;
import com.binance.account.vo.user.enums.CommonStatusEnum;
import com.binance.account.vo.user.request.AccountActiveUserRequest;
import com.binance.account.vo.user.request.OauthResendEmailRequest;
import com.binance.account.vo.user.request.OpenIdActiveRequest;
import com.binance.account.vo.user.response.AccountActiveUserResponse;
import com.binance.account.vo.user.response.OpenIdActiveResponse;
import com.binance.master.enums.SysType;
import com.binance.master.models.RedisVerify;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.data.entity.oauth.OauthBind;
import com.binance.account.data.entity.security.UserSecurity;
import com.binance.account.data.entity.security.UserSecurityLog;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.entity.user.UserInfo;
import com.binance.account.data.entity.user.UserIp;
import com.binance.account.data.mapper.oauth.OauthBindMapper;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.data.mapper.user.UserInfoMapper;
import com.binance.account.data.mapper.user.UserIpMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.datamigration.IMsgNotification;
import com.binance.account.service.user.IOauth;
import com.binance.account.vo.device.response.AddUserDeviceResponse;
import com.binance.account.vo.user.request.BindOauthRequest;
import com.binance.account.vo.user.response.BindOauthResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.CacheKeys;
import com.binance.master.constant.Constant;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.old.ibusiness.sys.ISysConfig;
import com.binance.master.utils.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OauthBusiness implements IOauth {
	private static final String REGEX_EMAIL = "^[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)*@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
	private static final Integer MAX_EMAIL_LENGTH = 200;

	public static final String NODE_TYPE_OAUTH_LOGIN = "email_binancelogin";
	public static final String NODE_TYPE_OAUTH_LOGIN_SIGNUP = "email_binancelogin_signup";
	public static final String OAUTH_LINK_EMAIL = "OAUTH_LINK_EMAIL";
	protected static final String DEFAULT_RESULT = "lctwmv9fdld6yfdk06g";
	@Resource
	protected IMsgNotification iMsgNotification;
	@Resource
	protected UserMapper userMapper;
	@Autowired
	private UserCommonBusiness userCommonBusiness;
	@Resource
	protected UserSecurityMapper userSecurityMapper;
	@Autowired
	private ISysConfig iSysConfig;
	@Autowired
	private UserInfoMapper userInfoMapper;
	@Resource
	protected UserIpMapper userIpMapper;
	@Resource
	protected UserSecurityLogMapper userSecurityLogMapper;
	@Resource
	protected OauthBindMapper oauthBindMapper;

	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	@Override
	public APIResponse<BindOauthResponse> bind(APIRequest<BindOauthRequest> request) throws Exception {
		final String ip = WebUtils.getRequestIp();
		final BindOauthRequest requestBody = request.getBody();
		BindOauthResponse response = new BindOauthResponse();
		response.setStatus(CommonStatusEnum.INIT);
		OauthBind oauthBind = oauthBindMapper.selectByClientAndOauthUserId(requestBody.getClientId(),
				requestBody.getOauthUserId());
		if (oauthBind != null) {
			response.setUserId(oauthBind.getUserId());
			// 1. 已绑定，未激活openid 发送激活邮件 -> 激活openid关系
			if (CommonStatusEnum.isInit(oauthBind.getStatus())) {
				User user = userMapper.queryById(oauthBind.getUserId());
				sendVeiryEmail(requestBody, user, NODE_TYPE_OAUTH_LOGIN, false);
				response.setStatus(CommonStatusEnum.INIT);
				log.info("1. 已绑定，未激活openid 发送激活邮件 -> 激活openid关系,userId={}", oauthBind.getUserId());
			} else if (CommonStatusEnum.isCancel(oauthBind.getStatus())) {
				// 2. 曾经绑定，已解除 关联openid
				oauthBindMapper.bind(oauthBind.getId());
				response.setStatus(CommonStatusEnum.SUCCESS);
				log.info("2. 曾经绑定，已解除 关联openid,userId={}", oauthBind.getUserId());
			} else {
				// 3. 已绑定
				response.setStatus(CommonStatusEnum.SUCCESS);
			}
			return APIResponse.getOKJsonResult(response);
		} else {
			oauthBind = new OauthBind();
			BeanUtils.copyProperties(requestBody, oauthBind);
			// 默认绑定关系未激活
			oauthBind.setStatus(CommonStatusEnum.INIT.getKey());
		}
		// email强制转换成小写
		final String email = requestBody.getEmail().trim().toLowerCase();

		User tempUser = this.userMapper.queryByEmail(email);
		if (tempUser != null && BitUtils.isTrue(tempUser.getStatus(), Constant.USER_DELETE)) {
			// 4.用户已存在，但被删除，直接提示错误
			throw new BusinessException(GeneralCode.USER_FAIL_TO_REGISTER);
		} else if (tempUser != null) {
			// 5. 若用户已存在则绑定用户，发送激活邮件
			oauthBind.setUserId(tempUser.getUserId());
			oauthBindMapper.insert(oauthBind);
			response.setUserId(tempUser.getUserId());
			sendVeiryEmail(requestBody, tempUser, NODE_TYPE_OAUTH_LOGIN, false);
			return APIResponse.getOKJsonResult(response);
		}
		// 6.用户未注册
		UserIndex userIndex = userCommonBusiness.getUserIndexForRegister(email);// 获取一个用户索引
		String cipherCode = RedisCacheUtils.get(CacheKeys.PASSWORD_CIPHER, DEFAULT_RESULT, true);
		User user = User.buildRegisterObject(userIndex, StringUtils.left(UUID.randomUUID().toString(), 12), cipherCode);
		this.userMapper.insert(user);// 插入用户登录信息
		String userEmail = user.getEmail();
		UserSecurity userSecurity = new UserSecurity();
		userSecurity.setUserId(user.getUserId());
		userSecurity.setEmail(userEmail);
		userSecurity.setAntiPhishingCode("");// 防钓鱼码
		userSecurity.setSecurityLevel(1);// 安全级别
		userSecurity.setMobileCode("");
		userSecurity.setMobile("");
		userSecurity.setLoginFailedNum(0);
		userSecurity.setLoginFailedTime(DateUtils.getNewDate());
		userSecurity.setAuthKey("");
		userSecurity.setLastLoginTime(DateUtils.getNewDate());
		userSecurity.setLockEndTime(DateUtils.getNewDate());
		userSecurity.setInsertTime(DateUtils.getNewDate());
		userSecurity.setUpdateTime(DateUtils.getNewDate());
		userSecurity.setWithdrawSecurityStatus(0);
		userSecurity.setWithdrawSecurityAutoStatus(0);
		log.info("oauth register:插入用户安全信息");
		this.userSecurityMapper.insert(userSecurity);// 用户安全信息
		log.info("oauth register:初始化userInfo信息");
		UserInfo userInfo = new UserInfo();
		userInfo.setUserId(user.getUserId());
		userInfo.setParent(null);
		BigDecimal agentRewardRatio = new BigDecimal(
				this.iSysConfig.selectByDisplayName("agent_reward_ratio").getCode());
		userInfo.setAgentRewardRatio(agentRewardRatio);// 经纪人返佣比例
		userInfo.setTradingAccount(null);// 用户交易账户 激活时创建
		BigDecimal makerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("maker_commission").getCode());
		BigDecimal takerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("taker_commission").getCode());
		BigDecimal buyerCommission = new BigDecimal(this.iSysConfig.selectByDisplayName("buyer_commission").getCode());
		BigDecimal sellerCommission = new BigDecimal(
				this.iSysConfig.selectByDisplayName("seller_commission").getCode());
		userInfo.setMakerCommission(makerCommission);// 被动方手续费
		userInfo.setTakerCommission(takerCommission);// 主动方手续费
		userInfo.setBuyerCommission(buyerCommission);// 买方交易手续费
		userInfo.setSellerCommission(sellerCommission);// 卖方交易手续费
		userInfo.setDailyWithdrawCap(null);// 单日最大出金总金额
		userInfo.setDailyWithdrawCountLimit(null);// 单日最大出金次数
		userInfo.setAutoWithdrawAuditThreshold(null);// 免审核额度
		userInfo.setNickName("");
		userInfo.setRemark("");
		userInfo.setTrackSource(requestBody.getClientId());
		userInfo.setInsertTime(DateUtils.getNewDate());
		userInfo.setUpdateTime(DateUtils.getNewDate());
		// userInfo.setAgentId(requestBody.getAgentId());// 获取推荐人
		userInfo.setTradeLevel(0);
		// 返佣开关关闭的话，无视推荐人
		String ref_switch = this.iSysConfig.selectByDisplayName("ref_switch").getCode();
		if ("0".equals(ref_switch) || "off".equalsIgnoreCase(ref_switch) || "false".equalsIgnoreCase(ref_switch)) {
			userInfo.setAgentId(null);
		}
		if (userInfo.getAgentId() == null) {
			log.info("register:设置默认推荐人");
			Long agentId = Long.valueOf(this.iSysConfig.selectByDisplayName("default_agent").getCode());
			userInfo.setAgentId(agentId);
		}
		log.info("register:插入userInfo信息");
		this.userInfoMapper.insertSelective(userInfo);// 插入用户信息
		sendVeiryEmail(requestBody, user, NODE_TYPE_OAUTH_LOGIN_SIGNUP, false);
		// 记录设备指纹信息
		String locationCity = IP2LocationUtils.getCountryCity(ip);
		String clientType = request.getTerminal().getCode();
		AddUserDeviceResponse deviceResponse = null;
		// Map<String, String> deviceInfo = requestBody.getDeviceInfo();
		// if (deviceInfo != null) {
		// try {
		// userDeviceBusiness.preCheck(deviceInfo, user.getUserId(), clientType);
		// deviceResponse = userDeviceBusiness.addDevice(user.getUserId(), clientType,
		// UserDevice.Status.AUTHORIZED, UserDeviceConst.SOURCE_REGIST, deviceInfo);
		// } catch (Exception e) {
		// log.error("新增设备指纹出错 userId:{}, deviceInfo:{}", user.getUserId(),
		// requestBody.getDeviceInfo(), e);
		// }
		// }
		// 添加注册日志
		try {
			final UserSecurityLog securityLog = new UserSecurityLog(user.getUserId(), ip, locationCity, clientType,
					Constant.SECURITY_OPERATE_TYPE_REGIST, "注册");
			if (deviceResponse != null) {
				securityLog.touchDevice(deviceResponse.getId(), deviceResponse.getDeviceId());
			}
			UserIp userIp = new UserIp(user.getUserId(), ip);
			this.userIpMapper.insertIgnore(userIp);
			this.userSecurityLogMapper.insertSelective(securityLog);
		} catch (Exception e) {
			log.error(String.format("add oauth register log failed, email:%s, exception:", userEmail), e);
		}
		log.info("oauth register:注册结束");
		// 绑定用户
		oauthBind.setUserId(user.getUserId());
		oauthBindMapper.insert(oauthBind);
		response.setUserId(user.getUserId());

		// 临时的代码 完全迁移后移除 start
		Map<String, Object> dataMsg = new HashMap<>();
		dataMsg.put(UserConst.USER_ID, user.getUserId());
		dataMsg.put(UserConst.EMAIL, userEmail);
		dataMsg.put("salt", user.getSalt());
		dataMsg.put("password", user.getPassword());
		// dataMsg.put("registerToken", sendParams[0]);
		// dataMsg.put("code", sendParams[1]);
		dataMsg.put("agentId", userInfo.getAgentId());
		dataMsg.put("trackSource", userInfo.getTrackSource());
		dataMsg.put("ipAddress", WebUtils.getRequestIp());
		MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.REGISTER, dataMsg);
		log.info("iMsgNotification register:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg), "code"));
		this.iMsgNotification.send(msg);
		// 临时的代码 完全迁移后移除 end
		return APIResponse.getOKJsonResult(response);
	}

	private void sendVeiryEmail(BindOauthRequest request, User user, String template, boolean isResendEmail) {
		try {
			log.info("oauth sendVeiryEmail");
			userCommonBusiness.sendOauthBindEmail(request, user, template, isResendEmail);
		} catch (Exception e) {
			log.error(String.format("oauth sendVeiryEmail failed, email:%s, exception:", user.getEmail()), e);
		}
	}

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
	public APIResponse<OpenIdActiveResponse> openIdActive(APIRequest<OpenIdActiveRequest> request) {
		final OpenIdActiveRequest requestBody = request.getBody();
		log.info("openIdActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(request)));
		OauthRedisDto oauthRedisDto = RedisCacheUtils.get(requestBody.getEmail(), OauthRedisDto.class,
				OAUTH_LINK_EMAIL);
		if (oauthRedisDto == null || !StringUtils.equals(oauthRedisDto.getVerifyCode(), requestBody.getVerifyCode())) {
			throw new BusinessException(GeneralCode.USER_LINK_EXPIRED);
		}
		if (DateUtils.add(oauthRedisDto.getTime(), Calendar.MINUTE, UserCommonBusiness.EXPIRED_TIME)
				.getTime() < DateUtils.getNewUTCDate().getTime()) {
			// 发送时间超过30分钟的也认为失效
			throw new BusinessException(GeneralCode.USER_LINK_EXPIRED);
		}
		User tempUser = this.userMapper.queryByEmail(requestBody.getEmail());
		// 1. 账号未激活则先激活账号
		if (BitUtils.isFalse(tempUser.getStatus(), Constant.USER_ACTIVE)) {
			Long tradingAccount;
			User user = new User();
			user.setEmail(tempUser.getEmail());
			user.setStatus(tempUser.getStatus() | Constant.USER_ACTIVE | Constant.USER_FEE);// 默认禁用app交易
			this.userMapper.updateByEmail(user);
			UserInfo tempInfo = this.userInfoMapper.selectByPrimaryKey(tempUser.getUserId());
			if (tempInfo.getTradingAccount() == null) {
				tradingAccount = userCommonBusiness.createTradingAccount(tempInfo);// 创建交易账户
			} else {
				tradingAccount = tempInfo.getTradingAccount();
			}
			// 临时的代码 完全迁移后移除 start
			sendAccountActiveMqMsg(tempUser, tradingAccount);
			// 临时的代码 完全迁移后移除 end
			log.info("用户激活,userId={}", tempUser.getUserId());
		}
		// 2.激活openId
		OauthBind oauthBind = oauthBindMapper.selectByClientAndOauthUserId(oauthRedisDto.getClientId(),
				oauthRedisDto.getOauthUserId());
		if (oauthBind != null) {
			oauthBindMapper.bind(oauthBind.getId());
			log.info("oauth openId 激活成功,clientId={} oauthUserId={}", oauthRedisDto.getClientId(),
					oauthRedisDto.getOauthUserId());
		} else {
			log.error("oauth openId 激活失败，oauth信息不存在,clientId={} oauthUserId={}", oauthRedisDto.getClientId(),
					oauthRedisDto.getOauthUserId());
		}
		RedisCacheUtils.del(requestBody.getEmail(), OAUTH_LINK_EMAIL);
		OpenIdActiveResponse response = new OpenIdActiveResponse();
		BeanUtils.copyProperties(oauthRedisDto, response);
		return APIResponse.getOKJsonResult(response);
	}

	@Override
	public APIResponse<String> resendVerifyEmail(APIRequest<OauthResendEmailRequest> request) throws Exception {
		User user = this.userMapper.queryByEmail(request.getBody().getEmail());
		userCommonBusiness.sendOauthBindEmail(null, user, null, true);
		return APIResponse.getOKJsonResult(null);
	}

	private void sendAccountActiveMqMsg(User tempUser, Long tradingAccount) {
		Map<String, Object> dataMsg = new HashMap<>();
		dataMsg.put(UserConst.USER_ID, tempUser.getUserId());
		dataMsg.put("tradingAccount", tradingAccount);
		MsgNotification msg = new MsgNotification(SysType.PNK_WEB, MsgNotification.OptType.ACCOUNT_ACTIVE, dataMsg);
		log.info("iMsgNotification accountActive:{}", LogMaskUtils.maskJsonString(JSON.toJSONString(msg)));
		this.iMsgNotification.send(msg);
	}
}
