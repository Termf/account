package com.binance.account.service.user.impl;

import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.binance.account.common.enums.UserKycEmailNotifyStatus;
import com.binance.account.common.enums.UserKycEmailNotifyType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.UserKycEmailNotify;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.certificate.UserKycEmailNotifyMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.IUserKycEmailNotify;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.streamer.api.order.OrderApi;
import com.binance.streamer.api.request.order.QueryOrderListRequest;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Service
public class UserKycEmailNotifyBusiness implements IUserKycEmailNotify {
	@Autowired
	ApolloCommonConfig config;

	@Resource
	UserKycEmailNotifyMapper userKycEmailNotifyMapper;

	@Resource
	UserCommonBusiness userCommonBusiness;

	@Resource
	OrderApi orderApi;

	@Resource
	UserMapper userMapper;

	@Override
	public void addBasicNotifyTask(Long userId, String email) {
		// 目前美国站才会开启
		convertAdd(userId, email, UserKycEmailNotifyType.BASIC, 1);
		convertAdd(userId, email, UserKycEmailNotifyType.BASIC72H, 3);
		convertAdd(userId, email, UserKycEmailNotifyType.BASIC30D, 30);
	}

	@Override
	public void addTradeNotifyTask(Long userId, String email) {
		// 添加trade task
		convertAdd(userId, email, UserKycEmailNotifyType.TRADE, 3);
		convertAdd(userId, email, UserKycEmailNotifyType.TRADE24H, 1);
		convertAdd(userId, email, UserKycEmailNotifyType.TRADE7D, 7);
		convertAdd(userId, email, UserKycEmailNotifyType.TRADE30D, 30);

		// BASIC 标记为已经完成
		UserKycEmailNotify userKycEmailNotify = new UserKycEmailNotify();
		userKycEmailNotify.setUserId(userId);
		userKycEmailNotify.setType(UserKycEmailNotifyType.BASIC.name());
		userKycEmailNotify.setStatus(UserKycEmailNotifyStatus.SUCCESS.name());
		userKycEmailNotify.setFailReason("user has finish BASIC");
		userKycEmailNotify.setUpdateTime(DateUtils.getNewUTCDate());
		userKycEmailNotifyMapper.updateByPrimaryKeySelective(userKycEmailNotify);
		userKycEmailNotify.setType(UserKycEmailNotifyType.BASIC72H.name());
		userKycEmailNotifyMapper.updateByPrimaryKeySelective(userKycEmailNotify);
		userKycEmailNotify.setType(UserKycEmailNotifyType.BASIC30D.name());
		userKycEmailNotifyMapper.updateByPrimaryKeySelective(userKycEmailNotify);
	}

	@Override
	public boolean needDepositNotifyTask(Long userId) {
		if (!config.isKycEmailNotifyUser()) {
			return false;
		}
		UserKycEmailNotify userKycEmailNotify = userKycEmailNotifyMapper.selectByPrimaryKey(userId,
				UserKycEmailNotifyType.DEPOSIT.name());
		if (userKycEmailNotify != null) {
			return false;
		}
		return true;
	}

	@Override
	public void addDepositNotifyTask(Long userId, String email) {
		if (!config.isKycEmailNotifyUser()) {
			return;
		}
		UserKycEmailNotify userKycEmailNotify = userKycEmailNotifyMapper.selectByPrimaryKey(userId,
				UserKycEmailNotifyType.DEPOSIT.name());
		if (userKycEmailNotify == null) {
			convertAdd(userId, email, UserKycEmailNotifyType.DEPOSIT, 1);
		}
	}

	@Override
	public void addTradeCompleteTask(Long userId, String email, UserKycEmailNotifyType type) {
		if (!config.isKycEmailNotifyUser()) {
			return;
		}
		UserKycEmailNotify userKycEmailNotify = userKycEmailNotifyMapper.selectByPrimaryKey(userId, type.name());
		if (userKycEmailNotify == null) {
			convertAdd(userId, email, type, 1);
		}
	}

	@Override
	public void reset(Long userId) {
		if (!config.isKycEmailNotifyUser()) {
			return;
		}
		userKycEmailNotifyMapper.deleteByUserId(userId);
	}

	private UserKycEmailNotify convertAdd(Long userId, String email, UserKycEmailNotifyType type, int delay) {
		if (!config.isKycEmailNotifyUser()) {
			return null;
		}
		try {

			Date time = DateUtils.getNewUTCDate();
			Date delayTime = DateUtils.addDays(time, delay);

			UserKycEmailNotify userKycEmailNotify = new UserKycEmailNotify();
			userKycEmailNotify.setUserId(userId);
			userKycEmailNotify.setEmail(email);
			userKycEmailNotify.setType(type.name());
			userKycEmailNotify.setStatus(UserKycEmailNotifyStatus.INIT.name());
			userKycEmailNotify.setCreateTime(time);
			userKycEmailNotify.setUpdateTime(userKycEmailNotify.getCreateTime());
			userKycEmailNotify.setExecuteTime(delayTime);

			userKycEmailNotifyMapper.insert(userKycEmailNotify);
			return userKycEmailNotify;
		} catch (Exception e) {
			log.warn("添加kyc 用户邮件通知异常 userId: {}", userId, e);
			return null;
		}

	}

