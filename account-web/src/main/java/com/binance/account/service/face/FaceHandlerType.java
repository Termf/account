package com.binance.account.service.face;

import com.binance.inspector.common.enums.FaceTransType;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 定义了人脸识别的处理类型信息
 *
 * @author liliang1
 * @date 2019-02-28 9:50
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface FaceHandlerType {

    /**
     * 处理类型描述, 一个处理器可以处理多种类型的人脸识别，也可以分开
     *
     * @return
     */
    FaceTransType[] values();

}
