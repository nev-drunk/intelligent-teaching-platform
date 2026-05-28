package com.huadi.intelligentteachingplatform.exception;

import com.huadi.intelligentteachingplatform.common.ApiResponse;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ApiResponse<Void> handleValid(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("参数校验失败");
        return ApiResponse.fail(400, msg);
    }

    @ExceptionHandler(BindException.class)
    public ApiResponse<Void> handleBind(BindException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .orElse("参数绑定失败");
        return ApiResponse.fail(400, msg);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ApiResponse<Void> handleIllegal(IllegalArgumentException ex) {
        return ApiResponse.fail(400, ex.getMessage());
    }

    @ExceptionHandler(BusinessException.class)
    public ApiResponse<Void> handleBusiness(BusinessException ex) {
        return ApiResponse.fail(ex.getCode(), ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleGeneral(Exception ex) {
        return ApiResponse.fail(500, ex.getMessage() != null ? ex.getMessage() : "服务器内部错误");
    }
}