	@Override
	public void doTask() {
		try {
			Date endTime = DateUtils.getNewUTCDate();
			Date startTime = DateUtils.addDays(endTime, -4);
			List<UserKycEmailNotify> result = userKycEmailNotifyMapper.selectPage(startTime, endTime,
					UserKycEmailNotifyStatus.INIT.name(), 0, 500);
			if (result == null || result.isEmpty()) {
				log.info("用户BASIC和TRADE邮件通知执行完成 执行记录:0");
				return;
			}
			log.info("执行用户BASIC和TRADE邮件通知开始");
			for (UserKycEmailNotify userKycEmailNotify : result) {
				Long userId = userKycEmailNotify.getUserId();
				try {
					UserKycEmailNotifyType type = UserKycEmailNotifyType.valueOf(userKycEmailNotify.getType());

					String tplCode = null;
					switch (type) {
					case TRADE:
					case TRADE24H:
					case TRADE7D:
					case TRADE30D:
					case DEPOSIT:
						boolean needMail = false;
						if (UserKycEmailNotifyType.DEPOSIT.equals(type)) {
							needMail = needSendNoTradeMail(userKycEmailNotify, userKycEmailNotify.getCreateTime(),
									userKycEmailNotify.getExecuteTime());
						} else {
							needMail = needSendNoTradeMail(userKycEmailNotify, null, null);
						}
						if (!needMail) {
							break;
						}
						tplCode = "user.kyc." + type.name().toLowerCase() + ".notify";
						break;
					default:
						tplCode = "user.kyc." + type.name().toLowerCase() + ".notify";
						break;
					}
					if (StringUtils.isBlank(tplCode)) {
						continue;
					}
					User dbUser = this.userMapper.queryByEmail(userKycEmailNotify.getEmail());
					// 邮件通知
					userCommonBusiness.sendEmailWithoutRequest(tplCode, dbUser, null, tplCode, LanguageEnum.EN_US);
					updateUserKycEmailNotify(UserKycEmailNotifyStatus.SUCCESS, userId,
							UserKycEmailNotifyType.valueOf(userKycEmailNotify.getType()), null);

				} catch (Exception e) {
					log.warn("用户BASIC和TRADE邮件通知执行异常.舍弃任务 userId: {},type: {} ,", userKycEmailNotify.getUserId(),
							userKycEmailNotify.getType(), e);
					updateUserKycEmailNotify(UserKycEmailNotifyStatus.FAIL, userId,
							UserKycEmailNotifyType.valueOf(userKycEmailNotify.getType()), e.getMessage());
				}
			}
			log.info("执行用户BASIC和TRADE邮件通知完成");
		} catch (Exception e) {
			log.warn("执行任务失败 ", e);
		}
	}

	private void updateUserKycEmailNotify(UserKycEmailNotifyStatus status, Long userId, UserKycEmailNotifyType type,
			String failReason) {
		UserKycEmailNotify record = new UserKycEmailNotify();
		record.setUserId(userId);
		record.setType(type.name());
		record.setStatus(status.name());
		record.setFailReason(failReason);
		record.setUpdateTime(DateUtils.getNewUTCDate());
		userKycEmailNotifyMapper.updateByPrimaryKeySelective(record);

	}

	/**
	 * 判断用户完成kyc1后是否做过交易
	 * 
	 * @param userKycEmailNotify
	 * @return
	 */
	private boolean needSendNoTradeMail(UserKycEmailNotify userKycEmailNotify, Date startTime, Date endTime) {
		Long userId = userKycEmailNotify.getUserId();
		QueryOrderListRequest request = new QueryOrderListRequest();
		request.setUserId(userId);
		request.setStartTime(startTime);
		request.setEndTime(endTime);
		APIResponse<Long> response = orderApi.getOrderListCount(APIRequest.instance(request));
		if (response == null) {
			updateUserKycEmailNotify(UserKycEmailNotifyStatus.FAIL, userId,
					UserKycEmailNotifyType.valueOf(userKycEmailNotify.getType()), "request streamer fail");
			return false;
		}

		if (response == null || response.getStatus() != APIResponse.Status.OK) {
			String message = response.getCode();
			message += response.getErrorData() == null ? "" : response.getErrorData().toString();
			updateUserKycEmailNotify(UserKycEmailNotifyStatus.FAIL, userId,
					UserKycEmailNotifyType.valueOf(userKycEmailNotify.getType()), message);
			return false;
		}
		if (response.getData() != null && response.getData().longValue() > 0L) {
			updateUserKycEmailNotify(UserKycEmailNotifyStatus.SUCCESS, userId,
					UserKycEmailNotifyType.valueOf(userKycEmailNotify.getType()), "user has finish TRADE");
			return false;
		}
		return true;
	}

}
