package com.binance.account.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashMap;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RiskTask {


    String type() default "";

    String userId() default "";

    String email() default "";
}
