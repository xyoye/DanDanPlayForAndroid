package com.xyoye.dandanplay.utils.smb.http;

import java.io.InputStream;

/**
 * Created by xyoye on 2019/7/15.
 */
public class HttpResponse{
    //内容
    private InputStream contentInputStream = null;

    //http状态码
    private int statusCode = 0;
    //服务名
    private String server;
    //内容类型
    private String contentType;
    //内容长度
    private String contentLength;
    //内容范围
    private String contentRange;

    public HttpResponse() {
        String OSName = System.getProperty("os.name");
        String OSVersion = System.getProperty("os.version");

        server = OSName + "/" + OSVersion + " HTTP/1.1";
        contentType = "text/html; charset=\"utf-8\"";
    }

    public void setStatusCode(int code) {
        statusCode = code;
    }

    public void setContentType(String contentType){
        this.contentType =  contentType;
    }

    public void setContentInputStream(InputStream inputStream){
        contentInputStream = inputStream;
    }

    public void setContentLength(long contentLength){
        this.contentLength = String.valueOf(contentLength);
    }

    public void setContentRange(long startRange, long endRange, long length){
        String rangeStr = "bytes ";
        rangeStr += startRange + "-";
        rangeStr += endRange + "/";
        rangeStr += ((0 < length) ? Long.toString(length) : "*");
        contentRange = rangeStr;
    }

    public String getResponseHeader() {
        String statusLine = "HTTP/1.1 " + statusCode + " " + HttpStatus.code2String(statusCode);
        return statusLine + "\r\n" +
                    "Server: " + server + "\r\n" +
                    "Content-Type: " + contentType + "\r\n" +
                    "Content-Length: " + contentLength + "\r\n" +
                    "Content-Range: " + contentRange + "\r\n";
    }

    public InputStream getContentInputStream(){
        return contentInputStream;
    }
}
