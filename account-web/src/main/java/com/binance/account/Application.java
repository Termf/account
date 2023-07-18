package com.binance.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.scheduling.annotation.EnableAsync;

import com.binance.account.constants.AccountConstants;
import com.binance.master.annotations.EnableSKYWalkingTraceId;
import com.binance.master.utils.IPUtils;

@EnableAsync
@EnableDiscoveryClient
@SpringBootApplication
@EnableSKYWalkingTraceId
@EnableAspectJAutoProxy(exposeProxy = true)
@EnableFeignClients(basePackages = {"com.binance.messaging.api", "com.binance.inspector.api", "com.binance.risk.api",
        "com.binance.assetservice.api","com.binance.mbxgateway.api","com.binance.streamer.api","com.binance.margin.api",
        "com.binance.notification.api", "com.binance.rule.api", "com.binance.future.api", "com.binance.capital.api",
        "com.binance.inbox.api", "com.binance.fiatpayment.core.api", "com.binance.c2c.api","com.binance.featureservice.api",
        "com.binance.report.api","com.binance.infra.telegram.alarm","com.binance.capital.api","com.binance.fiat.payment.service.external.api","com.binance.margin.isolated.api",
        "com.binance.certification.api","com.binance.push.api","com.binance.fiat.payment.service.api","com.binance.delivery.periphery.api",
        "com.binance.authcenter.api"})
public class Application {

    public static void main(String[] args) {
        System.setProperty(AccountConstants.LOCAL_IP, IPUtils.getIp());
        SpringApplication.run(Application.class, args);

    }
}
