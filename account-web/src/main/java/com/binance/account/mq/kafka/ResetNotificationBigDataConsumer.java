package com.binance.account.mq.kafka;

import java.util.Date;

import javax.annotation.Resource;

import com.binance.account.config.KafkaExclusionCondition;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Conditional;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.binance.account.data.entity.security.UserResetBigDataLog;
import com.binance.account.data.mapper.security.UserResetBigDataLogMapper;
import com.binance.account.service.question.checker.QuestionModuleChecker;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

/**
 * RM-250，消费大数据reset处理数据，打tag/落库/提供admin查询的api
 *
 */
@Slf4j
@Service
@Conditional({KafkaExclusionCondition.class})
public class ResetNotificationBigDataConsumer {

	@Resource
	private UserResetBigDataLogMapper mapper;
	@Resource
	private QuestionModuleChecker checker;
	
	@KafkaListener(id = "spark-risk-receiver", 
			topics = "bigdata-risk-notification",
            containerFactory = "kafkaListenerContainerFactory",
			groupId = "bigdata")
	public void onMessage(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
		String value = record.value();
		log.info("消费大数据reset处理流水,message: {}", value);
		try {
			/*
			 * { "userId": "12345678", "type": "RESET_2FA_MODEL", "action":
			 * "ADD_USER_TAG", "description": "Reset2FA Risk - Daily Batch", "data": {
			 * "userId": "36410292", "transId": "6082da1e4e944ab98de1f6951696f04e",
			 * "batchTime": 1551697486530, "score": 0.7893 } }
			 */
			JSONObject msg = JSONObject.parseObject(value);
			if ("RESET_2FA_MODEL".equalsIgnoreCase(msg.getString("type"))) {
				ResetData resetData = msg.getObject("data", ResetData.class);
				// 打tag
				Long userId = resetData.userId;
				checker.make2ProtectedMode(userId);
				log.info("消费大数据reset处理流水->打tag,userId:{}", userId);
				// 保存流水
				UserResetBigDataLog data =new UserResetBigDataLog();
				data.setUserId(userId);
				data.setTransId(resetData.transId);
				Double score = resetData.getScore();
				score = score == null ? Double.valueOf(0) : score;
				data.setScore((int)(score * 10000));//保留4位小数
				data.setBatchTime(new Date(resetData.batchTime));
				data.setCreateTime(new Date());
				mapper.insert(data);
				log.info("消费大数据reset处理流水->保存,userId:{}", userId);
			}
		} catch (Exception ex) {
			log.error("消费大数据reset处理流水，error :", ex);
		} finally {
			// 手动ack
			acknowledgment.acknowledge();
		}
	}
	
	@Getter
	@Setter
	@ToString
	private static class ResetData {
		private Long userId;
		private String transId;
		private Long batchTime;
		private Double score;
	}
}
