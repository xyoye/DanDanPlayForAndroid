//package com.xyoye.dandanplay.utils.smbv2;
//
//import android.text.TextUtils;
//
//import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
//import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
//import com.xyoye.dandanplay.utils.smbv2.http.HttpContentListener;
//import com.xyoye.dandanplay.utils.smbv2.http.HttpResponse;
//import com.xyoye.dandanplay.utils.smbv2.http.HttpSocket;
//import com.xyoye.dandanplay.utils.smbv2.http.HttpStatus;
//
//import java.io.BufferedInputStream;
//import java.io.ByteArrayOutputStream;
//import java.io.IOException;
//import java.io.InputStream;
//import java.io.OutputStream;
//import java.net.Socket;
//
///**
// * Created by xyoye on 2019/7/19.
// */
//
//public class TorrentServerThread extends Thread {
//    //包含请求内容的Socket
//    private Socket socket;
//    //获取视频内容及信息的接口
//    private HttpContentListener httpContent;
//    //种子下载任务
//    private BtTask btTask;
//    //播放文件地址
//    private String videoFilePath;
//
//    TorrentServerThread(Socket socket, BtTask btTask, String videoFilePath, HttpContentListener httpContentListener) {
//        this.socket = socket;
//        this.btTask = btTask;
//        this.videoFilePath = videoFilePath;
//        this.httpContent = httpContentListener;
//    }
//
//    @Override
//    public void run() {
//        HttpSocket httpSocket = new HttpSocket(socket);
//        if (!httpSocket.open()) {
//            return;
//        }
//
//        printLog("----- read request thread start -----");
//
//        long[] requestRange = getRangeByRequestHeader(httpSocket);
//        if (requestRange == null) {
//           return;
//        }
//        handleHttpRequest(httpSocket, requestRange);
//        httpSocket.close();
//    }
//
//    //从请求头中读取range信息
//    private synchronized long[] getRangeByRequestHeader(HttpSocket socket) {
//        printLog("----- read request header -----");
//        InputStream inputStream = socket.getInputStream();
//        BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//
//        String firstLine = readRequestHeaderLine(bufferedInputStream);
//        if (TextUtils.isEmpty(firstLine)) {
//            printLog("----- request header is empty -----");
//            return null;
//        }
//
//        String headerLine = readRequestHeaderLine(bufferedInputStream);
//        while (!TextUtils.isEmpty(headerLine)) {
//            int colonIdx = headerLine.indexOf(':');
//            if (colonIdx > 0) {
//                String name = new String(headerLine.getBytes(), 0, colonIdx);
//                String value = new String(headerLine.getBytes(), colonIdx + 1, headerLine.length() - colonIdx - 1);
//                if (name.equals("Range")) {
//                    int cutIndex = value.indexOf("=");
//                    value = value.substring(cutIndex + 1);
//                    if (value.contains("-")) {
//                        if (value.startsWith("-")) {
//                            value = "0" + value;
//                        } else if (value.endsWith("-")) {
//                            value = value + "0";
//                        }
//                        String[] ranges = value.split("-");
//                        long[] range = new long[2];
//                        range[0] = Long.valueOf(ranges[0]);
//                        range[1] = Long.valueOf(ranges[1]);
//                        printLog("----- read range success ----- :" + range[0] + "/" + range[1]);
//                        return range;
//                    }
//                }
//            }
//            headerLine = readRequestHeaderLine(bufferedInputStream);
//        }
//        return new long[]{0, 0};
//    }
//
//    //按行读取头信息
//    private String readRequestHeaderLine(BufferedInputStream bufferedInputStream) {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        byte[] bytes = new byte[1];
//        try {
//            int readLen = bufferedInputStream.read(bytes);
//            while (readLen > 0) {
//                byte n = '\n';
//                byte r = '\r';
//                if (bytes[0] == n) {
//                    break;
//                }
//                if (bytes[0] != r) {
//                    byteArrayOutputStream.write(bytes[0]);
//                }
//                readLen = bufferedInputStream.read(bytes);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//            return "";
//        }
//
//        return byteArrayOutputStream.toString();
//    }
//
//    //处理请求构建response
//    private void handleHttpRequest(HttpSocket httpSocket, long[] requestRange) {
//        printLog("----- handle http request -----");
//        // 获取文件流
//        InputStream contentInputStream = httpContent.getContentInputStream();
//        // 获取文件的大小
//        long contentLength = httpContent.getContentLength();
//        // 获取文件类型
//        String contentType = httpContent.getContentType();
//
//        long requestRangeStart = requestRange[0];
//        long requestRangeEnd = requestRange[1] <= 0 ? contentLength - 1 : requestRange[1];
//
//        if (contentLength <= 0 || contentType.length() <= 0 || contentInputStream == null) {
//            printLog("----- handle failed : smb file error -----");
//            HttpResponse badResponse = new HttpResponse();
//            badResponse.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
//            badResponse.setContentLength(0);
//            dispatchResponse(badResponse, httpSocket, 0, 0, true);
//            return;
//        }
//
//        if ((requestRangeStart > contentLength) || (requestRangeEnd > contentLength)) {
//            printLog("-----handle failed : request range error -----");
//            HttpResponse badResponse = new HttpResponse();
//            badResponse.setStatusCode(HttpStatus.INVALID_RANGE);
//            badResponse.setContentLength(0);
//            dispatchResponse(badResponse, httpSocket, 0, 0, true);
//            return;
//        }
//
//        printLog("-----handle success : response build -----");
//        long responseLength = requestRangeEnd - requestRangeStart + 1;
//        HttpResponse response = new HttpResponse();
//        response.setStatusCode(HttpStatus.PARTIAL_CONTENT);
//        response.setContentType(contentType);
//        response.setContentLength(responseLength);
//        response.setContentInputStream(contentInputStream);
//        response.setContentRange(requestRangeStart, requestRangeEnd, contentLength);
//        if (requestRangeStart == contentLength)
//            requestRangeStart--;
//        dispatchResponse(response, httpSocket, requestRangeStart, responseLength, false);
//    }
//
//    private int getTorrentFilePosition(){
//        int filePosition = -1;
//
//        Torrent torrent = btTask.getTorrent();
//        for (int i=0; i<torrent.getTorrentFileList().size(); i++){
//            Torrent.TorrentFile torrentFile = torrent.getTorrentFileList().get(i);
//            if (videoFilePath.equals(torrentFile.getPath())){
//                filePosition = i;
//            }
//        }
//
//        return filePosition;
//    }
//
//    //返回response
//    private synchronized void dispatchResponse(HttpResponse response,
//                                               HttpSocket httpSocket,
//                                               long contentOffset,
//                                               long contentLength,
//                                               boolean badRequest) {
//        printLog("----- dispatch http response -----");
//
//        try {
//            OutputStream outputStream = httpSocket.getOutputStream();
//            outputStream.write(response.getResponseHeader().getBytes());
//            //必须返回！！！
//            outputStream.write("\r\n".getBytes());
//
//            if (badRequest) {
//                outputStream.flush();
//                printLog("----- return bad response -----");
//                return;
//            }
//
//            InputStream inputStream = response.getContentInputStream();
//            if (contentOffset > 0) {
//                if (inputStream.skip(contentOffset) > 0) {
//                    printLog("----- input stream skip -----:" + contentOffset);
//                }
//            }
//
//            int taskFilePosition = getTorrentFilePosition();
//            int bufferSize = 256  * 1024;
//            byte[] readBuffer = new byte[bufferSize];
//            long readTotalSize = 0;
//            long readSize = (bufferSize > (contentLength - readTotalSize))
//                    ? (contentLength - readTotalSize)
//                    : bufferSize;
//
//
//            btTask.setQueryPrice(taskFilePosition, contentOffset, readSize);
//            while (!btTask.getQueryPriceResult()){
//                Thread.sleep(200);
//            }
//            int readLen = inputStream.read(readBuffer, 0, (int) readSize);
//
//            printLog("----- send video data start -----:" + contentOffset);
//            while (readLen > 0 && readTotalSize < contentLength) {
//                outputStream.write(readBuffer, 0, readLen);
//                outputStream.flush();
//                readTotalSize += readLen;
//                readSize = (bufferSize > (contentLength - readTotalSize))
//                        ? (contentLength - readTotalSize)
//                        : bufferSize;
//
//                btTask.setQueryPrice(taskFilePosition, contentOffset+readTotalSize, readSize);
//                while (!btTask.getQueryPriceResult()){
//                    Thread.sleep(200);
//                }
//                readLen = inputStream.read(readBuffer, 0, (int) readSize);
//
//                printLog("----- send video data success -----:" + readTotalSize + "/" + contentLength);
//            }
//
//
//            printLog("----- send video data over -----:" + contentOffset);
//            outputStream.flush();
//        } catch (Exception e) {
//            printLog("----- send video data error -----:" + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    //打印信息
//    private void printLog(String message) {
//        System.out.println(message);
//    }
//
//}
