package com.wisewind.zhiyou.common;

/**
 * 包装返回对象工具类
 */
public class ResultUtils {
    public static <T> BaseResponse<T> success(T data){
        return new BaseResponse<>(0, data);
    }

    public static BaseResponse error(ErrorCode errorCode){
        return new BaseResponse<>(errorCode);
    }

    public static BaseResponse error(ErrorCode errorCode, String message, String description){
        return new BaseResponse<>(errorCode.getCode(), null, message, description);
    }

    public static BaseResponse error(ErrorCode errorCode, String description){
        return new BaseResponse<>(errorCode.getCode(), null, errorCode.getMessage(), description);
    }

    public static BaseResponse error(int code,String message, String description){
        return new BaseResponse(code, null, message, description);
    }

}
