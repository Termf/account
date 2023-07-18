package com.binance.account.service.certificate.impl;

import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.data.entity.certificate.JumioHandlerType;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.mapper.country.CountryMapper;
import com.binance.account.service.security.IFace;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.common.enums.JumioDocumentType;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.models.APIResponse.Status;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

@Component
public class KycAduitContextBusiness {

	@Resource
	private JumioBusiness jumioBusiness;

	@Resource
	private FaceIdApi faceIdApi;

	@Resource
	private IFace iFace;

	@Resource
	private CountryMapper countryMapper;
	
	@Resource
	private ApolloCommonConfig apolloCommonConfig;
	

	public UserKycAuditContext builder(UserKyc userKyc) {
		if (userKyc == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}

		Long userId = userKyc.getUserId();

		UserKycAuditContext context;
		APIResponse<FaceIdCardOcrVo> response = faceIdApi.getFaceIdCardOcr(APIRequest.instance(userId.toString()));

		if (userKyc.isOcrFlow()) {
			if (response == null || response.getStatus() != Status.OK) {
				return null;
			}
			FaceIdCardOcrVo vo = response.getData();


			context = UserKycAuditContext.builder().isOcrFlow(userKyc.isOcrFlow()).number(vo.getIdcardNumber())
					.country("CN").documentType(JumioDocumentType.ID_CARD.name()).ocrStatus(vo.getStatus())
					.firstName(vo.getName())
					.birthday(vo.getBirthYear() + "-" + vo.getBirthMonth() + "-" + vo.getBirthDay()).build();
		} else {
			
			JumioInfoVo jumioInfoVo = jumioBusiness.getByUserAndScanRef(userId, userKyc.getScanReference(),
					JumioHandlerType.USER_KYC.getCode());

			if (jumioInfoVo == null) {
				return null;
			}
			Country country = null;
			if (StringUtils.isNotBlank(jumioInfoVo.getIssuingCountry())) {
				country = countryMapper.selectByCode2(jumioInfoVo.getIssuingCountry());
			}
			context = UserKycAuditContext.builder().isOcrFlow(userKyc.isOcrFlow()).number(jumioInfoVo.getNumber())
					.country(country != null ? country.getCode() : null).documentType(jumioInfoVo.getDocumentType())
					.jumioId(userKyc.getJumioId()).scanReference(jumioInfoVo.getScanReference())
					.jumioStatus(jumioInfoVo.getStatus()).jumioSource(jumioInfoVo.getSource())
					.firstName(jumioInfoVo.getFirstName()).lastName(jumioInfoVo.getLastName())
					.birthday(jumioInfoVo.getDob()).build();
			
			if (response != null && response.getStatus() == Status.OK && response.getData() != null) {
				context.setOcrStatus(response.getData().getStatus());
			}
		}
		boolean isForbidCountry = apolloCommonConfig.isKycPassForbidCountry(context.getCountry());
		context.setForbidCountry(isForbidCountry);
		context.setUserId(userKyc.getUserId());
		context.setUserKyc(userKyc);
		return context;
	}

}
