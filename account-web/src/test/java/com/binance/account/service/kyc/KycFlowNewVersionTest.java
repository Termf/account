package com.binance.account.service.kyc;

import java.util.Date;

import javax.annotation.Resource;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.binance.account.controller.certificate.UserKycController;
import com.binance.account.controller.security.UserFaceController;
import com.binance.account.data.entity.security.TransactionFaceLog;
import com.binance.account.data.mapper.security.TransactionFaceLogMapper;
import com.binance.account.service.certificate.impl.UserCertificateBusiness;
import com.binance.account.service.face.handler.UserKycFaceHandler;
import com.binance.account.service.security.impl.FaceBusiness;
import com.binance.account.vo.face.request.FaceInitRequest;
import com.binance.account.vo.face.request.FacePcResultRequest;
import com.binance.account.vo.face.request.TransFaceInitRequest;
import com.binance.account.vo.face.response.FaceInitResponse;
import com.binance.account.vo.user.UserKycVo;
import com.binance.account.vo.user.request.KycBaseInfoRequest;
import com.binance.account.vo.user.response.JumioTokenResponse;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.inspector.vo.faceid.response.FaceWebResultResponse;
import com.binance.master.enums.LanguageEnum;
import com.binance.master.enums.TerminalEnum;
import com.binance.master.models.APIRequest;
import com.binance.master.models.APIResponse;

public class KycFlowNewVersionTest extends BaseTest {

	@Resource
	private KycApiTransferAdapter kycApiTransferAdapter;

	@Resource
	private UserFaceController userFaceController;

	@Resource
	private TransactionFaceLogMapper transactionFaceLogMapper;

	@Mock
	UserKycFaceHandler userKycFaceHandler;
	
	@Resource
	FaceBusiness faceBusiness;
	@Mock
	FaceIdApi faceIdApi;
	
	@Resource
	UserKycController userKycController;
	
	@Resource
	UserCertificateBusiness userCertificateBusiness;
	
	@Test
	public void testBaseInfoSubmit() throws Exception {
		KycBaseInfoRequest request = new KycBaseInfoRequest();
		request.setUserId(350462089l);

		UserKycVo.BaseInfo baseInfo = new UserKycVo.BaseInfo();
//		{'country':'CN','firstName':'冬梅','lastName':'王','address':'广西壮族自治区荆门县长寿任路G座 999037','nationality':'科摩罗','city':'东莞县','dob':'1981-04-11T18:24:08','postalCode':'915528'}
		baseInfo.setCountry("CN");
		baseInfo.setFirstName("峰");
		baseInfo.setLastName("刘");
		baseInfo.setAddress("上海市浦东新区浦电路1000号");
		baseInfo.setNationality("上海");
		baseInfo.setCity("上海");
		baseInfo.setDob(new Date());
		baseInfo.setPostalCode("915528");
		request.setBaseInfo(baseInfo);
		APIResponse<JumioTokenResponse> response = kycApiTransferAdapter
				.submitUserBaseInfo(APIRequest.instance(request));
		System.out.println(response);
	}

	@Test
	public void initFaceFlowByTransId() {
		TransFaceInitRequest request = new TransFaceInitRequest();
		request.setKycLockOne(true);
		request.setNeedEmail(false);
		request.setTransId("63e2da9b03a448fa8ce6500dab8b04f9");
		request.setType("user");
		request.setUserId(350462089l);
		userFaceController.initFaceFlowByTransId(APIRequest.instance(request));
	}

	@Test
	public void facePcInit() {
		MockitoAnnotations.initMocks(this);
		TransactionFaceLog faceLog = transactionFaceLogMapper.findLastByUserId(350462089l, "KYC_USER", null);
		FaceInitRequest request = new FaceInitRequest();
		request.setTransId(faceLog.getTransId());
		request.setType(FaceTransType.KYC_USER.getCode());
		APIRequest req = APIRequest.instance(request);
		req.setTerminal(TerminalEnum.WEB);
		RequestContextHolder.setRequestAttributes(null);
		APIResponse<FaceInitResponse> response = kycApiTransferAdapter.facePcInit(req);
		System.out.println(response);
	}

