package com.binance.account.aop;

import com.binance.account.service.async.SecurityLogAsyncTask;
import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.security.UserSecurityLogMapper;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.master.commons.ExpressionEvaluator;
import com.binance.master.models.APIRequestHeader;
import com.binance.master.utils.StringUtils;
import com.binance.master.utils.WebUtils;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.reflect.Method;

@Log4j2
@Component
@Aspect
@Order(2)
public class SecurityLogAspect {

    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Autowired
    private SecurityLogAsyncTask securityLogAsyncTask;

    @Resource
    private UserSecurityLogMapper userSecurityLogMapper;

    @Resource
    private UserMapper userMapper;

    @Around("@annotation(securityLog)")
    public Object afterReturning(ProceedingJoinPoint pjp, SecurityLog securityLog) throws Throwable {
        Long userId = null;
        if (StringUtils.isNotBlank(securityLog.userId())) {
            userId = this.getValue(pjp, securityLog.userId(), Long.class);
        } else {
            String email = this.getValue(pjp, securityLog.email(), String.class);
            User user = this.userMapper.queryByEmail(email);
            if (null != user) {
                userId = user.getUserId();
            }
        }
        final APIRequestHeader header = WebUtils.getAPIRequestHeader();
        Object obj = pjp.proceed();
        if (null == userId) {
            log.warn("{}:{}", pjp.toString(), "user id is null");
        } else {
            securityLogAsyncTask.saveSecurityLog(userId, WebUtils.getRequestIp(), securityLog, header);
        }
        return obj;
    }

    private <T> T getValue(ProceedingJoinPoint pjp, String condition, Class<T> clazz) {
        return getValue(pjp.getTarget(), pjp.getArgs(), pjp.getTarget().getClass(),
                ((MethodSignature) pjp.getSignature()).getMethod(), condition, clazz);
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
