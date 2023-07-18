package com.binance.account.aop;

import com.binance.account.service.async.UserAsyncTask;
import com.binance.account.vo.user.response.RegisterUserResponse;
import com.binance.account.vo.user.response.RegisterUserResponseV2;
import com.binance.master.commons.ExpressionEvaluator;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.JsonUtils;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;


@Log4j2
@Component
@Aspect
@Order(3)
public class RiskTaskAspect {

    @Autowired
    private UserAsyncTask userSimpleBusiness;

    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    public static final String REGISTER = "register";
    public static final String REGISTERV2 = "registerV2";
    public static final String UPDATE_EMAIL = "updateEmail";
    public static final String CHANGE_PASSWORD = "changePassword";
    public static final String RISK_EXCHANGE = "fiat_risk_event_data_action_exchange";
    public static final String RISK_ROUNTING_KEY = "fiat_risk_event_data_route_key";
    public static final String RISK_ROUNTING_QUEUE = "fiat_risk_event_data_queue";

    @Around("@annotation(riskTask)")
    public Object afterReturning(ProceedingJoinPoint pjp, RiskTask riskTask) throws Throwable {
        Object obj = pjp.proceed();
        Long userId = null;
        String email = null;
        String deviceInfo = null;
        try {
            if (REGISTER.equals(riskTask.type())) {
                APIResponse<RegisterUserResponse> apiResponse = (APIResponse<RegisterUserResponse>)obj;
                userId = Long.parseLong(String.valueOf(apiResponse.getData().getUserId()));
                email = fetchEmailFromExpress(pjp, "#request.body.email");
                deviceInfo = fetchDeviceInfoFromExpress(pjp, "#request.body.deviceInfo");
            } else if (REGISTERV2.equals(riskTask.type())) {
                APIResponse<RegisterUserResponseV2> apiResponse = (APIResponse<RegisterUserResponseV2>)obj;
                userId = Long.parseLong(String.valueOf(apiResponse.getData().getUserId()));
                email = fetchEmailFromExpress(pjp, "#request.body.email");
                deviceInfo = fetchDeviceInfoFromExpress(pjp, "#request.body.deviceInfo");
            }else if (UPDATE_EMAIL.equals(riskTask.type())){
                userId = fetchUseridFromExpress(pjp,riskTask.userId());
                email = fetchEmailFromExpress(pjp,riskTask.email());
            }else{
                userId = fetchUseridFromExpress(pjp,riskTask.userId());
            }
            if (userId == null) {
                log.warn("RiskTaskAspect error{}:{}", pjp.toString(), "user id is null");
            } else {
                userSimpleBusiness.selectSendRiskMessage(RISK_EXCHANGE, RISK_ROUNTING_KEY,userId, email, deviceInfo,CHANGE_PASSWORD.equals(riskTask.type()),WebUtils.getRequestIp(),(REGISTERV2.equals(riskTask.type()) || REGISTER.equals(riskTask.type())));
            }
        }catch (Exception e){
            log.warn("send RiskTaskAspect error{}:{}", e, "user id is null");
        }
        return obj;
    }

    private String fetchDeviceInfoFromExpress(JoinPoint joinPoint, String condition) {
        Map deviceInfoMap = getValue(joinPoint, condition, Map.class);
        return JsonUtils.toJsonNotNullKey(deviceInfoMap);
    }

    private String fetchEmailFromExpress(JoinPoint joinPoint, String condition) {
        String email = getValue(joinPoint, condition, String.class);
        return email;
    }

    /**
     * 当用户没有显示指定表达式的时候，我会尝试通过默认的表达式来进行获取
     * */
    private Long getUserIdForCommon(JoinPoint joinPoint) {
        return fetchUseridFromExpress(joinPoint, "#request.body.userId");
    }

    private Long fetchUseridFromExpress(JoinPoint joinPoint, String condition) {
        Long userId = getValue(joinPoint, condition, Long.class);
        return userId;
    }

    private <T> T getValue(JoinPoint joinPoint, String condition, Class<T> clazz) {
        return getValue(joinPoint.getTarget(), joinPoint.getArgs(), joinPoint.getTarget().getClass(),
                ((MethodSignature) joinPoint.getSignature()).getMethod(), condition, clazz);
    }


    private <T> T getValue(Object object, Object[] args, Class<?> clazz, Method method, String condition,
                           Class<T> clazzRest) {
        if (null == args) {
            return null;
        }
        EvaluationContext evaluationContext = evaluator.createEvaluationContext(object, clazz, method, args);
        AnnotatedElementKey methodKey = new AnnotatedElementKey(method, clazz);
        return evaluator.condition(condition, methodKey, evaluationContext, clazzRest);
    }


}

