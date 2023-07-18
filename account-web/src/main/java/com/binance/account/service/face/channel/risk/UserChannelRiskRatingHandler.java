package com.binance.account.service.face.channel.risk;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.binance.account.common.enums.UserRiskRatingChannelCode;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface UserChannelRiskRatingHandler {

	UserRiskRatingChannelCode[] handlerType();
}
