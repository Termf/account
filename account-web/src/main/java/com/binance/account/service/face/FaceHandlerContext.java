package com.binance.account.service.face;

import com.binance.account.service.face.handler.AbstractFaceHandler;
import com.binance.inspector.common.enums.FaceTransType;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;
import lombok.extern.log4j.Log4j2;

import java.util.Map;

/**
 * 人脸识别处理器上下文，通过注入该上下文获取对应类型的人脸识别处理器
 *
 * @author liliang1
 * @date 2019-02-28 9:54
 */
@Log4j2
public class FaceHandlerContext {

    private Map<FaceTransType, AbstractFaceHandler> handlerMap;

    public FaceHandlerContext(Map<FaceTransType, AbstractFaceHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }

    /**
     * 根据类型返回对应的处理器
     *
     * @param faceTransType
     * @return
     */
    public AbstractFaceHandler getFaceHandler(FaceTransType faceTransType) {
        AbstractFaceHandler handler = handlerMap.get(faceTransType);
        if (handler == null) {
            log.error("获取人脸识别处理器失败. faceTransType:{}", faceTransType);
            throw new BusinessException(GeneralCode.SYS_ERROR);
        }
        return handler;
    }
}
