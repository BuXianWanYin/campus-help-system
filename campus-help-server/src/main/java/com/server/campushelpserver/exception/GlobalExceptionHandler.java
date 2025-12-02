package com.server.campushelpserver.exception;

import com.server.campushelpserver.common.Result;
import com.server.campushelpserver.common.ResultCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * 处理业务异常
     */
    @ExceptionHandler(BusinessException.class)
    public Result<?> handleBusinessException(BusinessException e) {
        logger.error("业务异常：{}", e.getMessage(), e);
        return Result.error(e.getCode() != null ? e.getCode() : ResultCode.FAIL.getCode(), e.getMessage());
    }
    
    /**
     * 处理参数校验异常
     */
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public Result<?> handleValidationException(Exception e) {
        logger.error("参数校验异常：{}", e.getMessage(), e);
        String message = "参数校验失败";
        if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException ex = (MethodArgumentNotValidException) e;
            List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
            if (!fieldErrors.isEmpty()) {
                FieldError fieldError = fieldErrors.get(0);
                message = fieldError.getDefaultMessage();
            }
        } else if (e instanceof BindException) {
            BindException ex = (BindException) e;
            List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();
            if (!fieldErrors.isEmpty()) {
                FieldError fieldError = fieldErrors.get(0);
                message = fieldError.getDefaultMessage();
            }
        }
        return Result.error(ResultCode.BAD_REQUEST.getCode(), message);
    }
    
    /**
     * 处理其他异常
     */
    @ExceptionHandler(Exception.class)
    public Result<?> handleException(Exception e) {
        logger.error("系统异常：{}", e.getMessage(), e);
        return Result.error(ResultCode.INTERNAL_SERVER_ERROR.getCode(), "系统异常，请稍后重试");
    }
}

