package com.wisewind.zhiyou.exception;

import com.wisewind.zhiyou.common.BaseResponse;
import com.wisewind.zhiyou.common.ErrorCode;
import com.wisewind.zhiyou.common.ResultUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse businessExceptionHandler(BusinessException e){
        return ResultUtils.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse businessExceptionHandler(RuntimeException e){
        return ResultUtils.error(ErrorCode.SYSTEM_ERROR);
    }
}
