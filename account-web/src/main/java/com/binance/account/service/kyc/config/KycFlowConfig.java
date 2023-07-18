package com.binance.account.service.kyc.config;

import java.util.ArrayList;
import java.util.List;

import com.binance.master.utils.StringUtils;

import lombok.Getter;
import lombok.Setter;

/**
 * xml 配置类
 * 
 * @author liufeng
 *
 */
@Getter
@Setter
public class KycFlowConfig {

	private boolean isInit = false;

	private List<KycFlowAtomDefine> atoms;

	private List<KycFlowComposeDefine> composes;

	private KycFlowGlobalEndDefine globalEnd;
	
	private boolean syncGlobalEnd = true;

	public void addAtoms(KycFlowAtomDefine atom) {
		if (atoms == null) {
			atoms = new ArrayList<KycFlowAtomDefine>();
		}
		atoms.add(atom);
	}

	public void addComposes(KycFlowComposeDefine compose) {
		if (composes == null) {
			composes = new ArrayList<KycFlowComposeDefine>();
		}
		composes.add(compose);
	}

	@Override
	public String toString() {
		return StringUtils.objectToString(this);
	}
}
