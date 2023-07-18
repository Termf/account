package com.binance.account.service.kyc.executor.master;

import java.util.Date;
import java.util.Map;
import java.util.Random;

import javax.annotation.Resource;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.certificate.RiskRatingChangeLevelEvent;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.AddressAuthValidator;
import com.binance.account.service.user.impl.UserCommonBusiness;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.constant.Constant;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;
import com.binance.platform.common.TrackingUtils;
import com.google.common.collect.Maps;

import lombok.extern.log4j.Log4j2;

/**
 * ocr 异步通知
 * 
 * @author liufeng
 *
 */
@Service
@Log4j2
public class AddressAuthResultMasterExecutor extends AbstractKycFlowCommonExecutor {

	@Autowired
	private AddressAuthValidator validator;

	@Resource
	private UserIndexMapper userIndexMapper;
	@Resource
	private UserMapper userMapper;
	@Resource
	protected UserCommonBusiness userCommonBusiness;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		try {
			AddresAuthResultRequest request = (AddresAuthResultRequest) kycFlowRequest;

			validator.validateApiRequest(request);

			Long userId = request.getUserId();

			KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

			validator.validateKycCertificateStatus(kycCertificate, request.getAddressStatus());

			KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.ADDRESS.name());
			if (kycFillInfo == null) {
				log.warn("地址认证用户kyc fill info信息不存在 userId:{}", userId);
				throw new BusinessException(AccountErrorCode.KYC_FILL_NOT_EXISTS);
			}

			KycCertificate record = KycCertificateConvertor.convert2KycCertificate(request);
			kycCertificateMapper.updateStatus(record);
			kycCertificate.setAddressStatus(record.getAddressStatus());
			kycCertificate.setAddressTips(record.getAddressTips());
			if (KycCertificateStatus.PASS.equals(request.getAddressStatus())
					|| KycCertificateStatus.REFUSED.equals(request.getAddressStatus())) {
				KycFillInfo recordFillInfo = new KycFillInfo();
				recordFillInfo.setId(kycFillInfo.getId());
				recordFillInfo.setUserId(kycFillInfo.getUserId());
				recordFillInfo.setFillType(kycFillInfo.getFillType());
				if (KycCertificateStatus.PASS.equals(request.getAddressStatus())) {
					recordFillInfo.setStatus("SUCCESS");
				}
				recordFillInfo.setUpdateTime(DateUtils.getNewUTCDate());
				recordFillInfo.setOperator(request.getOperator());
				kycFillInfoMapper.updateByUkSelective(recordFillInfo);

				
				String emailTemplate = KycCertificateStatus.PASS.equals(request.getAddressStatus())
						? AccountConstants.USER_KYC_ADDRESS_AUDIT_SUCCESS
						: AccountConstants.USER_KYC_ADDRESS_AUDIT_REFUSED;
				sendAddressEmail(emailTemplate, userId, kycFillInfo, kycCertificate);
			}

			try {
				if (KycCertificateStatus.PASS.equals(request.getAddressStatus())
						|| KycCertificateStatus.REFUSED.equals(request.getAddressStatus())) {
					log.info("地址验证审核触发riskRating userId:{} addressStatus:{}", userId, request.getAddressStatus());
					RiskRatingChangeLevelEvent event = new RiskRatingChangeLevelEvent(this);
					event.setKycCertificate(kycCertificate);
					event.setTraceId(TrackingUtils.getTrace());
					event.setAddressPush(true);
					applicationEventPublisher.publishEvent(event);
				}
			} catch (Exception e) {
				log.warn("地址认证审核通过，修改riskRating异常. userId:{}", userId, e);
			}

			KycFlowContext.getContext().setKycCertificate(kycCertificate);
			KycFlowContext.getContext().setKycFillInfo(kycFillInfo);
			return new KycFlowResponse();
		} catch (BusinessException e) {
			log.error(String.format("地址认证执行异常 userId:%s", kycFlowRequest.getUserId()), e);
			throw e;
		} catch (Exception e) {
			log.error(String.format("地址认证执行异常 userId:%s", kycFlowRequest.getUserId()), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	private void sendAddressEmail(String emailTemplate, Long userId, KycFillInfo kycFillInfo,
			KycCertificate kycCertificate) {
		UserIndex userIndex = userIndexMapper.selectByPrimaryKey(userId);
		final User dbUser = this.userMapper.queryByEmail(userIndex.getEmail());
		LanguageEnum language = StringUtils.isEmpty(kycFillInfo.getCountry()) ? LanguageEnum.EN_US
				: LanguageEnum.findByLang(kycFillInfo.getCountry().toLowerCase());
		Map<String, Object> data = Maps.newHashMap();

		String reasonMsg;
		if (language == LanguageEnum.ZH_CN) {
			reasonMsg = userCommonBusiness.getJumioFailReason(kycCertificate.getAddressTips(), true);
		} else {
			reasonMsg = userCommonBusiness.getJumioFailReason(kycCertificate.getAddressTips(), false);
		}
		if (StringUtils.isNotBlank(reasonMsg)) {
			data.put("reason", reasonMsg);
		}
		log.info("发送地址审核邮件 userId:{} addressStatus:{} language:{} reasonMsg:{}", userId,
				kycCertificate.getAddressStatus(), language, reasonMsg);
		userCommonBusiness.sendEmailWithoutRequest(emailTemplate, dbUser, data, "地址认证结果邮件", language);

	}
}
