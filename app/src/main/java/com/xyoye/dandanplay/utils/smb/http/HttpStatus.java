package com.xyoye.dandanplay.utils.smb.http;


public class HttpStatus {
    //请求者应当继续提出请求。服务器返回此代码则意味着，服务器已收到了请求的第一部分，现正在等待接收其余部分。
    public static final int CONTINUE = 100;
    //服务器已成功处理了请求
    public static final int OK = 200;
    //服务器成功处理了部分 GET 请求
    public static final int PARTIAL_CONTENT = 206;
    //服务器不理解请求的语法
    public static final int BAD_REQUEST = 400;
    //服务器找不到请求的网页
    public static final int NOT_FOUND = 404;
    //服务器未满足请求者在请求中设置的其中一个前提条件。
    public static final int PRECONDITION_FAILED = 412;
    //所请求的范围无法满足
    public static final int INVALID_RANGE = 416;
    //服务器遇到错误，无法完成请求
    public static final int INTERNAL_SERVER_ERROR = 500;

    // 将状态码转换成提示的字符串 ,如果没有此状态码返回一个空字符串
    public static String code2String(int code) {
        switch (code) {
            case CONTINUE:
                return "Continue";
            case OK:
                return "OK";
            case PARTIAL_CONTENT:
                return "Partial Content";
            case BAD_REQUEST:
                return "Bad Request";
            case NOT_FOUND:
                return "Not Found";
            case PRECONDITION_FAILED:
                return "Precondition Failed";
            case INVALID_RANGE:
                return "Invalid Range";
            case INTERNAL_SERVER_ERROR:
                return "Internal Server Error";
        }
        return "";
    }

}
