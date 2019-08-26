//package com.xyoye.dandanplay.utils.smb;
//
//import com.blankj.utilcode.util.FileUtils;
//import com.blankj.utilcode.util.StringUtils;
//import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
//import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;
//import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPRequest;
//import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPRequestListener;
//import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPResponse;
//import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPServerList;
//import com.xyoye.dandanplay.utils.smb.cybergarage.http.HTTPStatus;
//
//import java.io.File;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.io.InputStream;
//
//
//public class TorrentServer extends Thread implements HTTPRequestListener {
//    public static String playFilePath = "";
//
//    private HTTPServerList httpServerList = new HTTPServerList();
//    // 默认的共享端口
//    private int HTTPPort = 2222;
//    // 绑定的ip
//    private String bindIP = null;
//    // 绑定的下载任务
//    private BtTask btTask;
//
//    public TorrentServer(BtTask btTask){
//        this.btTask = btTask;
//    }
//
//    public void updateTask(BtTask btTask){
//        this.btTask = btTask;
//    }
//
//    @Override
//    public void run() {
//        super.run();
//        int retryCount = 0;
//
//        int bindPort = getHTTPPort();
//        HTTPServerList serverList = getHttpServerList();
//        while (!serverList.open(bindPort)) {
//            retryCount++;
//            if (retryCount > 100) {
//                return;
//            }
//            setHTTPPort(bindPort + 1);
//            bindPort = getHTTPPort();
//        }
//        serverList.addRequestListener(this);
//        serverList.start();
//
//        LocalIPUtil.IP = serverList.getHTTPServer(0).getBindAddress();
//        LocalIPUtil.PORT = serverList.getHTTPServer(0).getBindPort();
//
//    }
//
//    @Override
//    public void httpRequestRecieved(HTTPRequest httpRequest) {
//        if (btTask == null)
//            return;
//
//        long[] ranges = httpRequest.getContentRange();
//        if (ranges[1] == 0)
//            ranges[1] = 10 * 1024;
//
//        String filePath;
//        if (!StringUtils.isEmpty(playFilePath)){
//            filePath = playFilePath;
//        }else {
//            return;
//        }
//
//        int filePosition = -1;
//
//        Torrent torrent = btTask.getTorrent();
//        for (int i=0; i<torrent.getTorrentFileList().size(); i++){
//            Torrent.TorrentFile torrentFile = torrent.getTorrentFileList().get(i);
//            if (filePath.equals(torrentFile.getPath())){
//                filePosition = i;
//            }
//        }
//
//        if (filePosition == -1)
//            return;
//
//        try {
//
//            File videoFile = new File(filePath);
//            if (ranges[0] > videoFile.length() || ranges[1] > videoFile.length()){
//                return;
//            }
//            if (ranges[1] > videoFile.length() - ranges[0]){
//                ranges[1] = videoFile.length() - ranges[0];
//            }
//
//            //设置查询的条件
//            btTask.setQueryPrice(filePosition, ranges[0], ranges[1]-ranges[0]);
//            //获取查询的结果
//            while (!btTask.getQueryPriceResult()){
//                Thread.sleep(200);
//            }
//
//            // 获取文件的大小
//            long contentLen = videoFile.length();
//            // 获取文件类型
//            String contentType = FileUtils.getFileExtension(filePath);
//            // 获取文文件流
//            InputStream inputStream = new FileInputStream(videoFile);
//
//            if (contentLen <= 0 || contentType.length() <= 0) {
//                httpRequest.returnBadRequest();
//                return;
//            }
//
//            HTTPResponse httpRes = new HTTPResponse();
//            httpRes.setContentType(contentType);
//            httpRes.setStatusCode(HTTPStatus.OK);
//            httpRes.setContentLength(contentLen);
//            httpRes.setContentInputStream(inputStream);
//
//            httpRequest.post(httpRes);
//
//            inputStream.close();
//        } catch (IOException e) {
//            httpRequest.returnBadRequest();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//
//
//    public String getBindIP() {
//        return bindIP;
//    }
//
//    public void setBindIP(String bindIP) {
//        this.bindIP = bindIP;
//    }
//
//    public HTTPServerList getHttpServerList() {
//        return httpServerList;
//    }
//
//    public void setHttpServerList(HTTPServerList httpServerList) {
//        this.httpServerList = httpServerList;
//    }
//
//    public int getHTTPPort() {
//        return HTTPPort;
//    }
//
//    public void setHTTPPort(int hTTPPort) {
//        HTTPPort = hTTPPort;
//    }
//
//}
