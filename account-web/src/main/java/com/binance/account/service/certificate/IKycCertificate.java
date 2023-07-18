package com.binance.account.service.certificate;

import com.binance.account.common.enums.KycFillType;
import com.binance.account.common.query.KycCertificateQuery;
import com.binance.account.common.query.KycRefByNumberQuery;
import com.binance.account.common.query.KycRefQuery;
import com.binance.account.common.query.SearchResult;
import com.binance.account.data.entity.certificate.CertificateAuthResult;
import com.binance.account.data.entity.certificate.KycCertificate;
import com.binance.account.data.entity.certificate.KycFillInfo;
import com.binance.account.data.entity.certificate.UserKycApprove;
import com.binance.account.vo.certificate.response.KycRefQueryByNumberResponse;
import com.binance.account.vo.certificate.response.KycRefQueryResponse;
import com.binance.account.vo.certificate.response.UserKycCountryResponse;
import com.binance.account.vo.kyc.KycCertificateVo;
import com.binance.account.vo.kyc.KycFillInfoHistoryVo;
import com.binance.account.vo.kyc.KycFillInfoVo;
import com.binance.account.vo.kyc.request.AdditionalInfoRequest;
import com.binance.account.vo.kyc.request.DeleteKycNumberInfoRequest;
import com.binance.account.vo.kyc.request.FiatKycSyncStatusRequest;
import com.binance.account.vo.kyc.request.GetBaseInfoRequest;
import com.binance.account.vo.kyc.response.BaseInfoResponse;
import com.binance.account.vo.kyc.response.DeleteKycNumberInfoResponse;

import java.util.List;

public interface IKycCertificate {

	BaseInfoResponse getKycBaseInfo(GetBaseInfoRequest request);

	SearchResult<KycCertificateVo> getKycCertificateList(KycCertificateQuery query);

	KycCertificateVo getKycCertificateDetail(Long userId, boolean deepLoad);

	KycFillInfoVo getKycFillInfo(Long userId, KycFillType fillType);

	List<KycFillInfoHistoryVo> getKycFillInfoHistories(Long userId, KycFillType fillType);

	Boolean syncFiatPtStatus(FiatKycSyncStatusRequest request);

	SearchResult<KycRefQueryResponse> kycRefQuery(KycRefQuery kycRefQuery);

	SearchResult<KycRefQueryByNumberResponse> kycRefQueryByNumber(KycRefByNumberQuery kycRefByNumberQuery);

	DeleteKycNumberInfoResponse deleteKycNumberInfo(DeleteKycNumberInfoRequest deleteKycNumberInfoRequest);

	UserKycCountryResponse getKycCountry(Long userId) throws Exception;

	CertificateAuthResult getCertificateAuth(Long userId);

	void syncCertificateInfo(UserKycApprove userKycApprove, KycCertificate kycCertificate, KycFillInfo kycFillInfo);

	KycFillInfoVo additionalInfo(AdditionalInfoRequest request);
}
