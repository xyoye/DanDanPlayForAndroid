package com.xyoye.dandanplay.utils;

import android.media.MediaPlayer;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by xyoye on 2018/7/14.
 */

public class MD5Util {
    private static MessageDigest messagedigest = null;
    private static char hexDigits[]   = { '0', '1', '2', '3', '4', '5', '6',  '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    static {
        try {
            messagedigest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            System.err.println("MD5Util messagedigest初始化失败");
            e.printStackTrace();
        }
    }

    public static String getVideoFileHash(String filePath) {
        try {
            File file = FileUtils.getFileByPath(filePath);
            if (file != null) {
                if (file.length() < 16 * 1024 * 1024) {
                    return getFileMD5String(file);
                } else {
                    RandomAccessFile r = new RandomAccessFile(file, "r");
                    r.seek(0);
                    byte[] bs = new byte[16 * 1024 * 1024];
                    r.read(bs);
                    return getMD5String(bs);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 对文件进行MD5加密
     */
    private static String getFileMD5String(File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        FileChannel ch = in.getChannel();
        MappedByteBuffer byteBuffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
        messagedigest.update(byteBuffer);
        return bufferToHex(messagedigest.digest());
    }

    /**
     * 对byte类型的数组进行MD5加密
     */
    private static String getMD5String(byte[] bytes) {
        messagedigest.update(bytes);
        return bufferToHex(messagedigest.digest());
    }

    public static long getVideoDuration(String path) {
        long result = 0;
        File f = new File(path);
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(new FileInputStream(f).getFD());
            mediaPlayer.prepare();
            result = mediaPlayer.getDuration();
        } catch (IOException e) {
            LogUtils.i(e.toString());
            e.printStackTrace();
        } finally {
            try {
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    private static String bufferToHex(byte bytes[]) {
        return bufferToHex(bytes, 0, bytes.length);
    }

    private static String bufferToHex(byte bytes[], int m, int n) {
        StringBuilder stringBuilder = new StringBuilder(2 * n);
        int k = m + n;
        for (int l = m; l < k; l++) {
            char c0 = hexDigits[(bytes[l] & 0xf0) >> 4];
            char c1 = hexDigits[bytes[l] & 0xf];
            stringBuilder.append(c0);
            stringBuilder.append(c1);
        }
        return stringBuilder.toString();
    }
}