	@Test
	public void pcFaceVerify() {
		FaceWebResultResponse response = new FaceWebResultResponse();
		response.setBaseLang(LanguageEnum.EN_US);
		response.setBaseUrl("https://www.devfdg.net\",\"bizId\":\"1572575765,4bbba42a-00aa-4c8e-90b5-a753bb3911ea");
		response.setBizNo("b6268281-b074-4cd7-887c-17e532ab7c35");
		response.setConfidence(90.734);
		response.setMaskConfidence(0.0);
		response.setMaskThreshold(0.5);
		response.setScreenReplayConfidence(0.001);
		response.setScreenReplayThreshold(0.5);
		response.setSuccess(true);
		response.setSyntheticFaceConfidence(0.0);
		response.setSyntheticFaceThreshold(0.5);
		response.setTransId("63e2da9b03a448fa8ce6500dab8b04f9");
		response.setTransType(FaceTransType.KYC_USER);
		response.setVerifyRequestId("1572575835,5de23bed-3e5e-496a-8b0a-c44f41aff90c");
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(faceBusiness, "faceIdApi", faceIdApi);
		Mockito.when(faceIdApi.webFaceResultHandler(Mockito.any())).thenReturn(APIResponse.getOKJsonResult(response));
		
		
		
		FacePcResultRequest request = new FacePcResultRequest();
		request.setSign("65eb356a240097050deae28c9ac4720b70794072");
		request.setData(
				"{'verify_result':'{'face_genuineness':'{'mask_confidpence':0.0,'screen_replay_threshold':0.5,'synthetic_face_threshold':0.5,'mask_threshold':0.5,'synthetic_face_confidence':0.0,'screen_replay_confidence':0.001}','result_ref1':'{'thresholds':'{'1e-6':78.038,'1e-5':74.399,'1e-4':69.315,'1e-3':62.169}','confidence':90.734}','request_id':'1572575835,5de23bed-3e5e-496a-8b0a-c44f41aff90c','time_used':815}','biz_no':'b6268281-b074-4cd7-887c-17e532ab7c35','biz_id':'1572575765,4bbba42a-00aa-4c8e-90b5-a753bb3911ea','biz_extra_data':'63e2da9b03a448fa8ce6500dab8b04f9','liveness_result':'{'result':'success','datetime':'20191101103714','log':'1572575813\\\\\\\\\\\\\\\\n0:A\\\\\\\\\\\\\\\\n1035:A\\\\\\\\\\\\\\\\n6525:A\\\\\\\\\\\\\\\\n16170:P\\\\\\\\\\\\\\\\n17533:M\\\\\\\\\\\\\\\\n19459:E\\\\\\\\\\\\\\\\n21387:O\\\\\\\\\\\\\\\\n','failure_reason':'null','version':'MegLive 2.4.0L'}'}");
		APIResponse<String> string = userFaceController.pcFaceVerify(APIRequest.instance(request));
		
		
		
		System.out.println(string);
	}
	
	@Test
	public void syncJumioAuditResult() {
		String json = "{'lastName':'刘峰','redirectUrl':'https://bnbtest.netverify.com/web/v4/app?authorizationToken=eyJhbGciOiJIUzUxMiIsInppcCI6IkdaSVAifQ.H4sIAAAAAAAAAB3MuwrCQBCF4XeZOguzk8le0ilESGMRgmIVdmdn6-AFFPHdjVan-c7_Bn3u7tDbzlNHzOTIUgNpLNBDm7VWlWgIlQ3njCaxrwbZagjetpQQGpBJ66bPw3553PS6qAj5SBolOk7Jp22KjaFQjuKs_V3-fc1IHh2aWBQNO3Um1uqMhC4IhSKtdRte59e66eMwn4ZpPFzg8wUZl1Q_tgAAAA.MgStP-NzHurFw1j9Xns_G7h_QRN6lF0yCMYUT3vvwM_V2q32ulwRU9W1aJIuPXaXTjvBAMYiB-kiyplFSZteDg&locale=en','documentType':'ID_CARD','issuingDate':'2015-02-03','back':'/JUMIO_IMG20191031/350462089_back_8184331.jpg','remark':'手持照与他人存在相似人脸','source':'WEB_UPLOAD','applyIp':'192.168.181.101','local':'en','expiryDate':'2035-02-03','number':'310115198805071914','scanSource':'WEB_UPLOAD','lockOne':true,'disableFlag':false,'bizId':'63e2da9b03a448fa8ce6500dab8b04f9','baseLanguage':'EN_US','failReason':'FACE_IMAGE_MATCH_OTHER','id':396631112645505024,'scanReference':'3beffec9-20e4-4bb0-a47f-041e887132a0','sdkTransaction':false,'idSubType':'NATIONAL_ID','handlerType':'USER_KYC','updateTime':1572522855000,'userId':350462089,'firstName':'N/A','baseUrl':'https://www.devfdg.net','face':'/JUMIO_IMG20191031/350462089_face_4406297.jpg','createTime':1572522626000,'dob':'1988-05-07','issuingCountry':'CHN','clientIp':'52.46.162.1','front':'/JUMIO_IMG20191031/350462089_front_7602080.jpg','status':'PASSED'}";
		
		APIResponse<?> response = userKycController.syncJumioAuditResult(APIRequest.instance(json));
		System.out.println(response);
	}
	
	@Test
	public void testIsIDNumberOccupied() {
		userCertificateBusiness.isIDNumberOccupied("310115198805071914", "CN", "ID_CARD", 350462089l);
	}
}
