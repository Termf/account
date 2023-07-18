package com.binance.account.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.service.capical.check.ICapitalCheck;
import com.binance.assetservice.api.IProductApi;
import com.binance.assetservice.vo.request.product.PriceConvertRequest;
import com.binance.assetservice.vo.response.product.PriceConvertResponse;
import com.binance.capital.api.DepositApi;
import com.binance.capital.vo.deposit.request.FiatRiskUpdateRequest;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.idm.IdmTrxData;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.LogMaskUtils;
import com.binance.master.utils.TrackingUtils;
import com.binance.sysconf.service.SysConfigVarCacheService;
import com.rabbitmq.client.Channel;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.handler.annotation.Header;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.InvalidParameterException;
import java.util.UUID;

/**
 * 合规--临时用
 */
@Log4j2
@Configuration
@ConditionalOnProperty(value = "accout.capital.check.listener.on", havingValue = "true")
public class CapitalInOutListener2 {
	private static final String OPT_TYPE_CHARGE_CRYPTO = "USER_CHARGE_CRYPTO";

	private static final String DATA = "data";
	private static final String PNK_WEB = "PNK_WEB";
	private static final String SYS_TYPE = "sysType";
	private static final String OPT_TYPE = "optType";
	private static final String USER_ID = "userId";

	private static final String COIN = "coin";
	private static final String SOURCE_ADDRESS = "sourceAddress";
	private static final String TARGET_ADDRESS = "targetAddress";
	private static final String TRANSFER_AMOUNT = "transferAmount";
	private static final String TX_ID = "txId";
	private static final String ID = "id";

	private static final String QUEUE_NAME = "exchange_pnk_user_query";
	private static final String ROUTING_KEY = "exchange.pnk.user.query";
	private static final String EXCHANGE_NAME = "rabbit.exchange.direct";


	@PostConstruct
	public void init(){
		log.info("CapitalInOutListener bean initialized!!!!!");
	}

	@Autowired
	private SysConfigVarCacheService sysConfigVarCacheService;

	@Resource
	private ICapitalCheck capitalCheckService;

	@Resource
	private DepositApi depositApi;

	@Resource
	private ApolloCommonConfig apolloCommonConfig;

	@Resource
	private IdmApi idmApi;

	@Resource
	private IProductApi productApi;

