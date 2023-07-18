package com.binance.account.aop;

import com.binance.account.data.entity.user.User;
import com.binance.account.service.async.UserAsyncTask;
import com.binance.account.vo.user.response.RegisterUserResponse;
import com.binance.account.vo.user.response.RegisterUserResponseV2;
import com.binance.master.commons.ExpressionEvaluator;
import com.binance.master.models.APIResponse;
import com.binance.master.utils.StringUtils;
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


@Log4j2
@Component
@Aspect
@Order(3)
public class FrontTaskAspect {

    @Autowired
    private UserAsyncTask userSimpleBusiness;

    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    public static final String REGISTER = "register";

    public static final String REGISTERV2 = "registerV2";

    @Around("@annotation(frontTask)")
    public Object afterReturning(ProceedingJoinPoint pjp, FrontTask frontTask) throws Throwable {
        Long userId = null;
        Object obj = pjp.proceed();
        try {
            if (REGISTER.equals(frontTask.type())) {
                APIResponse<RegisterUserResponse> apiResponse = (APIResponse<RegisterUserResponse>)obj;
                userId = Long.parseLong(String.valueOf(apiResponse.getData().getUserId()));
            } else if (REGISTERV2.equals(frontTask.type())) {
                APIResponse<RegisterUserResponseV2> apiResponse = (APIResponse<RegisterUserResponseV2>)obj;
                userId = Long.parseLong(String.valueOf(apiResponse.getData().getUserId()));
            }else{
                userId = getUserIdForCommon(pjp);
            }
            if (userId == null) {
                log.warn("FrontTaskAspect error{}:{}", pjp.toString(), "user id is null");
            } else {
                userSimpleBusiness.sendMgsToFrontGroup(frontTask.routingKey(), String.valueOf(userId), frontTask.eventType(), frontTask.accountType(), frontTask.tfaType());
            }
        }catch (Exception e){
            log.warn("send FrontTaskAspect error{}:{}", pjp.toString(), "user id is null");
        }
        return obj;
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
