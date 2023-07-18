package com.binance.account.service.kyc.executor.master;

import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.KycFillInfoHistory;
import com.binance.account.data.mapper.security.UserSecurityMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.mq.KycAddressOcrSender;
import com.binance.account.service.country.ICountry;
import com.binance.account.service.file.IFileStorage;
import com.binance.account.service.kyc.AbstractKycFlowCommonExecutor;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.service.kyc.executor.KycFlowContext;
import com.binance.account.service.kyc.validator.AddressInfoSubmitMasterValidator;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.account.vo.kyc.request.KycFlowRequest;
import com.binance.account.vo.kyc.response.AddressInfoSubmitResponse;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.api.OcrApi;
import com.binance.inspector.vo.ocr.request.OcrDetectTextRequest;
import com.binance.master.annotations.DefaultDB;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.utils.DateUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;

import javax.annotation.Resource;
import java.util.Random;

@Service
@Log4j2
public class AddressInfoSubmitMasterExecutor extends AbstractKycFlowCommonExecutor {

	@Resource
	public ICountry iCountry;

	@Resource
	private UserSecurityMapper userSecurityMapper;

	@Resource
	private IFileStorage fileStorage;

	@Resource
	private OcrApi ocrApi;

	public static final String IMAGE_PATH = "/ADDRESS_IMG";

	@Autowired
	private AddressInfoSubmitMasterValidator validator;
	
	@Resource
	private KycAddressOcrSender kycAddressOcrSender;
	
	@Autowired
	private ApolloCommonConfig config;

	@Override
	@Transactional(value = DefaultDB.TRANSACTION, propagation = Propagation.REQUIRES_NEW, rollbackFor = Exception.class)
	public KycFlowResponse execute(KycFlowRequest kycFlowRequest) {
		AddressInfoSubmitRequest request = (AddressInfoSubmitRequest) kycFlowRequest;

		validator.validateApiRequest(request);

		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(request.getUserId());

		if (kycCertificate == null) {
			log.warn("地址认证用户kyc certificate信息不存在 userId:{}", request.getUserId());
			throw new BusinessException(AccountErrorCode.KYC_CERTIFICATE_NOT_EXISTS);
		}

		validator.validateKycCertificateStatus(kycCertificate);
		validator.validateRequestCount(kycCertificate);

		kycCertificate.setAddressStatus(KycCertificateStatus.REVIEW.name());
		KycFlowContext.getContext().setKycCertificate(kycCertificate);

		Long userId = kycFlowRequest.getUserId();
		// 加入一个锁, 防止重复提交导致多次初始化的问题
		try {
			KycFlowResponse response = saveAddressInfo(userId, request);
			return response;
		} catch (BusinessException e) {
			log.error(String.format("提交地址信息处理异常 userId:%s", userId), e);
			throw e;
		} catch (Exception e) {
			log.error(String.format("提交地址信息处理异常 userId:%s", userId), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	private KycFlowResponse saveAddressInfo(Long userId, AddressInfoSubmitRequest request) {

		try {
			AddressInfoSubmitResponse response = new AddressInfoSubmitResponse();
			// 图片保存
			byte[] billFile = Base64Utils.decodeFromString(request.getBillFile());

			StringBuilder sb = new StringBuilder();
			int randomInt = new Random().nextInt(10000000);
			String fileExt = FilenameUtils.getExtension(request.getBillFileName());
			if(StringUtils.isNotBlank(fileExt)) {
				fileExt = fileExt.toLowerCase();
			}
			String fileName = sb.append(IMAGE_PATH).append("_")
					.append(DateUtils.formatter(DateUtils.getNewUTCDate(), DateUtils.SIMPLE_NUMBER_PATTERN)).append("/")
					.append(userId).append("_").append(randomInt).append(".").append(fileExt).toString();

			fileStorage.save(billFile, fileName);

			KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.ADDRESS.name());
			if (kycFillInfo == null) {
				kycFillInfo = KycCertificateConvertor.convert2KycFillInfo(null, request, fileName);
				kycFillInfoMapper.insert(kycFillInfo);
			} else {
				// 已有记录 则将fill_info 迁入history，并且删掉原纪录重新插入新纪录。
				KycFillInfoHistory kycFillInfoHistory = new KycFillInfoHistory();
				BeanUtils.copyProperties(kycFillInfo, kycFillInfoHistory);
				kycFillInfoHistoryMapper.insert(kycFillInfoHistory);
				kycFillInfoMapper.deleteByUk(kycFillInfo.getUserId(), kycFillInfo.getFillType());

				kycFillInfo = KycCertificateConvertor.convert2KycFillInfo(null, request, fileName);
				kycFillInfoMapper.insert(kycFillInfo);
			}
			KycFlowContext.getContext().setKycFillInfo(kycFillInfo);

			KycCertificate kycCertificate = KycFlowContext.getContext().getKycCertificate();
			KycCertificate record = new KycCertificate();
			record.setAddressStatus(kycCertificate.getAddressStatus());
			record.setUserId(kycCertificate.getUserId());
			kycCertificateMapper.updateStatus(record);

			KycFillInfo baseFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
			// 执行ocr
			doOcr(kycFillInfo, baseFillInfo);
			BeanUtils.copyProperties(kycFillInfo, response);
			response.setKycType(KycCertificateKycType.getByCode(kycCertificate.getKycType()));
			if (StringUtils.isNotBlank(kycFillInfo.getRegionState())) {
				CountryState countryState = CountryStateHelper.getCountryStateByPk(kycFillInfo.getCountry(),
						kycFillInfo.getRegionState());
				if (countryState != null) {
					response.setRegionState(countryState.getEn());
				}
				response.setRegionStateCode(kycFillInfo.getRegionState());
			}
			KycFlowContext.getContext().setKycCertificate(kycCertificate);
			KycFlowContext.getContext().setKycFillInfo(kycFillInfo);
			return response;
		} catch (Exception e) {
			log.error(String.format("地址提交处理异常 request:%s", request.toString()), e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}

	}

	private void doOcr(KycFillInfo address, KycFillInfo base) {
		OcrDetectTextRequest body = new OcrDetectTextRequest();
		BeanUtils.copyProperties(address, body);
		body.setImage(address.getBillFile());
		body.setFirstName(base.getFirstName());
		body.setMiddleName(base.getMiddleName());
		body.setLastName(base.getLastName());
		try {
			kycAddressOcrSender.notifyMq(body);
		} catch (Exception e) {
			ocrApi.detectText(APIRequest.instance(body));
			log.error(String.format("调用INSPECTOR执行OCR异常 request:%s", body.toString()), e);
		}

	}

}