	@RabbitListener(
			bindings = @QueueBinding(value = @Queue(value = QUEUE_NAME, durable = "true", autoDelete = "false", exclusive="false"),
			exchange = @Exchange(value = EXCHANGE_NAME, durable = "true"), key = ROUTING_KEY),
			containerFactory = "rabbitListenerContainerFactory")
	public void onMessage(Message message, @Header(AmqpHeaders.CHANNEL) Channel channel) throws IOException{
		TrackingUtils.putTracking("capitalInOutListener", String.valueOf(UUID.randomUUID()).replaceAll("-", StringUtils.EMPTY));
		String body = null;
		try {
			body = new String(message.getBody());
			log.info("capitalInOutListener msg body:{}", LogMaskUtils.maskJsonString(body));

			if (StringUtils.isBlank(body)) {
				throw new InvalidParameterException("capitalInOutListener msg body is blank");
			}

			final JSONObject objectBody = JSON.parseObject(body);
			final String sysType = objectBody.getString(SYS_TYPE);

			if (StringUtils.equalsIgnoreCase(PNK_WEB, sysType)) {

				final JSONObject dataObject = objectBody.getJSONObject(DATA);
				final String optType = objectBody.getString(OPT_TYPE);
				final String userId = dataObject.getString(USER_ID);
				if (StringUtils.isBlank(userId)) {
					// Reject & Not-Requeue
					channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);
					return;
				}

				log.info(String.format("CapitalInOutListener msg, optType:%s", optType));

				switch (optType) {
					case OPT_TYPE_CHARGE_CRYPTO:
						// coin sourceAddress
						if ("1".equalsIgnoreCase(sysConfigVarCacheService.getValue("capital_chainalysis_check_deposit"))
								|| "1".equalsIgnoreCase(sysConfigVarCacheService.getValue("chainalysis_check_deposit"))) {
							String coin = dataObject.getString(COIN);
							String txHash = dataObject.getString(TX_ID);
							String chargeId = dataObject.getString(ID);
							String targetAddr = dataObject.getString(TARGET_ADDRESS);
							String sourceAddresses = dataObject.getString(SOURCE_ADDRESS);

							boolean result = capitalCheckService
									.detectAddressSourceBlackByAddress(userId, coin, txHash, sourceAddresses, targetAddr,chargeId);

							// 初始状态是5（手工）
							if (StringUtils.isNumeric(chargeId)) {
								FiatRiskUpdateRequest fiatRiskUpdateRequest = new FiatRiskUpdateRequest();
								fiatRiskUpdateRequest.setId(Long.valueOf(chargeId));
								fiatRiskUpdateRequest.setUserId(Long.valueOf(userId));
								if (result) {
									// 更新user_charge状态到4 (失败)
									fiatRiskUpdateRequest.setStatus(4);
									fiatRiskUpdateRequest.setAuditor("CA Reject");
								} else {
									// 更新user_charge状态到0，等待上账
									fiatRiskUpdateRequest.setStatus(0);
									fiatRiskUpdateRequest.setAuditor("CA Pass");
								}
								log.info("CapitalInOutListener -- change charge status:[{}], reportEnable:[{}].", fiatRiskUpdateRequest, apolloCommonConfig.getEnableReport());
								APIResponse<Boolean> response = depositApi.fiatRiskUpdate(APIRequest.instance(fiatRiskUpdateRequest));
								if (response ==null
										|| response.getStatus() != APIResponse.Status.OK
										|| !response.getData()) {
									log.error("CapitalInOutListener -- failed to change charge status! data:[{}]", fiatRiskUpdateRequest);
								}
							}

							if (StringUtils.equalsAnyIgnoreCase(apolloCommonConfig.getEnableReport(),"1","on","true")) {
								sendToIdm(String.valueOf(dataObject.get(TRANSFER_AMOUNT)), userId, coin, result);
							}
						}

						break;
					default:
						break;
				}

			} else {
				log.warn("not CapitalInOutListener msg, sysType:{}, body:{}", sysType, body);
			}

			// All going well, consumer sending ack to broker
			channel.basicAck(message.getMessageProperties().getDeliveryTag(), false);

		} catch (InvalidParameterException e) {

			log.error("discard message to handle InvalidParameterException", e);
			// Reject & Not-Requeue
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);

		} catch (NullPointerException e) {

			log.error("discard message to handle NullPointerException", e);
			// Reject & Not-Requeue
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);

		} catch (DuplicateKeyException e) {

			log.warn("discarded the message due to DuplicateKeyException, body:{}", body);
			// Reject & Not-Requeue
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), false);

		} catch (Exception e) {

			log.error("accountMsgNotification failed：", e);
			// Reject but Requeue
			channel.basicReject(message.getMessageProperties().getDeliveryTag(), true);

		} finally {
			TrackingUtils.removeTracking();
		}
	}

	/**
	 * 发送 report to idm
	 *
	 * @param amount
	 * @param userId
	 * @param coin
	 * @param result
	 * @throws Exception
	 */
	private void sendToIdm(String amount, String userId, String coin, boolean result) throws Exception {
		// 通知 充值 结果给 IDM.
		// 1 获取该币种对USD的价格
		PriceConvertRequest request = new PriceConvertRequest();
		request.setFrom(coin);
		request.setTo("USD");
		request.setAmount(new BigDecimal(amount));
		BigDecimal coinToUsdAmount = new BigDecimal(1);
		APIResponse<PriceConvertResponse> responseAPIResponse = productApi.priceConvert(APIRequest.instance(request));
		if (responseAPIResponse!=null
				&& responseAPIResponse.getStatus()== APIResponse.Status.OK
				&& responseAPIResponse.getData()!=null) {
			coinToUsdAmount = responseAPIResponse.getData().getAmount();
		}
		else {
			log.warn("Cannot get USD amount for [{}] [{}].", amount, coin);
		}

		IdmTrxData idmTrxData = IdmTrxData.builder()
				.amt(String.valueOf(coinToUsdAmount))
				.tti(String.valueOf(System.currentTimeMillis()/1000))
				.man(userId)
				.memo1(coin)
				.memo2(amount)
				.memo3(result?"highRisk":"lowRisk")
				.build();

		log.info("charge tx:[{}] will be sent to idm.", idmTrxData);
		// 2 发送给IDM
		idmApi.consumerDepositEvaluation(APIRequest.instance(idmTrxData));
	}
}
