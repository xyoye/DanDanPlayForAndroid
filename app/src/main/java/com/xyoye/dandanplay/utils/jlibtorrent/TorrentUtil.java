package com.xyoye.dandanplay.utils.jlibtorrent;

import android.support.annotation.Nullable;

import com.frostwire.jlibtorrent.TorrentInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentUtil {

    public static @Nullable TorrentInfo getTorrentInfoForFile(String torrentFilePath){
        InputStream inputStream = null;
        ByteArrayOutputStream outputStream = null;
        try {
            File torrentFile = new File(torrentFilePath);
            if (torrentFile.exists()){
                inputStream = new FileInputStream(torrentFile);
                outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                while (true) {
                    int byteCount = inputStream.read(buffer);
                    if (byteCount <= 0) {
                        break;
                    }
                    outputStream.write(buffer, 0, byteCount);
                }
                return TorrentInfo.bdecode(outputStream.toByteArray());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            try {
                if (inputStream != null)
                    inputStream.close();
                if (outputStream != null)
                    outputStream.close();
            }catch (IOException e) {
                e.printStackTrace();
            }

        }
        return null;
    }

    public static String getDhtBootstrapNodeString() {
        return "router.bittorrent.com:6681" +
                ",dht.transmissionbt.com:6881" +
                ",dht.libtorrent.org:25401" +
                ",dht.aelitis.com:6881" +
                ",router.bitcomet.com:6881" +
                ",router.bitcomet.com:6881" +
                ",dht.transmissionbt.com:6881" +
                ",router.silotis.us:6881"; // IPv6
    }
}
