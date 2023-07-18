package com.binance.account.aop;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.binance.account.vo.user.enums.UserPermissionOperationEnum;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface UserPermissionValidate {

    String name() default "";

    String userId() default "";

    String email() default "";

    UserPermissionOperationEnum userPermissionOperation() ;

}
