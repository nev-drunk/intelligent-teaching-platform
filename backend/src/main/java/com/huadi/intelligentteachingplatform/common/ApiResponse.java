package com.huadi.intelligentteachingplatform.common;

import lombok.Data;

@Data
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(200);
        r.setMsg("success");
        r.setData(data);
        return r;
    }

    public static <T> ApiResponse<T> ok(String msg, T data) {
        ApiResponse<T> r = ok(data);
        r.setMsg(msg);
        return r;
    }

    public static <T> ApiResponse<T> fail(int code, String msg) {
        ApiResponse<T> r = new ApiResponse<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
}
