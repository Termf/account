package com.binance.account.service.kyc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.annotation.Resource;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.util.Base64Utils;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.util.IOUtils;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.common.enums.KycCertificateStatus;
import com.binance.account.common.enums.KycFillInfoGender;
import com.binance.account.service.certificate.impl.UserKycBusiness;
import com.binance.account.service.file.IFileStorage;
import com.binance.account.service.kyc.executor.us.AddressInfoSubmitExecutor;
import com.binance.account.service.kyc.executor.us.IdmInitExecutor;
import com.binance.account.service.kyc.executor.us.KycBindMobileExecutor;
import com.binance.account.vo.kyc.request.AddresAuthResultRequest;
import com.binance.account.vo.kyc.request.AddressInfoSubmitRequest;
import com.binance.account.vo.kyc.request.BaseInfoRequest;
import com.binance.account.vo.kyc.request.KycBindMobileRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.api.IdmApi;
import com.binance.inspector.vo.idm.response.BaseInfoKycIdmResponse;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIResponse;

public class KycFlowProcessFactoryTest extends BaseTest {

	@Resource
	AddressInfoSubmitExecutor addressInfoSubmitExecutor;

	@Resource
	KycFlowProcessFactory kycFlowProcessFactory;

	@Mock
	IFileStorage fileStorage;
	
	@Resource
	KycBindMobileExecutor kycBindMobileExecutor;
	
	@Resource
	IdmInitExecutor idmInitExecutor;
	
	@Mock
	IdmApi idmApi;

	
	@Resource
	UserKycBusiness userKycBusiness;
	@Test
	public void testCompanyBaseInfo() {
		BaseInfoRequest request = new BaseInfoRequest();
		request.setUserId(350462089L);
		request.setKycType(KycCertificateKycType.COMPANY);
		request.setSource(TerminalEnum.WEB);
		request.setCountry("us");
		request.setCompanyName("JUNIT company Name");
		request.setContactNumber("JUNIT contact number");
		request.setRegisterName("JUNIT register Name");

		KycFlowResponse response = kycFlowProcessFactory.getProcessor("base-info-submit").process(request);
		System.out.println(response);

	}

	@Test
	public void testUserBaseInfo() {
		BaseInfoRequest request = new BaseInfoRequest();
		request.setUserId(350462089L);
		request.setKycType(KycCertificateKycType.USER);
		request.setSource(TerminalEnum.WEB);
		request.setCountry("US");
		request.setFirstName("first");
		request.setLastName("last");
		request.setMiddleName("middle");
		request.setGender(KycFillInfoGender.MALE);
		request.setBirthday("1989-01-01");
		request.setTaxId("112398741");
		request.setRegionState("AS");
		request.setCity("city");
		request.setAddress("address");
		request.setPostalCode("43212");
		request.setNationality("nationality");

		KycFlowResponse response = kycFlowProcessFactory.getProcessor("base-info-submit").process(request);
		System.out.println(response);

	}
	
	@Test
	public void testUserBaseInfoIdmDeny() {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(idmInitExecutor, "idmApi", idmApi);
		BaseInfoKycIdmResponse response = new BaseInfoKycIdmResponse();
		response.setErrorCode("IDM_UNABLE_NAME");
		response.setFrp("DENY");
		response.setRes("DENY");
		response.setState("D");
		response.setMtid("343209439214321");
		Mockito.when(idmApi.baseInfoKycIdm(Mockito.any())).thenReturn(APIResponse.getOKJsonResult(response));
		
		BaseInfoRequest request = new BaseInfoRequest();
		request.setUserId(350462089L);
		request.setKycType(KycCertificateKycType.USER);
		request.setSource(TerminalEnum.WEB);
		request.setCountry("US");
		request.setFirstName("first");
		request.setLastName("last");
		request.setMiddleName("middle");
		request.setGender(KycFillInfoGender.MALE);
		request.setBirthday("2019-01-01");
		request.setTaxId("112398741");
		request.setRegionState("AS");
		request.setCity("city");
		request.setAddress("address");
		request.setPostalCode("43212");
		request.setNationality("nationality");

		KycFlowResponse kresponse = kycFlowProcessFactory.getProcessor("base-info-submit").process(request);
		System.out.println(kresponse);

	}

	@Test
	public void testAddress() throws Exception {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(addressInfoSubmitExecutor, "fileStorage", fileStorage);
		Mockito.doNothing().when(fileStorage).save(Mockito.any(), Mockito.any());

		InputStream in = new FileInputStream(new File("/Users/liufeng/Downloads/front2.jpeg"));
		byte[] images = IOUtils.toByteArray(in);

		String img = new String(Base64Utils.encode(images));

		AddressInfoSubmitRequest request = new AddressInfoSubmitRequest();
		request.setUserId(35024060L);
		request.setKycType(KycCertificateKycType.USER);
		request.setSource(TerminalEnum.WEB);
		request.setCountry("us");
		request.setBillFile(img);
		request.setBillFileName("lfo.jpeg");
		request.setRegionState("region");
		request.setCity("city");
		request.setAddress("address");
		request.setPostalCode("postalCode");
		KycFlowResponse response = kycFlowProcessFactory.getProcessor("address-info-submit").process(request);
		System.out.println(response);
		
	}
	
	@Test
	public void testAddressAuth() {
		AddresAuthResultRequest request = new AddresAuthResultRequest();
		request.setUserId(35024060L);
		request.setKycType(KycCertificateKycType.USER);
		request.setSource(TerminalEnum.WEB);
		request.setAddressStatus(KycCertificateStatus.PASS);
		request.setAddressTips("junit pass");
		KycFlowResponse response = kycFlowProcessFactory.getProcessor("address-auth-result").process(request);
		System.out.println(response);
	}

	@Test
	public void testSendSms() {
		KycBindMobileRequest request = new KycBindMobileRequest();
		request.setUserId(35024060L);
		request.setKycType(KycCertificateKycType.USER);
		request.setMobile("13681788274");
		request.setMobileCode("CN");
		request.setSource(TerminalEnum.WEB);
		kycBindMobileExecutor.sendBindMobileVerifyCode(request);
		request.setSmsCode("111111");
		KycFlowResponse response = kycFlowProcessFactory.getProcessor("kyc-bind-mobile").process(request);
		System.out.println(response);
	}
	
	@Test
	public void testUserBaseInfoMaster() {
		BaseInfoRequest request = new BaseInfoRequest();
		request.setUserId(350462089L);
		request.setKycType(KycCertificateKycType.USER);
		request.setSource(TerminalEnum.WEB);
		request.setCountry("US");
		request.setFirstName("first");
		request.setLastName("last");
		request.setMiddleName("middle");
		request.setGender(KycFillInfoGender.MALE);
		request.setBirthday("2019-01-01");
		request.setTaxId("112398741");
		request.setRegionState("AS");
		request.setCity("city");
		request.setAddress("address");
		request.setPostalCode("43212");
		request.setNationality("nationality");

		KycFlowResponse response = kycFlowProcessFactory.getProcessor("base-info-submit").process(request);
		System.out.println(response);

	}
	@Test
	public void test333() throws InterruptedException {
		userKycBusiness.syncUserKycCanAutoPass(380967555989565441l, 350462089l);
		Thread.sleep(60*60*1000);
		
	}
}
