package com.binance.account.service.kyc;

import com.binance.account.data.entity.certificate.MessageMap;
import com.binance.account.data.mapper.certificate.MessageMapMapper;
import com.binance.account.vo.other.MessageMapVo;
import com.binance.master.enums.LanguageEnum;
import io.shardingsphere.api.HintManager;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
@Log4j2
public class MessageMapHelper implements ApplicationContextAware {

	private static MessageMapMapper messageMapMapper;

	private static ConcurrentHashMap<String, List<MessageMap>> LOCAL_CACHE;

	public synchronized static void initCache() {

		HintManager hintManager = null;
		try {
			hintManager = HintManager.getInstance();
			hintManager.setMasterRouteOnly();
			log.info("初始化MESSAGE_MAP信息开始");
			ConcurrentHashMap<String, List<MessageMap>> temp = new ConcurrentHashMap<>();
			List<MessageMap> results = messageMapMapper.getAll();
			for (MessageMap messageMap : results) {
				if (temp.get(messageMap.getCode()) == null) {
					temp.put(messageMap.getCode(), new ArrayList<MessageMap>());
					log.info("初始化MESSAGE_MAP信息结束,MESSAGE_CODE:{}", messageMap.getCode());
				}
				temp.get(messageMap.getCode()).add(messageMap);
			}
			LOCAL_CACHE = temp;
			log.info("初始化MESSAGE_MAP信息结束");
		} finally {
			if (null != hintManager) {
				hintManager.close();
			}
		}

	}

	/**
	 * 保存信息映射关系
	 *
	 * @param mapVos
	 */
	public static void saveMessageMap(List<MessageMapVo> mapVos) {
		if (CollectionUtils.isEmpty(mapVos)) {
			return;
		}
		List<MessageMap> target = mapVos.stream().filter(item -> StringUtils.isNotBlank(item.getCode()))
				.map(item -> new MessageMap(item.getCode(), item.getLang(), item.getMessage()))
				.collect(Collectors.toList());

		List<MessageMap> del = messageMapMapper.batchSelectByPk(target);

		if (del != null && !del.isEmpty()) {
			messageMapMapper.batchDelete(del);
		}
		if (target != null && !target.isEmpty()) {
			messageMapMapper.batchInsert(target);
		}

	}

	public static List<MessageMapVo> fuzzyGetByCode(String code) {
		List<MessageMap> result = messageMapMapper.fuzzySeach(code);
		if (result == null || result.isEmpty()) {
			return null;
		}
		return result.stream().map(item -> new MessageMapVo(item.getCode(), item.getLang(), item.getMessage()))
				.collect(Collectors.toList());

	}

	public static void saveOneMessageMap(MessageMap messageMap) {
		try {
			messageMapMapper.insert(messageMap);
		} catch (DuplicateKeyException e) {
			messageMapMapper.update(messageMap);
		}
	}

	/**
	 * 获取对应的 message 信息
	 *
	 * @param code
	 * @param language
	 * @return
	 */
	public static String getMessage(String code, LanguageEnum language) {
		if (StringUtils.isBlank(code)) {
			return code;
		}
		try {
			List<MessageMap> maps = LOCAL_CACHE.get(code);
			if (maps.isEmpty()) {
				// 获取不到的情况下直接返回对应的code
				return code;
			}
			String defaultMessage = code;
			String lang = language == null ? LanguageEnum.EN_US.getLang() : language.getLang();
			for (MessageMap map : maps) {
				if (StringUtils.equalsIgnoreCase(map.getLang(), lang) && StringUtils.isNotBlank(map.getMessage())) {
					return map.getMessage();
				}else if (StringUtils.equalsIgnoreCase(map.getLang(), LanguageEnum.EN_US.getLang()) && StringUtils.isNotBlank(map.getMessage())) {
					// 默认英文值
					defaultMessage = map.getMessage();
				}
			}
			return defaultMessage;
		} catch (Exception e) {
			return code;
		}
	}

	/**
	 * 获取对应的 message list 信息
	 *
	 * @param code
	 * @return
	 */
	public static List<MessageMapVo> getMessageList(String code) {
		try {
			List<MessageMap> maps = LOCAL_CACHE.get(code);
			if (maps.isEmpty()) {
				return Collections.emptyList();
			}
			List<MessageMapVo> vos = maps.stream().map(item -> {
				MessageMapVo messageMapVo = new MessageMapVo();
				BeanUtils.copyProperties(item, messageMapVo);
				return messageMapVo;
			}).collect(Collectors.toList());
			return vos;
		} catch (Exception e) {
			return Collections.emptyList();
		}
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		MessageMapHelper.messageMapMapper = applicationContext.getBean(MessageMapMapper.class);
		initCache();
	}
}
