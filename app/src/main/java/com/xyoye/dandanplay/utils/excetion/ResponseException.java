package com.xyoye.dandanplay.utils.excetion;

/**
 * Modified by xyy on 2017/9/15
 */

public class ResponseException extends Exception {

    public static final int ERROR_CODE_0 = 0; //服务器返回的错误类型
    public static final int ERROR_CODE_1 = 1; //网络异常返回的错误类型
    public static final int ERROR_CODE_2 = 2; //其他异常返回的错误类型

    private int errorCode = -1;

    public ResponseException() {

    }

    public ResponseException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ResponseException(String message, Throwable cause) {
        super(message, cause);
        this.initCause(cause);
    }

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }
}
