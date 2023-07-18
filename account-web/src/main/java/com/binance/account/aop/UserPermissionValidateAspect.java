package com.binance.account.aop;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.expression.AnnotatedElementKey;
import org.springframework.core.annotation.Order;
import org.springframework.expression.EvaluationContext;
import org.springframework.stereotype.Component;

import com.binance.account.data.entity.user.User;
import com.binance.account.data.mapper.user.UserMapper;
import com.binance.account.service.user.UserCommonValidateService;
import com.binance.account.service.user.impl.UserPermissionBussiness;
import com.binance.master.commons.ExpressionEvaluator;
import com.binance.master.error.BusinessException;
import com.binance.master.error.GeneralCode;

import lombok.extern.log4j.Log4j2;

/**
 * @author pengchenxue
 * 有些操作是不支持某种账号类型用户来访问的，比方说fiat用户不能创建apikey，margin账户不能登录，但是你让我在大量接口里面都写一段
 * 验证的代码太麻烦了，所以用aop拦截,用前置通知就可以了，不符合要求直接就返回完事了
 * */
@Log4j2
@Component
@Aspect
@Order(1)
public class UserPermissionValidateAspect {
    private ExpressionEvaluator evaluator = new ExpressionEvaluator();

    @Value("${user.permissionValidate.switch:true}")
    private Boolean permissionValidate;

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private UserCommonValidateService userCommonValidateService;

    @Autowired
    private UserPermissionBussiness userPermissionBussiness;

    @Pointcut("@annotation(com.binance.account.aop.UserPermissionValidate)")//指向自定义注解路径
    public void validatePointCut() {

    }

    /**
     * 前置通知：目标方法执行之前执行以下方法体的内容
     */
    @Before("validatePointCut()")
    public void before(JoinPoint joinPoint) throws Throwable {
        if (!permissionValidate) {
            return;    
        }
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        UserPermissionValidate userPermissionValidate = method.getAnnotation(UserPermissionValidate.class);
        if (null == userPermissionValidate || null == userPermissionValidate.userPermissionOperation()) {
            log.warn("UserPermissionValidateAspect.before：userPermissionValidate=null");
            return;
        }
        Long userId =null;
        if(StringUtils.isAllBlank(userPermissionValidate.email(),userPermissionValidate.userId())){
            userId=getUserIdForCommon(joinPoint);
        }else{
            userId= getUserId(joinPoint, userPermissionValidate);
        }
        if (null == userId) {
            log.warn("UserPermissionValidateAspect.before：userId=null");
            return;
        }
        Boolean checkResult = userPermissionBussiness.selectByUserIdAndOperation(userId,userPermissionValidate.userPermissionOperation());
        if (!checkResult) {
            log.info("user permission validate failed, user:{} operation:{} is rejected", userId, userPermissionValidate.userPermissionOperation().getOperation());
            throw new BusinessException(GeneralCode.SYS_NOT_SUPPORT);
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

    private Long getUserId(JoinPoint joinPoint, UserPermissionValidate userPermissionValidate) {
        Long userId = null;
        if (StringUtils.isNotBlank(userPermissionValidate.userId())) {
            userId = fetchUseridFromExpress(joinPoint, userPermissionValidate.userId());
        }
        if (StringUtils.isNotBlank(userPermissionValidate.email())) {
            String email = fetchEmailFromExpress(joinPoint, userPermissionValidate.email());
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
