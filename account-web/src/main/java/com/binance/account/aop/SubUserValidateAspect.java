package com.binance.account.aop;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.error.AccountErrorCode;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.master.commons.ExpressionEvaluator;
import com.binance.master.error.BusinessException;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * @author pengchenxue
 * 有些操作是不支持子账户用户来访问的，但是你让我在大量接口里面都写一段
 * 验证的代码太麻烦了，所以用aop拦截,用前置通知就可以了，不符合要求直接就返回完事了
 * */
@Log4j2
@Component
@Aspect
@Order(1)
public class SubUserValidateAspect {
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserCommonValidateService userCommonValidateService;

    @Pointcut("@annotation(com.binance.account.aop.SubUserValidate)")//指向自定义注解路径
    public void validatePointCut() {

    }

    /**
     * 前置通知：目标方法执行之前执行以下方法体的内容
     */
    @Before("validatePointCut()")
    public void before(JoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        SubUserValidate subUserValidate = method.getAnnotation(SubUserValidate.class);
        if (null == subUserValidate) {
            log.warn("SubUserValidateAspect.before：subUserValidate=null");
            return;
        }
        Long userId =null;
        if(StringUtils.isAllBlank(subUserValidate.email(),subUserValidate.userId())){
            userId=getUserIdForCommon(joinPoint);
        }else{
            userId= getUserId(joinPoint, subUserValidate);
        }
        if (null == userId) {
            log.warn("SubUserValidateAspect.before：userId=null");
            return;
        }
        Boolean isSubUser = userCommonValidateService.isSubUser(userId);
        if (isSubUser) {
            throw new BusinessException(AccountErrorCode.SYS_NOT_SUPPORT_FOR_MARGIN_USER);
        }
    }
    /**
     * 当用户没有显示指定表达式的时候，我会尝试通过默认的表达式来进行获取
     * */
    private Long getUserIdForCommon(JoinPoint joinPoint) {
        Long userId = null;
        userId = fetchUseridFromExpress(joinPoint, "#request.body.userId");
        if(null!=userId){
            return userId;
        }
        String email = fetchEmailFromExpress(joinPoint, "#request.body.email");
        User user = this.userMapper.queryByEmail(email);
        if (null != user) {
            userId = user.getUserId();
            return userId;
        }
        return userId;
    }

    private Long getUserId(JoinPoint joinPoint, SubUserValidate subUserValidate) {
        Long userId = null;
        if (StringUtils.isNotBlank(subUserValidate.userId())) {
            userId = fetchUseridFromExpress(joinPoint, subUserValidate.userId());
        }
        if (StringUtils.isNotBlank(subUserValidate.email())) {
            String email = fetchEmailFromExpress(joinPoint, subUserValidate.email());
            User user = this.userMapper.queryByEmail(email);
            if (null != user) {
                userId = user.getUserId();
            }
        }
        return userId;
    }

    private Long fetchUseridFromExpress(JoinPoint joinPoint, String condition) {
        Long userId = getValue(joinPoint, condition, Long.class);
        return userId;
    }

    private String fetchEmailFromExpress(JoinPoint joinPoint, String condition) {
        String email = getValue(joinPoint, condition, String.class);
        return email;
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
