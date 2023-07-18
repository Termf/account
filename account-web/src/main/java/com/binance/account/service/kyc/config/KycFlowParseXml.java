package com.binance.account.service.kyc.config;

import com.binance.master.utils.StringUtils;
import lombok.extern.log4j.Log4j2;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * 解析kyc-flow 逻辑
 * 
 * @author liufeng
 *
 */
@Log4j2
@Configuration
public class KycFlowParseXml {

	@Bean
	public KycFlowConfigMap parseXml() throws Exception {
		try {
			ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
			Resource[] resources = resolver.getResources("kycflow/*.xml");
			KycFlowConfigMap kycFlowConfigMap = new KycFlowConfigMap();
			for (Resource resource : resources) {
				InputStream stream = resource.getInputStream();
				log.info("fileName {} length {} ", resource.getFilename(), resource.contentLength());
				kycFlowConfigMap.putConfig(resource.getFilename(), parseXml(stream, resource.getFilename()));
			}
			return kycFlowConfigMap;
		} catch (Exception e) {
			log.error("pase xml exception", e);
			throw e;
		}
	}

	public KycFlowConfig parseXml(InputStream is, String fileName) {
		SAXReader reader = new SAXReader();
		KycFlowConfig config = new KycFlowConfig();
		try {
			Document doc = reader.read(is);
			Element root = doc.getRootElement();
			Iterator<Element> it = root.elementIterator();
			while (it.hasNext()) {
				Element e = it.next();
				String eName = e.getName().toLowerCase();
				switch (eName) {
				case KycFlowAtomDefine.ROOT_NAME:
					config.addAtoms(parseAtom(e));
					break;
				case KycFlowComposeDefine.ROOT_NAME:
					config.addComposes(parseCompose(e));
					break;
				case KycFlowGlobalEndDefine.ROOT_NAME:
					String isSync = e.attributeValue(KycFlowGlobalEndDefine.DEFINE_IS_SYNC);
					if(StringUtils.isNotBlank(isSync) && "false".equals(isSync)) {
						config.setSyncGlobalEnd(false);
					}
					config.setGlobalEnd(parseGlobalEnd(e));
					break;
				default:
					log.error(fileName + "未知配置" + eName);
					break;
				}
			}
			config.setInit(true);
			return config;
		} catch (DocumentException e) {
			log.error("解析kycflow/" + fileName + "失败", e);
			return new KycFlowConfig();
		} finally {
			if(is != null) {
				try {
					is.close();
				} catch (IOException e) {
					log.error("parse xml exception",e);
				}
			}
		}
	}

	private KycFlowAtomDefine parseAtom(Element e) {
		KycFlowAtomDefine atomDefine = new KycFlowAtomDefine();
		String runGlobalEndDefine = e.attributeValue(KycFlowGlobalEndDefine.DEFINE_RUN_GLOBAL_END);
		if (StringUtils.isNotBlank(runGlobalEndDefine)) {
			atomDefine.setRunGlobalEnd(Boolean.parseBoolean(runGlobalEndDefine));
		}
		atomDefine.setName(e.attributeValue(KycFlowAtomDefine.DEFINE_NAME));
		KycFlowExecutorDefine executorDefine = new KycFlowExecutorDefine();
		executorDefine.setExecutorName(e.attributeValue(KycFlowAtomDefine.EXECUTOR_NAME));
		atomDefine.setExecutor(executorDefine);
		return atomDefine;
	}
	
	private KycFlowComposeDefine parseCompose(Element e) {
		KycFlowComposeDefine composeDifine = new KycFlowComposeDefine();
		composeDifine.setName(e.attributeValue(KycFlowComposeDefine.DEFINE_NAME));
		String runGlobalEndDefine = e.attributeValue(KycFlowGlobalEndDefine.DEFINE_RUN_GLOBAL_END);
		if (StringUtils.isNotBlank(runGlobalEndDefine)) {
			composeDifine.setRunGlobalEnd(Boolean.getBoolean(runGlobalEndDefine));
		}
		Element executors = e.element(KycFlowComposeDefine.DEFINE_EXECUTORS);
		Iterator<Element> it = executors.elementIterator();
		while (it.hasNext()) {
			Element executor = it.next();
			KycFlowExecutorDefine executorDefine = new KycFlowExecutorDefine();
			executorDefine.setExecutorName(executor.attributeValue(KycFlowComposeDefine.DEFINE_EXECUTE_NAME));
			composeDifine.addExecutor(executorDefine);
		}
		return composeDifine;
	}

	private KycFlowGlobalEndDefine parseGlobalEnd(Element e) {
		KycFlowGlobalEndDefine kycFlowGlobalEndDefine = new KycFlowGlobalEndDefine();
		Element executors = e.element(KycFlowGlobalEndDefine.DEFINE_EXECUTORS);
		Iterator<Element> it = executors.elementIterator();
		while (it.hasNext()) {
			Element executor = it.next();
			KycFlowExecutorDefine executorDefine = new KycFlowExecutorDefine();
			executorDefine.setExecutorName(executor.attributeValue(KycFlowComposeDefine.DEFINE_EXECUTE_NAME));
			kycFlowGlobalEndDefine.addExecutor(executorDefine);
		}
		return kycFlowGlobalEndDefine;

	}
}
