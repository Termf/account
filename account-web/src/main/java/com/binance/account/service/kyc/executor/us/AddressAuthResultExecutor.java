package com.binance.account.service.kyc.executor.us;

import java.util.Date;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.AddressAuthValidator;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.utils.DateUtils;

import lombok.extern.log4j.Log4j2;

/**
 * ocr 异步通知
 * 
 * @author liufeng
 *
 */
@Service
@Log4j2
public class AddressAuthResultExecutor extends AbstractKycFlowCommonExecutor {

	@Autowired
	private AddressAuthValidator validator;

	@Value("${ocr.auto.delay.time:0}")
	private String ocrAutoDelayTime;

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

			if (KycCertificateStatus.PASS.equals(request.getAddressStatus())
					|| KycCertificateStatus.REFUSED.equals(request.getAddressStatus())) {
				KycFillInfo recordFillInfo = new KycFillInfo();
				recordFillInfo.setId(kycFillInfo.getId());
				recordFillInfo.setUserId(kycFillInfo.getUserId());
				recordFillInfo.setFillType(kycFillInfo.getFillType());
				if (KycCertificateStatus.PASS.equals(request.getAddressStatus())) {
					recordFillInfo.setStatus("SUCCESS");
				}
				
				Date updateTime = DateUtils.getNewUTCDate();
				if (!"0".equals(ocrAutoDelayTime)) {
					try {
						String[] timeRange = ocrAutoDelayTime.split(",",-1);
						int min = Integer.parseInt(timeRange[0]);
						int max = Integer.parseInt(timeRange[1]);
						Random random = new Random();
						int delayTime = random.nextInt(max)%(max-min+1) + min;
						updateTime = DateUtils.addMinutes(updateTime, delayTime);
					}catch(Exception e) {
					}
				}
				
				recordFillInfo.setUpdateTime(updateTime);
				kycFillInfoMapper.updateByUkSelective(recordFillInfo);
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
}
