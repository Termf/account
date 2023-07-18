package com.binance.account.controller.other;

import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.binance.account.api.UsSiteAutomatedEmailDripApi;
import com.binance.account.async.AsyncTaskExecutor;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.service.user.IUserKycEmailNotify;
import com.binance.account.vo.other.AddTradeCompleteNotifyTaskRequest;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

import lombok.extern.log4j.Log4j2;

@RestController
@Log4j2
public class UsSiteAutomatedEmailDripControl implements UsSiteAutomatedEmailDripApi {

	@Resource
	private IUserKycEmailNotify iUserKycEmailNotify;

	@Resource
	protected UserIndexMapper userIndexMapper;

	@Override
	public APIResponse<Void> addDepositNotifyTask(APIRequest<Long> request) {
		UserIndex userIndex = userIndexMapper.selectByPrimaryKey(request.getBody());
		if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
			return APIResponse.getOKJsonResult();
		}
		iUserKycEmailNotify.addDepositNotifyTask(userIndex.getUserId(), userIndex.getEmail());
		return APIResponse.getOKJsonResult();
	}

	@Override
	public APIResponse<Boolean> needSendDepositNotify(APIRequest<Long> request) {
		return APIResponse.getOKJsonResult(new Boolean(iUserKycEmailNotify.needDepositNotifyTask(request.getBody())));
	}

	@Override
	public APIResponse<Void> batchAddTradeCompleteTask(APIRequest<List<AddTradeCompleteNotifyTaskRequest>> request) {
		List<AddTradeCompleteNotifyTaskRequest> body = request.getBody();

		if (body == null || body.isEmpty()) {
			return APIResponse.getOKJsonResult();
		}
		AsyncTaskExecutor.execute(() -> {
			log.info("开始异步添加交易完成定额后邮件通知用户.");
			for (AddTradeCompleteNotifyTaskRequest addTradeCompleteNotifyTaskRequest : body) {
				UserIndex userIndex = userIndexMapper.selectByPrimaryKey(addTradeCompleteNotifyTaskRequest.getUserId());
				if (userIndex == null || StringUtils.isBlank(userIndex.getEmail())) {
					return;
				}
				iUserKycEmailNotify.addTradeCompleteTask(userIndex.getUserId(), userIndex.getEmail(),
						addTradeCompleteNotifyTaskRequest.getType());
			}
		});
		return APIResponse.getOKJsonResult();
	}
}
