package com.binance.account.service.kyc;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.service.kyc.config.KycFlowAtomDefine;
import com.binance.account.service.kyc.config.KycFlowComposeDefine;
import com.binance.account.service.kyc.config.KycFlowConfig;
import com.binance.account.service.kyc.config.KycFlowConfigMap;
import com.binance.account.service.kyc.config.KycFlowExecutorDefine;
import com.binance.account.service.kyc.config.KycFlowGlobalEndDefine;
import com.binance.account.service.kyc.endHandler.AbstractEndHandler;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.annotation.Resource;

/**
 * 处理器工厂类
 * 
 * @author liufeng
 *
 */
@Service
public class KycFlowProcessFactory implements ApplicationContextAware {

	private Map<String, Map<String, KycFlowProcessor>> processorMaps = new HashMap<String, Map<String, KycFlowProcessor>>();

	public static ThreadPoolExecutor THREAD_POOL = new ThreadPoolExecutor(5, 10, 60, TimeUnit.SECONDS,
			new LinkedBlockingQueue<Runnable>(1000), new ThreadPoolExecutor.CallerRunsPolicy());

	@Resource
	protected KycFlowConfigMap kycFlowConfigMap;

	@Autowired
	protected ApolloCommonConfig config;

	public KycFlowProcessor getProcessor(String name) {
		if (processorMaps.get(config.getKycFlowFileName()) == null) {
			return null;
		}
		String fileName = config.getKycFlowFileName();
		return processorMaps.get(fileName).get(name);
	}

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		Map<String, KycFlowConfig> configs = kycFlowConfigMap.getConfigs();
		Set<String> keys = configs.keySet();

		for (String k : keys) {
			KycFlowConfig kycFlowConfig = configs.get(k);
			Map<String, KycFlowProcessor> process = parseProcessor(kycFlowConfig, applicationContext);
			processorMaps.put(k, process);
		}
	}

	private Map<String, KycFlowProcessor> parseProcessor(KycFlowConfig kycFlowConfig,
			ApplicationContext applicationContext) {
		Map<String, KycFlowProcessor> processorMap = new HashMap<>();

		// 初始化 global end 执行期
		KycFlowGlobalEndDefine globalEndDefine = kycFlowConfig.getGlobalEnd();
		boolean syncGlobalEnd = kycFlowConfig.isSyncGlobalEnd();

		List<AbstractEndHandler> globalEnds = new ArrayList<>();

		for (KycFlowExecutorDefine globalEnd : globalEndDefine.getExecutors()) {
			AbstractEndHandler globalProcessor = (AbstractEndHandler) applicationContext
					.getBean(globalEnd.getExecutorName());
			globalEnds.add(globalProcessor);
		}

		// 初始化 atom 执行器
		List<KycFlowAtomDefine> atoms = kycFlowConfig.getAtoms();

		if (atoms != null && !atoms.isEmpty()) {
			for (KycFlowAtomDefine atom : atoms) {
				AtomKycFlowProcessor atomKycFlowProcessor = new AtomKycFlowProcessor();
				AbstractKycFlowCommonExecutor executor = (AbstractKycFlowCommonExecutor) applicationContext
						.getBean(atom.getExecutor().getExecutorName());
				atomKycFlowProcessor.initExecutor(executor);
				if(atom.isRunGlobalEnd()) {
					atomKycFlowProcessor.initGlobalEnd(atom.isRunGlobalEnd() ? globalEnds : null, syncGlobalEnd);
				}
				processorMap.put(atom.getName(), atomKycFlowProcessor);
			}
		}

		// 初始化 compose 执行器
		List<KycFlowComposeDefine> composes = kycFlowConfig.getComposes();

		if (composes != null && !composes.isEmpty()) {
			for (KycFlowComposeDefine compose : composes) {
				List<KycFlowExecutorDefine> executorDefines = compose.getExecutors();
				List<AbstractKycFlowCommonExecutor> composeExecutors = new ArrayList<AbstractKycFlowCommonExecutor>();
				ComposeKycFlowProcessor composeKycFlowProcessor = new ComposeKycFlowProcessor();
				for (KycFlowExecutorDefine define : executorDefines) {
					AbstractKycFlowCommonExecutor executor = (AbstractKycFlowCommonExecutor) applicationContext
							.getBean(define.getExecutorName());
					composeExecutors.add(executor);
				}
				composeKycFlowProcessor.initExecutor(composeExecutors);
				if(compose.isRunGlobalEnd()) {
					composeKycFlowProcessor.initGlobalEnd(compose.isRunGlobalEnd() ? globalEnds : null, syncGlobalEnd);
				}
				processorMap.put(compose.getName(), composeKycFlowProcessor);
			}
		}

		return processorMap;
	}

}
