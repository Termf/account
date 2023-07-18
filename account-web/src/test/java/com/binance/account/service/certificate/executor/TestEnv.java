package com.binance.account.service.certificate.executor;

public class TestEnv {

    static {
        System.setProperty("eureka.instance.metadataMap.envflag", "jack.li");
        System.setProperty("env", "dev");
        System.setProperty("com.binance.secretsManager.accessKey", "AKIASQS2UI6PJPEGV5MG");
        System.setProperty("com.binance.secretsManager.secretKey", "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");
    }

}
