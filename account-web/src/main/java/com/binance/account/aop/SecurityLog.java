package com.binance.account.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SecurityLog {

    String name() default "";

    String operateType() default "";

    String userId() default "";

    String email() default "";
}
