package com.binance.account.service.kyc.executor.us;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoGender;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.KycFillInfoHistory;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.BaseInfoSubmitUsValidator;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

@Service
@Log4j2
public class BaseInfoSubmitUsExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	private ICountry iCountry;

	@Resource
	private UserSecurityMapper userSecurityMapper;

	@Autowired
	private BaseInfoSubmitUsValidator validator;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		Long userId = kycFlowRequest.getUserId();
		// 加入一个锁, 防止重复提交导致多次初始化的问题
		try {
			KycFlowResponse response = initKycBaseInfoHandler(userId, kycFlowRequest);
			KycFlowContext.getContext().setKycFlowResponse(response);
			return response;
		} catch (BusinessException e) {
			throw e;
		} catch (Exception e) {
			log.error(String.format("提交基本信息处理异常 userId:%s", userId), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	/**
	 * 初始化Kyc module
	 *
	 * @param userId
	 * @param kycFlowRequest
	 * @return
	 */
	private KycFlowResponse initKycBaseInfoHandler(Long userId, KycFlowRequest kycFlowRequest) {
		BaseInfoRequest baseInfoRequest = (BaseInfoRequest) kycFlowRequest;

		validator.validateApiRequest(baseInfoRequest);

		BaseInfoResponse response = new BaseInfoResponse();

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate != null) {
			validator.validateKycCertificateStatus(kycCertificate);
			validator.validateRequestCount(kycCertificate);
		}
		if (kycCertificate == null) {
			kycCertificate = KycCertificateConvertor.convert2KycCertificate(baseInfoRequest);
			checkFaceSwitch(kycCertificate);
			kycCertificate.setFlowDefine("us");
			kycCertificateMapper.insert(kycCertificate);
		} else if (!baseInfoRequest.getKycType().getCode().equals(kycCertificate.getKycType())) {
			// KYC_TYPE 请求和db 不相同，更改kycType
			kycCertificate.setKycType(baseInfoRequest.getKycType().getCode());
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificate.setBaseFillStatus(KycCertificateStatus.PROCESS.name());
			checkFaceSwitch(kycCertificate);
			// user 变 company
			if (KycCertificateKycType.COMPANY.equals(baseInfoRequest.getKycType())) {
				kycCertificate.setAddressStatus(null);
				kycCertificate.setAddressTips(null);
				kycCertificate.setBindMobile(null);
				kycCertificate.setMobileCode(null);
				kycCertificate.setGoogleFormStatus(KycCertificateStatus.REVIEW.name());
			} else if (KycCertificateKycType.USER.equals(baseInfoRequest.getKycType())) {
				// company 转 user
				kycCertificate.setGoogleFormStatus(null);
				kycCertificate.setGoogleFormTips(null);
			}
			kycCertificate.setFlowDefine("us");
			kycCertificateMapper.updateKycType(kycCertificate);
		} else {
			kycCertificate.setUpdateTime(DateUtils.getNewUTCDate());
			kycCertificate.setBaseFillStatus(KycCertificateStatus.PROCESS.name());
			kycCertificate.setBaseFillTips("");
			checkFaceSwitch(kycCertificate);
			if(KycCertificateKycType.COMPANY.getCode().equals(kycCertificate.getKycType())) {
				kycCertificate.setGoogleFormStatus(KycCertificateStatus.REVIEW.name());
				kycCertificate.setGoogleFormTips("");
			}
			kycCertificateMapper.updateByPrimaryKeySelective(kycCertificate);
		}

		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		if (kycFillInfo == null) {
			kycFillInfo = KycCertificateConvertor.convert2FillInfo(null, baseInfoRequest);
			kycFillInfoMapper.insert(kycFillInfo);
		} else {
			// 已有记录 则将fill_info 迁入history，并且删掉原纪录重新插入新纪录。
			KycFillInfoHistory kycFillInfoHistory = new KycFillInfoHistory();
			BeanUtils.copyProperties(kycFillInfo, kycFillInfoHistory);
			kycFillInfoHistoryMapper.insert(kycFillInfoHistory);
			kycFillInfoMapper.deleteByUk(kycFillInfo.getUserId(), kycFillInfo.getFillType());

			kycFillInfo = KycCertificateConvertor.convert2FillInfo(kycFillInfo, baseInfoRequest);
			kycFillInfo.setIdmTid(kycFillInfoHistory.getIdmTid());
			kycFillInfoMapper.insert(kycFillInfo);
		}
		KycFlowContext.getContext().setKycCertificate(kycCertificate);
		KycFlowContext.getContext().setKycFillInfo(kycFillInfo);
		BeanUtils.copyProperties(kycFillInfo, response);
		response.setBaseFillStatus(KycCertificateStatus.valueOf(kycCertificate.getBaseFillStatus()));
		response.setKycType(KycCertificateKycType.getByCode(kycCertificate.getKycType()));
		response.setGender(KycFillInfoGender.getGender(kycFillInfo.getGender()));
		if (StringUtils.isNotBlank(kycFillInfo.getRegionState())) {
			CountryState countryState = CountryStateHelper.getCountryStateByPk(kycFillInfo.getCountry(),
					kycFillInfo.getRegionState());
			if (countryState != null) {
				response.setRegionState(countryState.getEn());
			}
			response.setRegionStateCode(kycFillInfo.getRegionState());
		}
		return response;
	}

	private void checkFaceSwitch(KycCertificate kycCertificate) {
		if (!config.isKycJumioFaceSwitch()) {
			kycCertificate.setFaceStatus(KycCertificateStatus.SKIP.name());
			kycCertificate.setFaceTips("");
		}
	}
}
