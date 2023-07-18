package com.binance.account.service.kyc;

import javax.annotation.Resource;

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import com.binance.account.job.KycCnUserDoOcrJobHandler;
import com.binance.account.job.KycCnUserDoOcrSubJobHandler;
import com.binance.inspector.api.FaceIdApi;
import com.binance.inspector.vo.faceid.FaceIdCardOcrVo;
import com.binance.inspector.vo.faceid.response.ImageCanDoFaceResponse;
import com.binance.master.models.APIResponse;

public class KycCnUserDoOcrTest extends BaseTest{
	@Resource
	KycCnUserDoOcrJobHandler kycCnUserDoOcrJobHandler;
	
	@Resource
	KycCnUserDoOcrSubJobHandler kycCnUserDoOcrSubJobHandler;
	@Mock
	FaceIdApi faceIdApi;
	
	@Test
	public void test1() throws Exception {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(kycCnUserDoOcrJobHandler, "faceIdApi", faceIdApi);
		FaceIdCardOcrVo response = new FaceIdCardOcrVo();
		response.setName("junit");
		Mockito.when(faceIdApi.verifyIdCardOcrDirect(Mockito.any())).thenReturn(APIResponse.getOKJsonResult(response));
		kycCnUserDoOcrJobHandler.execute("user:35000051,10000008");
	}
	
	@Test
	public void test2() throws Exception {
		MockitoAnnotations.initMocks(this);
		ReflectionTestUtils.setField(kycCnUserDoOcrSubJobHandler, "faceIdApi", faceIdApi);
		FaceIdCardOcrVo response = new FaceIdCardOcrVo();
		response.setName("junit");
		Mockito.when(faceIdApi.getFaceIdCardOcr(Mockito.any())).thenReturn(APIResponse.getOKJsonResult(response));
		kycCnUserDoOcrSubJobHandler.execute(null);
	}

}
