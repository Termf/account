package com.binance.account.service.certificate.executor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.binance.account.Application;
import com.binance.account.common.enums.KycCertificateKycType;
import com.binance.account.service.kyc.executor.FaceOcrSubmitExecutor;
import com.binance.account.vo.kyc.request.FaceOcrSubmitRequest;
import com.binance.account.vo.kyc.response.KycFlowResponse;
import com.binance.inspector.common.enums.FaceOcrUploadType;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Base64Utils;

import javax.annotation.Resource;
import java.io.File;
import java.nio.file.Files;

@Log4j2
@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = Application.class)
public class FaceOcrSubmitExecutorTest extends TestEnv {

    @Resource
    private FaceOcrSubmitExecutor faceOcrSubmitExecutor;

    private String imageBase64(String path) throws Exception {
        byte[] data = Files.readAllBytes(new File(path).toPath());
        return Base64Utils.encodeToString(data);
    }

    @Test
    public void testOcrSubmit() throws Exception{
        String frontBase64 = imageBase64("/Users/jack/myself/docs/idcard1.jpg");
        String backBase64 = imageBase64("/Users/jack/myself/docs/idcard2.jpg");
        FaceOcrSubmitRequest request = new FaceOcrSubmitRequest();
        request.setKycType(KycCertificateKycType.USER);
        request.setUserId(350462089L);
        request.setFront(frontBase64);
        request.setBack(backBase64);
        request.setType(FaceOcrUploadType.OCR.name());
        KycFlowResponse response = faceOcrSubmitExecutor.execute(request);
        log.info("response \n {}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
    }

    @Test
    public void testFaceSubmit() throws Exception {
        String faceBase64 = imageBase64("/Users/jack/myself/docs/face.jpg");
        FaceOcrSubmitRequest request = new FaceOcrSubmitRequest();
        request.setKycType(KycCertificateKycType.USER);
        request.setUserId(350462089L);
        request.setFace(faceBase64);
        request.setType(FaceOcrUploadType.FACE.name());
        KycFlowResponse response = faceOcrSubmitExecutor.execute(request);
        log.info("response \n {}", JSON.toJSONString(response, SerializerFeature.PrettyFormat));
    }
}
