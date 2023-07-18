package com.binance.account.service.certificate.impl;

import com.alibaba.fastjson.JSON;
import com.binance.account.common.enums.CertificateType;
import com.binance.account.common.enums.CompanyCertificateStatus;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.enums.KycStatus;
import com.binance.account.common.query.KycCertificateQuery;
import com.binance.account.common.query.KycRefByNumberQuery;
import com.binance.account.common.query.KycRefQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.config.ApolloCommonConfig;
import com.binance.account.constants.AccountConstants;
import com.binance.account.data.entity.certificate.CertificateAuthResult;
import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.CountryState;
import com.binance.account.data.entity.certificate.JumioHandlerType;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.KycFillInfoHistory;
import com.binance.account.data.entity.certificate.UserCertificateIndex;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.data.entity.country.Country;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.entity.user.UserIndex;
import com.binance.account.data.mapper.certificate.CompanyCertificateMapper;
import com.binance.account.data.mapper.certificate.KycCertificateMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoHistoryMapper;
import com.binance.account.data.mapper.certificate.KycFillInfoMapper;
import com.binance.account.data.mapper.certificate.UserCertificateIndexMapper;
import com.binance.account.data.mapper.certificate.UserKycApproveMapper;
import com.binance.account.data.mapper.certificate.UserKycMapper;
import com.binance.account.data.mapper.user.UserIndexMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.certificate.IKycCertificate;
import com.binance.account.service.country.impl.CountryBusiness;
import com.binance.account.service.kyc.CountryStateHelper;
import com.binance.account.service.kyc.convert.KycCertificateConvertor;
import com.binance.account.service.kyc.executor.us.KycBindMobileExecutor;
import com.binance.account.service.security.impl.FaceBusiness;
import com.binance.account.vo.certificate.response.KycRefQueryByNumberResponse;
import com.binance.account.vo.certificate.response.KycRefQueryResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.country.CountryVo;
import com.binance.account.vo.kyc.JumioVo;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoHistoryVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.AdditionalInfoRequest;
import com.binance.account.vo.kyc.request.DeleteKycNumberInfoRequest;
import com.binance.account.vo.kyc.request.FiatKycSyncStatusRequest;
import com.binance.account.vo.kyc.request.GetBaseInfoRequest;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.DeleteKycNumberInfoResponse;
import com.binance.inspector.api.JumioAdminApi;
import com.binance.inspector.api.JumioApi;
import com.binance.inspector.common.enums.IdCardOcrStatus;
import com.binance.inspector.common.enums.JumioDocumentType;
import com.binance.inspector.common.enums.JumioStatus;
import com.binance.inspector.common.query.JumioAuditQuery;
import com.binance.inspector.common.query.KycValidateRefQuery;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.jumio.JumioInfoVo;
import com.binance.inspector.vo.jumio.KycRefQueryResultVo;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.DateUtils;
import com.binance.master.utils.RedisCacheUtils;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class KycCertificateBusiness implements IKycCertificate {

	@Resource
	KycBindMobileExecutor kycBindMobileExecutor;

	@Resource
	KycFillInfoMapper kycFillInfoMapper;

	@Resource
	KycCertificateMapper kycCertificateMapper;

	@Resource
	JumioApi jumioApi;
	@Resource
	private JumioAdminApi jumioAdminApi;

	@Resource
	private CountryBusiness countryBusiness;

	@Autowired
	private ApolloCommonConfig config;

	@Resource
	private UserMapper userMapper;

	@Resource
	private UserIndexMapper userIndexMapper;

	@Resource
	private KycFillInfoHistoryMapper kycFillInfoHistoryMapper;

	@Resource
	private UserCertificateIndexMapper userCertificateIndexMapper;

	@Resource
	private FaceBusiness faceBusiness;

	@Resource
	private JumioBusiness jumioBusiness;

	@Resource
	private CompanyCertificateMapper companyCertificateMapper;

	@Resource
	private UserKycMapper userKycMapper;

	@Resource
	private UserKycApproveMapper userKycApproveMapper;

	@Override
	public BaseInfoResponse getKycBaseInfo(GetBaseInfoRequest request) {
		Long userId = request.getUserId();
		KycFillType fillType = request.getFillType() == null ? KycFillType.BASE : request.getFillType();
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, fillType.name());
		if (kycFillInfo == null) {
			return null;
		}
		BaseInfoResponse response = KycCertificateConvertor.convert2BaseInfoResponse(kycCertificate, kycFillInfo);
		if (StringUtils.isNotBlank(kycFillInfo.getRegionState())) {
			CountryState countryState = CountryStateHelper.getCountryStateByPk(kycFillInfo.getCountry(),
					kycFillInfo.getRegionState());
			if (countryState != null) {
				response.setRegionState(countryState.getEn());
			}
			response.setRegionStateCode(kycFillInfo.getRegionState());
		}

		UserIndex index = userIndexMapper.selectByPrimaryKey(userId);
		if (index != null) {
			response.setEmail(index.getEmail());
		}
		return response;
	}

	public SearchResult<KycCertificateVo> getKycCertificateList(KycCertificateQuery query) {

		String email = query.getEmail();
		if (StringUtils.isNotBlank(email)) {
			query.setEmail(email.toLowerCase());
			User user = this.userMapper.queryByEmail(query.getEmail());
			if (user == null) {
				throw new BusinessException(GeneralCode.USER_NOT_EXIST);// 账号不存在
			}
			query.setUserId(user.getUserId());
		}

		long total = kycCertificateMapper.getListCount(query);
		if (total <= 0) {
			return new SearchResult<>(Collections.emptyList(), 0);
		} else {
			List<KycCertificate> list = kycCertificateMapper.getList(query);
			if (list == null || list.isEmpty()) {
				return new SearchResult<>(Collections.emptyList(), total);
			}
			List<KycCertificateVo> vos = list.stream()
					.map(item -> KycCertificateConvertor.convert2KycCertificateVo(item)).collect(Collectors.toList());

			if (StringUtils.isNotBlank(email)) {
				for (KycCertificateVo kycCertificateVo : vos) {
					kycCertificateVo.setEmail(email);
				}
				return new SearchResult<>(vos, total);
			}

			List<Long> userIds = vos.stream().map(KycCertificateVo::getUserId).collect(Collectors.toList());
			List<UserIndex> userIndexs = userIndexMapper.selectByUserIds(userIds);
			if (userIndexs == null || userIndexs.isEmpty()) {
				return new SearchResult<>(vos, total);
			}
			// 转成map,key=userId,value=email
			Map<Long, String> emailMap = userIndexs.stream().collect(Collectors.toMap(UserIndex::getUserId, UserIndex::getEmail));
			vos.forEach( v -> {
				v.setEmail(emailMap.get(v.getUserId()));
			});
			return new SearchResult<>(vos, total);
		}
	}

	public KycCertificateVo getKycCertificateDetail(Long userId, boolean deepLoad) {
		try {
			if (userId == null) {
				return null;
			}
			KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
			KycCertificateVo kycCertificateVo = KycCertificateConvertor.convert2KycCertificateVo(kycCertificate);

			if (kycCertificateVo == null) {
				return null;
			}

			if (deepLoad) {
				KycFillInfoVo baseInfo = getKycFillInfo(userId, KycFillType.BASE);
				KycFillInfoVo addressInfo = getKycFillInfo(userId, KycFillType.ADDRESS);

				kycCertificateVo.setAddressInfo(addressInfo);
				kycCertificateVo.setBaseInfo(baseInfo);

				JumioAuditQuery query = new JumioAuditQuery();
				query.setUserId(userId);
				APIResponse<JumioInfoVo> response = jumioApi.getLastJumio(APIRequest.instance(new Long(userId)));
				if (response != null && response.getStatus() == APIResponse.Status.OK && response.getData() != null) {
					JumioInfoVo vo = response.getData();
					JumioVo jumioVo = new JumioVo();
					BeanUtils.copyProperties(vo, jumioVo);
					jumioVo.setJumioStatus(vo.getStatus().name());
					kycCertificateVo.setJumioVo(jumioVo);
				}
			}
			return kycCertificateVo;
		} catch (Exception e) {
			log.error("查询kyc_certificate信息异常 userId: {}", userId, e);
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
	}

	public KycFillInfoVo getKycFillInfo(Long userId, KycFillType fillType) {
		if (userId == null || fillType == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, fillType.name());
		if (kycFillInfo == null) {
			return null;
		}
		return kycFillInfoConvert2Vo(kycFillInfo);
	}

	private KycFillInfoVo kycFillInfoConvert2Vo(KycFillInfo kycFillInfo) {
		KycFillInfoVo vo = new KycFillInfoVo();
		BeanUtils.copyProperties(kycFillInfo, vo);
		if (StringUtils.isNotBlank(kycFillInfo.getRegionState())) {
			vo.setRegionStateCode(kycFillInfo.getRegionState());
			CountryState countryState = CountryStateHelper.getCountryStateByPk(kycFillInfo.getCountry(),
					kycFillInfo.getRegionState());
			if (countryState != null) {
				vo.setRegionState(countryState.getEn());
			}
		}
		return vo;
	}

	public List<KycFillInfoHistoryVo> getKycFillInfoHistories(Long userId, KycFillType fillType) {
		if (userId == null || fillType == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		List<KycFillInfoHistory> histories = kycFillInfoHistoryMapper.getHistories(userId, fillType.name());
		if (histories == null || histories.isEmpty()) {
			return Collections.emptyList();
		}
		List<KycFillInfoHistoryVo> voList = histories.stream().map(item -> {
			KycFillInfoHistoryVo vo = new KycFillInfoHistoryVo();
			BeanUtils.copyProperties(item, vo);
			return vo;
		}).collect(Collectors.toList());
		return voList;
	}

	public Boolean syncFiatPtStatus(FiatKycSyncStatusRequest request) {
		Long userId = request.getUserId();
		if (userId == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (StringUtils.isAnyBlank(request.getFiatPtStatus(), request.getFiatPtTips())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		KycCertificate record = new KycCertificate();
		record.setUserId(userId);
		record.setFiatPtStatus(request.getFiatPtStatus());
		record.setFiatPtTips(request.getFiatPtTips());
		record.setUpdateTime(DateUtils.getNewUTCDate());
		int i = kycCertificateMapper.updateFiatPtStatus(record);
		return i > 0;
	}

	public SearchResult<KycRefQueryResponse> kycRefQuery(KycRefQuery kycRefQuery) {
		if (kycRefQuery == null || kycRefQuery.getQueryType() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		SearchResult<KycRefQueryResponse> result;
		switch (kycRefQuery.getQueryType()) {
		case BASIC:
			result = kycRefQueryBasic(kycRefQuery);
			break;
		case JUMIO:
			result = kycRefQueryFromValidate(kycRefQuery, KycValidateRefQuery.KycValidateRefQueryType.JUMIO);
			break;
		case FACE_OCR:
			result = kycRefQueryFromValidate(kycRefQuery, KycValidateRefQuery.KycValidateRefQueryType.FACE_OCR);
			break;
		default:
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		// 查询和显示用户当前的kyc等级和类型
		if (result.getRows() != null && !result.getRows().isEmpty()) {
			result.getRows().stream().forEach(item -> {
				KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(item.getUserId());
				if (kycCertificate != null) {
					KycCertificateKycType kycType = kycCertificate.getKycType() == null ? null
							: KycCertificateKycType.getByCode(kycCertificate.getKycType());
					item.setKycType(kycType == null ? null : kycType.name());
					item.setKycLevel(kycCertificate.getKycLevel());
				}
			});
		}
		return result;
	}

	public SearchResult<KycRefQueryByNumberResponse> kycRefQueryByNumber(KycRefByNumberQuery kycRefByNumberQuery) {
		if (kycRefByNumberQuery == null || kycRefByNumberQuery.getNumber() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		List<UserCertificateIndex> list = userCertificateIndexMapper
				.selectCertificateByNumber(kycRefByNumberQuery.getNumber());
		List<KycRefQueryByNumberResponse> rows = new ArrayList<>(4);
		list.forEach(item -> {
			KycRefQueryByNumberResponse kycRefQueryByNumberResponse = new KycRefQueryByNumberResponse();
			BeanUtils.copyProperties(item, kycRefQueryByNumberResponse);
			rows.add(kycRefQueryByNumberResponse);
		});
		SearchResult<KycRefQueryByNumberResponse> result = new SearchResult<>(rows, rows.size());
		return result;
	}

	public DeleteKycNumberInfoResponse deleteKycNumberInfo(DeleteKycNumberInfoRequest deleteKycNumberInfoRequest) {
		// 请求参数校验
		if (deleteKycNumberInfoRequest == null || deleteKycNumberInfoRequest.getUserId() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		String number = deleteKycNumberInfoRequest.getNumber();
		String country = deleteKycNumberInfoRequest.getCountry();
		String type = deleteKycNumberInfoRequest.getType();
		Long userId = deleteKycNumberInfoRequest.getUserId();
		userCertificateIndexMapper.deleteConsiderTypeNull(number, country, type, userId);
		DeleteKycNumberInfoResponse response = new DeleteKycNumberInfoResponse();
		response.setDeleteResult(true);
		return response;
	}

	public UserKycCountryResponse getKycCountry(Long userId) throws Exception {
		if (userId == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		UserKycCountryResponse response = new UserKycCountryResponse();
		// default
		response.setUserId(userId);
		response.setCertificateType(CertificateType.UNVERIFIED.getCode());
		response.setCountryCode(null);

		String redisKey = String.format(AccountConstants.KYC_COUNTRY_CACHE_PRE, userId);

		try {
			// step1: 从redis 缓存获取，获取不到再从库表考虑
			UserKycCountryResponse redisCache = RedisCacheUtils.get(redisKey, UserKycCountryResponse.class);
			if (redisCache != null) {
				log.info("获取country成功.userId:{},response:{}", userId, redisCache);
				return redisCache;
			}

			// step2: 缓存获取不到时，从根据数据查询来获取

			KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);
			// 老流程从 approve 表查询记录 没有则返回空。新逻辑判状态是否为pass 如果不是pass则返回
			if (kycCertificate == null || !KycCertificateStatus.PASS.name().equals(kycCertificate.getStatus())) {
				// 当前用户就没有通过KYC, 做Redis缓存(120s)后返回默认值,
				RedisCacheUtils.set(redisKey, response, config.getKycCountryCacheTimes());
				return response;
			}
			KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());

			response.setFirstName(kycFillInfo.getFirstName());
			response.setLastName(kycFillInfo.getLastName());

			switch (KycCertificateKycType.getByCode(kycCertificate.getKycType())) {
			case USER:
				String fillCountry = kycFillInfo.getCountry();
				response.setCountryCode(fillCountry);
				APIResponse<JumioInfoVo> resInspect = jumioApi.getLastJumio(APIRequest.instance(userId));
				if (resInspect != null && resInspect.getData() != null) {
					JumioInfoVo jumioInfoVo = resInspect.getData();
					// jumio 没有则用用户填写的country，如果jumio 有则用jumio
					CountryVo countryVo = countryBusiness.getCountryByAlpha3WithCache(jumioInfoVo.getIssuingCountry());

					response.setCountryCode(countryVo == null ? fillCountry : countryVo.getCode());
					response.setIdNumber(jumioInfoVo.getNumber());
					response.setDocumentType(jumioInfoVo.getDocumentType());
					response.setBirthday(jumioInfoVo.getDob());
					response.setFirstName(jumioInfoVo.getFirstName());
					response.setLastName(jumioInfoVo.getLastName());
				}
				response.setCertificateType(CertificateType.USER.getCode());
				if ("CN".equalsIgnoreCase(response.getCountryCode())
						|| StringUtils.isNotBlank(kycCertificate.getFaceOcrStatus())) {
					// 中国的用户的话，优先考虑从face ocr 中获取
					FaceIdCardOcrVo ocrVo = faceBusiness.getFaceIdCardOcr(userId);
					if (ocrVo != null && IdCardOcrStatus.PASS.equals(ocrVo.getStatus())) {
						response.setIdNumber(ocrVo.getIdcardNumber());
						response.setDocumentType(JumioDocumentType.ID_CARD.name());
						response.setCanDoC2C(true);
						response.setBirthday(UserKycHelper.idCardOcrBirthday(ocrVo.getBirthYear(),
								ocrVo.getBirthMonth(), ocrVo.getBirthDay()));
						response.setFirstName(ocrVo.getName());
						response.setCountryCode("CN");
					}
				}
				break;
			case COMPANY:
				response.setCertificateType(CertificateType.COMPANY.getCode());
				response.setCountryCode(kycFillInfo.getCountry());
				break;
			default:
				break;
			}

			response.setCity(kycFillInfo.getCity());
			response.setAddress(kycFillInfo.getAddress());
			response.setFillFirstName(kycFillInfo.getFirstName());
			response.setFillMiddleName(kycFillInfo.getMiddleName());
			response.setFillLastName(kycFillInfo.getLastName());
			response.setFillBirthday(kycFillInfo.getBirthday());
			RedisCacheUtils.set(redisKey, response, config.getKycCountryCacheTimes());
			log.info("获取country成功.userId:{},response:{}", userId, response);
			return response;
		} catch (Exception e) {
			log.error("获取用户KYC国籍异常: userId:{}", userId, e);
			throw e;
		}

	}

	public CertificateAuthResult getCertificateAuth(Long userId) {
		KycCertificate kycCertificate = kycCertificateMapper.selectByPrimaryKey(userId);

		if (kycCertificate != null) {
			return getCertificateAuthNewVersion(kycCertificate);
		}

		// KYC 取没有过期的最后一条
		CompanyCertificate companyCertificate = companyCertificateMapper.getLast(userId);
		if (companyCertificate != null && CompanyCertificateStatus.delete == companyCertificate.getStatus()) {
			// 如果是删除状态的，就当作为空值
			companyCertificate = null;
		}
		// jumioKYC 取没有过期的最后一条
		UserKyc userKyc = userKycMapper.getLast(userId);
		if (userKyc != null && (KycStatus.delete == userKyc.getStatus() || KycStatus.basic == userKyc.getStatus())) {
			// 如果是删除状态的，就当作为空值
			userKyc = null;
		}
		// 获取最后一次认证时间
		Long companyTime = (companyCertificate == null || companyCertificate.getUpdateTime() == null) ? null
				: companyCertificate.getUpdateTime().getTime();
		Long userKycTime = (userKyc == null || userKyc.getUpdateTime() == null) ? null
				: userKyc.getUpdateTime().getTime();

		if (companyTime != null) {
			if (userKycTime != null && userKycTime > companyTime) {
				return setUserKycCertificate(userKyc);
			} else {
				return setCompanyKycCertificate(companyCertificate);
			}
		}
		if (userKycTime != null) {
			return setUserKycCertificate(userKyc);
		}
		return null;
	}

	public void syncCertificateInfo(UserKycApprove userKycApprove, KycCertificate kycCertificate,
			KycFillInfo kycFillInfo) {
		Long userId = userKycApprove.getUserId();
		if (StringUtils.isNotBlank(userKycApprove.getCertificateFirstName())
				|| StringUtils.isNotBlank(userKycApprove.getCertificateLastName())) {
			return;
		}

		if (StringUtils.isNotBlank(kycCertificate.getFaceOcrStatus())) {
			userKycApprove.setCertificateFirstName(kycFillInfo.getFirstName());
			userKycApprove.setCertificateLastName(kycFillInfo.getLastName());
			userKycApprove.setCertificateDob(kycFillInfo.getBirthday());
			userKycApprove.setCertificateCountry(kycFillInfo.getCountry());
			userKycApproveMapper.updateCertificateInfo(userKycApprove);
			return;
		}

		String scanReference = userKycApprove.getScanReference();

		JumioInfoVo jumioInfo = null;

		if (StringUtils.isNotBlank(scanReference)) {
			jumioInfo = jumioBusiness.getByUserAndScanRef(userId, scanReference,
					KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())
							? JumioHandlerType.USER_KYC.getCode()
							: JumioHandlerType.COMPANY_KYC.getCode());
		} else {
			jumioInfo = jumioBusiness.getLastByUserId(userId);
		}

		if (jumioInfo == null) {
			log.info("同步approve证件信息 jumio信息为空,取base信息作为证件识别信息 userId:{} scanRef:{}", userId, scanReference);
			userKycApprove.setCertificateFirstName(kycFillInfo.getFirstName());
			StringBuffer lastName = new StringBuffer()
					.append(StringUtils.isBlank(kycFillInfo.getMiddleName()) ? "" : kycFillInfo.getMiddleName() + " ")
					.append(StringUtils.isBlank(kycFillInfo.getLastName()) ? "" : kycFillInfo.getLastName());
			userKycApprove.setCertificateLastName(lastName.toString());
			userKycApprove.setCertificateDob(kycFillInfo.getBirthday());
			userKycApprove.setCertificateCountry(kycFillInfo.getCountry());
		} else {
			log.info("同步approve证件信息 jumio信息不空 userId:{} scanReference:{}", userId, scanReference);
			userKycApprove.setCertificateFirstName(StringUtils.isBlank(jumioInfo.getFirstName()) ? ""
					: jumioInfo.getFirstName().trim().replaceAll("N/A", ""));
			userKycApprove.setCertificateLastName(StringUtils.isBlank(jumioInfo.getLastName()) ? ""
					: jumioInfo.getLastName().trim().replaceAll("N/A", ""));
			userKycApprove.setCertificateDob(jumioInfo.getDob());
			CountryVo countryVo = countryBusiness.getCountryByAlpha3(jumioInfo.getIssuingCountry());
			userKycApprove.setCertificateCountry(countryVo.getCode());
		}

		userKycApproveMapper.updateCertificateInfo(userKycApprove);

	}

	private SearchResult<KycRefQueryResponse> kycRefQueryFromValidate(KycRefQuery kycRefQuery,
			KycValidateRefQuery.KycValidateRefQueryType refQueryType) {
		KycValidateRefQuery refQuery = new KycValidateRefQuery();
		BeanUtils.copyProperties(kycRefQuery, refQuery);
		refQuery.setQueryType(refQueryType);
		if (StringUtils.isNotBlank(kycRefQuery.getCountryCode())) {
			Country country = countryBusiness.getCountryByCode(kycRefQuery.getCountryCode());
			if (country == null) {
				throw new BusinessException(GeneralCode.ILLEGAL_PARAM, "Country Code error");
			}
			refQuery.setCountryCode(country.getCode());
			refQuery.setCountryCode2(country.getCode2());
		}
		APIResponse<com.binance.inspector.common.query.SearchResult<KycRefQueryResultVo>> response = jumioAdminApi
				.kycRefQuery(APIRequest.instance(refQuery));
		if (response == null || response.getStatus() != APIResponse.Status.OK) {
			log.warn("query kyc ref info fail.", JSON.toJSONString(response));
			throw new BusinessException(GeneralCode.SYS_ERROR);
		}
		List<KycRefQueryResponse> responses = response.getData().getRows().stream().map(item -> {
			KycRefQueryResponse vo = new KycRefQueryResponse();
			BeanUtils.copyProperties(item, vo);
			if (StringUtils.isNotBlank(vo.getCountryCode()) && vo.getCountryCode().length() > 2) {
				Country country = countryBusiness.getCountryByCode(vo.getCountryCode());
				if (country != null) {
					vo.setCountryCode(country.getCode());
				} else {
					vo.setCountryCode(null);
				}
			}
			return vo;
		}).collect(Collectors.toList());
		return new SearchResult<>(responses, response.getData().getTotal());
	}

	private SearchResult<KycRefQueryResponse> kycRefQueryBasic(KycRefQuery kycRefQuery) {
		// basic 中的数据查询,
		long count = kycFillInfoMapper.kycRefQueryCount(kycRefQuery);
		if (count <= 0) {
			return new SearchResult<>(Collections.emptyList(), 0L);
		}
		List<KycFillInfo> list = kycFillInfoMapper.kycRefQueryList(kycRefQuery);
		List<KycRefQueryResponse> responses = list.stream().map(item -> {
			KycRefQueryResponse queryResponse = new KycRefQueryResponse();
			queryResponse.setUserId(item.getUserId());
			queryResponse.setFirstName(item.getFirstName());
			queryResponse.setMiddleName(item.getMiddleName());
			queryResponse.setLastName(item.getLastName());
			queryResponse.setCountryCode(item.getCountry());
			queryResponse.setCompanyName(item.getCompanyName());
			queryResponse.setCreateTime(item.getCreateTime());
			queryResponse.setUpdateTime(item.getUpdateTime());
			queryResponse.setTaxId(item.getTaxId());
			queryResponse.setRegionState(item.getRegionState());
			queryResponse.setBirthday(item.getBirthday());
			return queryResponse;
		}).collect(Collectors.toList());
		return new SearchResult<>(responses, count);
	}

	private CertificateAuthResult getCertificateAuthNewVersion(KycCertificate kycCertificate) {
		CertificateAuthResult result = new CertificateAuthResult();
		result.setNewVersion(true);
		result.setCertificateType(KycCertificateKycType.USER.getCode().equals(kycCertificate.getKycType())
				? CertificateAuthResult.TYPE_USER
				: CertificateAuthResult.TYPE_COMPANY);
		KycCertificateStatus status = KycCertificateStatus.valueOf(kycCertificate.getStatus());

		switch (status) {
		case PASS:
			result.setStatus(CertificateAuthResult.STATUS_PASS);
			break;
		case REFUSED:
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
			break;
		case FORBID_PASS:
			result.setStatus(CertificateAuthResult.STATUS_PASS);
			result.setForbidPassed(true);
			break;
		default:
			break;
		}

		if (StringUtils.isNotBlank(result.getStatus())) {
			return result;
		}
		KycCertificateStatus authStatus = null;

		if (StringUtils.isNotBlank(kycCertificate.getJumioStatus())) {
			result.setSoucre(CertificateAuthResult.JUMIO);
			authStatus = KycCertificateStatus.valueOf(kycCertificate.getJumioStatus());
		} else if (StringUtils.isNotBlank(kycCertificate.getFaceOcrStatus())) {
			result.setSoucre(CertificateAuthResult.FACE_OCR);
			authStatus = KycCertificateStatus.valueOf(kycCertificate.getFaceOcrStatus());
		} else {
			return result;
		}

		switch (authStatus) {
		case PASS:
			result.setStatus(CertificateAuthResult.STATUS_PASS);
			return result;
		case REFUSED:
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
			return result;
		default:
			break;
		}

		if (CertificateAuthResult.FACE_OCR.equals(result.getSoucre())) {
			result.setStatus(CertificateAuthResult.STATUS_REVIEW);
			return result;
		}

		JumioInfoVo vo = jumioBusiness.reuseCurrentJumio(kycCertificate.getUserId());
		if (vo == null) {
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		} else {
			if (JumioStatus.PASSED.equals(vo.getStatus())) {
				result.setStatus(CertificateAuthResult.STATUS_PASS);
			} else {
				result.setStatus(CertificateAuthResult.STATUS_REVIEW);
			}
		}
		return result;
	}

	private CertificateAuthResult setUserKycCertificate(UserKyc userKyc) {
		if (userKyc == null || KycStatus.delete == userKyc.getStatus()) {
			return null;
		}
		CertificateAuthResult result = new CertificateAuthResult();
		result.setCertificateType(CertificateAuthResult.TYPE_USER);
		if (userKyc.getStatus() == KycStatus.passed) {
			result.setStatus(CertificateAuthResult.STATUS_PASS);
		} else if (userKyc.getStatus() == KycStatus.refused) {
			// 20190624 拒绝和不合规国籍通过状态的数据都归并为拒绝状态
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		} else if (userKyc.getStatus() == KycStatus.forbidPassed) {
			result.setStatus(CertificateAuthResult.STATUS_PASS);
			result.setForbidPassed(true);
		} else if (userKyc.getStatus() == KycStatus.jumioPassed) {
			result.setSoucre(CertificateAuthResult.JUMIO);
			result.setStatus(CertificateAuthResult.STATUS_PASS);
		} else if (userKyc.getStatus() == KycStatus.jumioRefused) {
			result.setSoucre(CertificateAuthResult.JUMIO);
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		}

		if (StringUtils.isNotBlank(result.getStatus())) {
			return result;
		}

		if (StringUtils.isNotBlank(userKyc.getFaceOcrStatus())) {
			result.setSoucre(CertificateAuthResult.FACE_OCR);

			if (IdCardOcrStatus.REFUSED.name().equals(userKyc.getFaceOcrStatus())) {
				result.setStatus(CertificateAuthResult.STATUS_REFUSED);
			}

			if (IdCardOcrStatus.PASS.name().equals(userKyc.getFaceOcrStatus())) {
				result.setStatus(CertificateAuthResult.STATUS_PASS);
			}
			result.setStatus(CertificateAuthResult.STATUS_REVIEW);
			return result;
		}

		result.setSoucre(CertificateAuthResult.JUMIO);
		JumioInfoVo vo = jumioBusiness.reuseCurrentJumio(userKyc.getUserId());
		if (vo == null) {
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		} else {
			if (JumioStatus.PASSED.equals(vo.getStatus())) {
				result.setStatus(CertificateAuthResult.STATUS_PASS);
			} else {
				result.setStatus(CertificateAuthResult.STATUS_REVIEW);
			}
		}

		return result;
	}

	/**
	 * 企业认证
	 *
	 * @param companyCertificate
	 */
	private CertificateAuthResult setCompanyKycCertificate(CompanyCertificate companyCertificate) {
		if (companyCertificate == null || CompanyCertificateStatus.delete == companyCertificate.getStatus()) {
			return null;
		}
		CertificateAuthResult result = new CertificateAuthResult();
		result.setCertificateType(CertificateAuthResult.TYPE_COMPANY);

		if (companyCertificate.getStatus() == CompanyCertificateStatus.passed) {
			result.setStatus(CertificateAuthResult.STATUS_PASS);
		} else if (companyCertificate.getStatus() == CompanyCertificateStatus.refused) {
			// 20190624 拒绝和不合规国籍通过状态的数据都归并为拒绝状态
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		} else if (companyCertificate.getStatus() == CompanyCertificateStatus.forbidPassed) {
			result.setStatus(CertificateAuthResult.STATUS_PASS);
			result.setForbidPassed(true);
		} else if (companyCertificate.getStatus() == CompanyCertificateStatus.jumioPassed) {
			result.setStatus(CertificateAuthResult.STATUS_PASS);
		} else if (companyCertificate.getStatus() == CompanyCertificateStatus.jumioRefused) {
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		}
		result.setSoucre(CertificateAuthResult.JUMIO);

		if (StringUtils.isNotBlank(result.getStatus())) {
			return result;
		}

		JumioInfoVo vo = jumioBusiness.reuseCurrentJumio(companyCertificate.getUserId());
		if (vo == null) {
			result.setStatus(CertificateAuthResult.STATUS_REFUSED);
		} else {
			if (JumioStatus.PASSED.equals(vo.getStatus())) {
				result.setStatus(CertificateAuthResult.STATUS_PASS);
			} else {
				result.setStatus(CertificateAuthResult.STATUS_REVIEW);
			}
		}
		return result;
	}

	@Override
	public KycFillInfoVo additionalInfo(AdditionalInfoRequest request) {
		if (request == null || request.getUserId() == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (StringUtils.isAllBlank(request.getPostalCode(), request.getIssuingAuthority())) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		Long userId = request.getUserId();
		log.info("kyc additional base info by userId:{} {}", userId, request);
		KycFillInfo kycFillInfo = kycFillInfoMapper.selectByUserIdFillType(userId, KycFillType.BASE.name());
		if (kycFillInfo == null) {
			throw new BusinessException(GeneralCode.ILLEGAL_PARAM);
		}
		if (StringUtils.isNotBlank(request.getPostalCode())) {
			kycFillInfo.setPostalCode(request.getPostalCode());
		}
		if (StringUtils.isNotBlank(request.getIssuingAuthority())) {
			kycFillInfo.setIssuingAuthority(request.getIssuingAuthority());
		}
		if (StringUtils.isNotBlank(request.getExpiryDate())) {
			kycFillInfo.setExpiryDate(request.getExpiryDate());
		}
		kycFillInfo.setUpdateTime(DateUtils.getNewUTCDate());
		kycFillInfoMapper.updateAdditionalByUk(kycFillInfo);
		return kycFillInfoConvert2Vo(kycFillInfo);
	}
}
