package com.xyoye.dandanplay.utils;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

/**
 * Created by xyoye on 2019/5/10.
 */

public class HashUtils {
    public static String getFileHash(String filePath) {
        try {
            StringBuilder stringBuilder = new StringBuilder();
            RandomAccessFile file = new RandomAccessFile(filePath, "r");
            long fileLength = file.length();
            long[] positions = new long[]{4096, fileLength / 3 * 2, fileLength / 3, fileLength - 8192};
            for (long position : positions) {
                byte[] buffer = new byte[4096];
                if (fileLength < position) {
                    file.close();
                    return stringBuilder.toString();
                }
                file.seek(position);
                int realBufferSize = file.read(buffer);
                buffer = Arrays.copyOfRange(buffer, 0, realBufferSize);
                stringBuilder.append(bytesToMD5(buffer));
                stringBuilder.append(";");
            }
            file.close();
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
            return stringBuilder.toString();
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getFileSHA1(String filePath){
        RandomAccessFile file = null;
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("SHA1");
            file = new RandomAccessFile(filePath, "r");
            long fileLength = file.length();
            if(fileLength < 0xF000) {
                byte[] buffer = new byte[0xF000];
                file.seek(0);
                file.read(buffer);
                file.close();
                return bytesToString(messageDigest.digest(buffer)).toUpperCase();
            }
            int bufferSize = 0x5000;
            long[] positions = new long[]{0, fileLength / 3, fileLength - bufferSize};
            for (int i = 0; i < positions.length; i++) {
                long position = positions[i];
                byte[] buffer = new byte[bufferSize];
                file.seek(position);
                file.read(buffer);
                messageDigest.update(buffer);
            }
            file.close();
            return bytesToString(messageDigest.digest()).toUpperCase();
        }catch (IOException | NoSuchAlgorithmException e){
            e.printStackTrace();
        }finally {
            try {
                if (file != null)
                    file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return "";

    }

    private static String bytesToMD5(byte[] bytes) throws NoSuchAlgorithmException {
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        byte[] buffer = messageDigest.digest(bytes);
        return bytesToString(buffer);
    }

    private static String bytesToString(byte[] bytes) {
        StringBuilder stringBuilder = new StringBuilder();
        for (byte b : bytes) {
            int bias = (b & 0xf0) >>> 4;
            stringBuilder.append(Integer.toHexString(bias));
            bias = b & 0xf;
            stringBuilder.append(Integer.toHexString(bias));
        }
        return stringBuilder.toString();
    }
}
