package com.binance.account.service.face;

import com.binance.account.service.face.handler.AbstractFaceHandler;
import com.binance.inspector.common.enums.FaceTransType;
import com.google.common.collect.Maps;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Resource;
import java.util.Map;

/**
 * 初始化所有 FaceHandlerContext bean，把所有包含有 FaceHandlerType 注解的Bean保存在Map中方便使用
 * @author liliang1
 * @date 2019-02-28 10:25
 */
@Log4j2
@Configuration
public class FaceHandlerConfig {

    @Resource
    private ApplicationContext applicationContext;

    @Bean
    public FaceHandlerContext getFaceHandlerContext() {
        Map<FaceTransType, AbstractFaceHandler> handlerMap = Maps.newHashMap();
        Map<String, Object> beanMap = applicationContext.getBeansWithAnnotation(FaceHandlerType.class);
        for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
            AbstractFaceHandler faceHandler = (AbstractFaceHandler) entry.getValue();
            FaceHandlerType faceHandlerType = faceHandler.getClass().getAnnotation(FaceHandlerType.class);
            if (faceHandlerType != null && faceHandlerType.values() != null) {
                FaceTransType[] transTypes = faceHandlerType.values();
                for (FaceTransType transType : transTypes) {
                    log.info("init face handler:{} bean:{}", transType, faceHandler);
                    handlerMap.put(transType, faceHandler);
                }
            }
        }
        return new FaceHandlerContext(handlerMap);
    }


}
