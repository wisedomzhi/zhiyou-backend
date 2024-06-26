package com.wisewind.zhiyou.common;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 通用返回对象
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {
    @Serial
    private static final long serialVersionUID = 1582959771295712511L;

    private int code;

    private T data;

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode){
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}
