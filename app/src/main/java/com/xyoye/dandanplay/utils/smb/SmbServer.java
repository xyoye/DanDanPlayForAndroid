package com.xyoye.dandanplay.utils.smb;

import android.text.TextUtils;

import com.xyoye.smb.SmbManager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

/**
 * Created by xyoye on 2020/7/1.
 */

public class SmbServer extends NanoHTTPD {

    public static String SMB_FILE_NAME;

    private static class Holder{
        static SmbServer instance = new SmbServer();
    }

    public static SmbServer getInstance(){
        return Holder.instance;
    }

    private SmbServer() {
        //固定使用9664端口
        super(9664);
    }

    @Override
    public Response serve(IHTTPSession session) {
        Map<String, String> headers = session.getHeaders();

        //从请求头获取range
        String range = null;
        if (headers.containsKey("Range")) {
            range = headers.get("Range");
        }
        if (headers.containsKey("range")){
            range = headers.get("range");
        }

        InputStream inputStream = getContentInputStream();
        String contentType = getContentType();
        long maxLength = getContentLength();

        long contentLength = maxLength;
        long startIndex = 0;
        long endIndex = maxLength;

        //包含range信息，调整返回数据
        if (!TextUtils.isEmpty(range)) {
            long[] rangeLong = parseRange(range);
            //内容初始位置
            if (rangeLong[0] < maxLength) {
                startIndex = rangeLong[0];
            }

            //内容结束位置
            if (rangeLong[1] > 0 && rangeLong[1] <= maxLength) {
                endIndex = rangeLong[1];
            }

            if (startIndex < endIndex) {
                contentLength = endIndex - startIndex + 1;
            }

            try {

                inputStream.skip(startIndex);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        //创建响应体
        Response response =NanoHTTPD.newFixedLengthResponse(Response.Status.PARTIAL_CONTENT, contentType, inputStream, maxLength);
        String content_length =  String.valueOf(contentLength);
        String content_range = String.format("bytes %s-%s/%s", startIndex, endIndex, String.valueOf(maxLength));
        //添加响应头
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Content-Length", content_length);
        response.addHeader("Content-Range",content_range);
        return response;
    }


    /**
     * 获取视频内容
     */
    private InputStream getContentInputStream() {
        return SmbManager.getInstance().getController().getFileInputStream(SMB_FILE_NAME);
    }

    /**
     * 获取视频格式
     */
    private String getContentType() {
        if (TextUtils.isEmpty(SMB_FILE_NAME))
            return "";
        int lastPoi = SMB_FILE_NAME.lastIndexOf('.');
        int lastSep = SMB_FILE_NAME.lastIndexOf(File.separator);
        if (lastPoi == -1 || lastSep >= lastPoi) return "";
        return "." + SMB_FILE_NAME.substring(lastPoi + 1);
    }

    /**
     * 获取视频长度
     */
    private long getContentLength() {
        return SmbManager.getInstance().getController().getFileLength(SMB_FILE_NAME);
    }

    /**
     * 解析range
     */
    private long[] parseRange(String range) {
        range = range.replace("bytes=", "");
        if (range.contains("-")) {
            if (range.startsWith("-")) {
                range = "0" + range;
            } else if (range.endsWith("-")) {
                range = range + "0";
            }
            String[] ranges = range.split("-");
            if (ranges.length == 2) {
                try {
                    return new long[]{Long.parseLong(ranges[0]),  Long.parseLong(ranges[1])};
                } catch (NumberFormatException ignore){

                }
            }
        }
        return new long[]{0, 0};
    }
}
