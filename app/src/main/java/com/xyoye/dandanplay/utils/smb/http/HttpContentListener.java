package com.xyoye.dandanplay.utils.smb.http;

import java.io.InputStream;

/**
 * Created by xyoye on 2019/7/19.
 */

public interface HttpContentListener {
    InputStream getContentInputStream();

    String getContentType();

    long getContentLength();
}
