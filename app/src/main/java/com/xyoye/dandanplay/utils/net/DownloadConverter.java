package com.xyoye.dandanplay.utils.net;

import com.xyoye.dandanplay.bean.FileDownloadBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import io.reactivex.functions.Function;
import okhttp3.ResponseBody;

/**
 * Created by xyy on 2018/12/20.
 */

public class DownloadConverter implements Function<ResponseBody, FileDownloadBean> {

    private CommOtherDataObserver commDownloadObserver;
    private String folder, fileName;

    public DownloadConverter(CommOtherDataObserver commDownloadObserver, String folder, String fileName) {
        this.commDownloadObserver = commDownloadObserver;
        this.folder = folder;
        this.fileName = fileName;
    }

    @Override
    public FileDownloadBean apply(ResponseBody responseBody) throws Exception {
        return saveFile(responseBody);
    }

    private FileDownloadBean saveFile(ResponseBody responseBody) throws IOException {
        InputStream is = null;
        byte[] buf = new byte[2048];
        int len = 0;
        FileOutputStream fos = null;
        try {
            is = responseBody.byteStream();
            final long total = responseBody.contentLength();
            long sum = 0;

            File dir = new File(folder);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
            fos = new FileOutputStream(file);
            while ((len = is.read(buf)) != -1) {
                sum += len;
                fos.write(buf, 0, len);
                final long finalSum = sum;
                //这里就是对进度的监听回调
                commDownloadObserver.onProgress((int) (finalSum * 100 / total),total);
            }
            fos.flush();

            return new FileDownloadBean(file);

        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (fos != null) fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}