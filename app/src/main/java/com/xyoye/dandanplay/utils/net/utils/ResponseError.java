package com.xyoye.dandanplay.utils.net.utils;

/**
 * Created by xyoye on 2019/11/7.
 *
 * 自定义的网络异常类
 */

public class ResponseError extends Exception {
    public int code;
    public String message;

    ResponseError(Throwable throwable, int code) {
        super(throwable);
        this.code = code;
    }
}
