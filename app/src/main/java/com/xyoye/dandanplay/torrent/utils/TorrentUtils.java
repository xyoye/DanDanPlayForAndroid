package com.xyoye.dandanplay.torrent.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.StatFs;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.torrent.TorrentEngine;
import com.xyoye.dandanplay.torrent.info.Torrent;

import org.apache.commons.io.FileUtils;
import org.libtorrent4j.ErrorCode;
import org.libtorrent4j.Priority;
import org.libtorrent4j.TorrentInfo;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by xyoye on 2019/8/20.
 */

public class TorrentUtils {
    //PeerId
    public static final String PEER_FINGERPRINT = "DD";
    //Magnet Header
    public static final String MAGNET_HEADER = "magnet:?xt=urn:btih:";


    /**
     * 获取文件选择信息
     */
    public static Priority[] getPriorities(Torrent torrent) {
        return torrent.getPriorities().toArray(new Priority[0]);
    }

    /**
     * 转换错误信息
     */
    public static String getErrorMsg(ErrorCode error) {
        return (error == null ? "" : error.message() + ", code " + error.value());
    }

    /**
     * 创建种子文件
     */
    public static File createTorrentFile(String name, byte[] data, String saveDirPath) throws Exception {
        File saveDir = new File(saveDirPath);
        if (!saveDir.exists() || !saveDir.isDirectory()) {
            if (saveDir.mkdirs())
                throw new FileNotFoundException("Dir Created Failed");
        }

        if (name == null || data == null)
            return null;

        File torrent = new File(saveDir, name);
        FileUtils.writeByteArrayToFile(torrent, data);

        return torrent;
    }

    /**
     * 获取路径下的剩余存储空间
     */
    @SuppressLint("UsableSpace")
    public static long getFreeSpace(String path) {
        long availableBytes = -1L;

        try {
            File file = new File(path);
            availableBytes = file.getUsableSpace();
        } catch (Exception e) {


            try {
                StatFs stat = new StatFs(path);
                availableBytes = stat.getAvailableBytes();
            } catch (Exception ignore) {
                //某些设备无法获取的剩余空间
            }
        }

        return availableBytes;
    }

    /**
     * 保存恢复数据
     */
    public static void saveResumeData(byte[] data) throws Exception {
        FileUtils.writeByteArrayToFile(new File(TorrentFileUtils.TaskResumeFilePath), data);
    }

    /**
     * 获取sha1链接
     */
    public static String makeSha1Hash(String s) {
        if (s == null)
            return null;

        MessageDigest messageDigest;
        try {
            messageDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        messageDigest.update(s.getBytes(Charset.forName("UTF-8")));
        StringBuilder sha1 = new StringBuilder();
        for (byte b : messageDigest.digest()) {
            if ((0xff & b) < 0x10)
                sha1.append("0");
            sha1.append(Integer.toHexString(0xff & b));
        }

        return sha1.toString();
    }

    /**
     * 将版本号转为数组
     */
    public static int[] getVersions() {
        int[] versions = new int[3];

        try {
            PackageManager packageManager = IApplication.get_context().getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(IApplication.get_context().getPackageName(), 0);
            String versionName = packageInfo.versionName;

            int subEnd = versionName.indexOf("-");
            if (subEnd > 0) {
                versionName = versionName.substring(0, subEnd);
            }

            String[] versionsTemp = versionName.split("\\.");
            if (versionsTemp.length < 2) {
                return versions;
            }

            versions[0] = Integer.parseInt(versionsTemp[0]);
            versions[1] = Integer.parseInt(versionsTemp[1]);
            if (versionsTemp.length >= 3)
                versions[2] = Integer.parseInt(versionsTemp[2]);

        } catch (PackageManager.NameNotFoundException ignore) {

        } catch (NumberFormatException ignore) {

        }

        return versions;
    }

    /**
     * 获取用户代理名
     */
    public static String getUserAgent() {
        String userAgent = "DanDanPlay-Android";
        int[] versions = getVersions();
        return String.format(userAgent + " %s%s%s", versions[0], versions[1], versions[2]);
    }

    /**
     * 是否已连接网络
     */
    public static boolean isConnectedNetwork() {
        ConnectivityManager manager = (ConnectivityManager) IApplication.get_context().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (manager == null) return false;
        NetworkInfo activeNetwork = manager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }

    /**
     * 是否已连接wifi
     */
    public static boolean isConnectedWifi() {
        WifiManager manager = (WifiManager) IApplication.get_context().getApplicationContext().getSystemService(WIFI_SERVICE);
        if (manager == null) return false;
        return manager.isWifiEnabled();
    }

    /**
     * 复制下载文件到自定义的文件夹
     */
    public static boolean copyTorrentFile(String hash, String newDirPath) throws IOException {
        if (hash == null || TextUtils.isEmpty(newDirPath)) {
            return false;
        }

        String torrentFileName = hash + ".torrent";

        File torrent = new File(TorrentFileUtils.getSystemCacheDirPath(), torrentFileName);
        if (!torrent.exists())
            return false;

        FileUtils.copyFile(torrent, new File(newDirPath, torrentFileName));
        return true;
    }

    /**
     * 通过种子文件获取种子信息
     */
    @Nullable
    public static TorrentInfo getTorrentInfoForFile(String torrentFilePath){
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
}
