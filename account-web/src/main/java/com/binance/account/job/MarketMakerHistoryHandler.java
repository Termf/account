package com.binance.account.job;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.binance.account.service.user.IMarketMakerUser;
import com.xxl.job.core.biz.model.ReturnT;
import com.xxl.job.core.handler.IJobHandler;
import com.xxl.job.core.handler.annotation.JobHandler;

import lombok.extern.log4j.Log4j2;

/**
 * 做市商账号历史数据处理
 * @author zhao chenkai
 *
 */

@Log4j2
@JobHandler(value = "marketMakerHistoryHandler")
@Component
public class MarketMakerHistoryHandler extends IJobHandler {

	@Autowired
	private IMarketMakerUser marketMakerUser;

	@Override
	public ReturnT<String> execute(String param) throws Exception {
		log.info("开始执行 marketMakerHistoryHandler param:{}", param);
		try {
			if (StringUtils.isBlank(param))
				return SUCCESS;

			String[] userIdArr = param.split("[,，；;]");
			for (String userIdStr : userIdArr) {
				if (StringUtils.isNumeric(userIdStr)) {
					try {
						Long userId = Long.valueOf(userIdStr);
						marketMakerUser.marketMakerEnable2fa(userId);
					} catch (Exception e) {
						log.error("marketMakerEnable2fa error, userId:"+userIdStr, e);
					}
				}
			}
			log.info("marketMakerHistoryHandler 执行完成，param:{}", param);
			return SUCCESS;
		} catch (Exception e) {
			log.error("执行marketMakerHistoryHandler失败 ", e);
			return FAIL;
		}
	}

}
