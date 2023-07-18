package com.binance.account.service.certificate;

import com.binance.account.data.entity.certificate.CompanyCertificate;
import com.binance.account.data.entity.certificate.UserKyc;
import com.binance.account.data.entity.certificate.UserKycApprove;

import java.util.List;

public interface IUserKycDataMigration {

	List<UserKycApprove> selectPage(String moveMsg,int start,int rows);

	void moveToKycCertificateByUserId(Long userId);

	void moveToKycCertificate(UserKycApprove userKycApprove);

	void addExceptionTag(UserKycApprove userKycApprove);

	List<UserKycApprove> selectFaceCheckPage(String faceCheck, int start, int rows, Long userId);

	String checkKycFaceCheck(UserKycApprove userKycApprove);
	
	List<UserKyc> selectUserPage(int start,int rows);
	
	List<CompanyCertificate> selectCompanyPage(int start,int rows);
	
	void moveUserKyc(UserKyc userKyc);
	
	void moveCompany(CompanyCertificate companyCertificate);
	
	void expiredUser(UserKyc userKyc, String memo);
	
	void expiredCompany(CompanyCertificate companyCertificate, String memo);
	
}
