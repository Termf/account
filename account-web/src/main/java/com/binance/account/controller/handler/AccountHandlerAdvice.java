package com.binance.account.controller.handler;

import com.binance.master.error.GeneralCode;
import com.binance.master.models.APIResponse;
import lombok.extern.log4j.Log4j2;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.HandlerMethod;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by yangyang on 2019/7/15.
 * RestControllerAdvice注解在源码中，是按照类名排序放在map中，制作account独有的handlerAdvice只需要起个好名字即可
 */
@Log4j2
@RestControllerAdvice
public class AccountHandlerAdvice{

    private APIResponse<?> getValid(BindingResult bindingResult) {
        Map<String, Object> data = new HashMap<>();
        for (FieldError error : bindingResult.getFieldErrors()) {
            data.put(error.getField(), error.getDefaultMessage());
        }
        return APIResponse.getErrorJsonResult(APIResponse.Type.VALID, GeneralCode.SYS_VALID.getCode(), data);
    }


    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    public APIResponse<?> exception(HttpServletResponse response, MethodArgumentNotValidException exception,
                                    HandlerMethod handler) throws IOException {
        log.warn("system error:", exception);
        return this.getValid(exception.getBindingResult());
    }

}
