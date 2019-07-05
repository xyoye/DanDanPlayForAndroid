package com.xyoye.dandanplay.utils;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * Created by xyoye on 2018/12/4.
 */

public class CommonUtils {
    private static final String EXTERNAL_STORAGE = "com.android.externalstorage.documents";
    private static final String DOWNLOAD_DOCUMENT = "com.android.providers.downloads.documents";
    private static final String MEDIA_DOCUMENT = "com.android.providers.media.documents";
    private static final String DOWNLOAD_URI = "content://downloads/public_downloads";

    /**
     * 获取本地软件版本号
     */
    public static String getLocalVersion(Context context) {
        String localVersionName = "";
        try {
            PackageInfo packageInfo = context.getApplicationContext()
                    .getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            localVersionName = packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return localVersionName;
    }

    /**
     * 时间格式化
     */
    public static String formatDuring(long mss) {
        long hours = mss / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        long seconds = (mss % (1000 * 60)) / 1000;
        StringBuilder stringBuilder=new StringBuilder();
        if (hours != 0){
            if (hours < 10)
                stringBuilder.append("0").append(hours).append(":");
            else
                stringBuilder.append(hours).append(":");
        }
        if (minutes == 0){
            stringBuilder.append("00:");
        } else{
            if (minutes < 10)
                stringBuilder.append("0").append(minutes).append(":");
            else
                stringBuilder.append(minutes).append(":");
        }
        if (seconds == 0){
            stringBuilder.append("00");
        } else{
            if (seconds < 10)
                stringBuilder.append("0").append(seconds);
            else
                stringBuilder.append(seconds);
        }
        return stringBuilder.toString();
    }
    /**
     * 读取tracker
     */
    public static List<String> readTracker(Context context) {
        List<String> stringList = new ArrayList<>();
        try {
            InputStream inputStream = context.getAssets().open("tracker.txt");
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String line;
            while (( line = bufferedReader.readLine()) != null) {
                if (StringUtils.isEmpty(line))
                    continue;
                stringList.add(line);
            }
        } catch (IOException ee) {
            ee.printStackTrace();
        }
        return stringList;
    }

    /**
     * 文件大小格式化
     */
    @SuppressLint("DefaultLocale")
    public static String convertFileSize(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.1f GB", (float) size / gb);
        } else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f M" : "%.1f M", f);
        } else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f K" : "%.1f K", f);
        } else
            return String.format("%d B", size);
    }

    /**
     * 判断数字
     */
    public static boolean isNum(String str){
        Pattern pattern = Pattern.compile("^-?[0-9]+");
        return pattern.matcher(str).matches();
    }

    /**
     * 判断url
     */
    public static boolean isUrlLink(String str){
        String regex = "^([hH][tT]{2}[pP]://|[hH][tT]{2}[pP][sS]://)(([A-Za-z0-9-~]+).)+([A-Za-z0-9-~/])+$";
        Pattern pattern = Pattern.compile(regex);
        return pattern.matcher(str).matches();
    }

    /**
     * 判断视频格式
     */
    public static boolean isMediaFile(String fileName){
        switch (com.blankj.utilcode.util.FileUtils.getFileExtension(fileName).toLowerCase()){
            case "3gp":
            case "avi":
            case "flv":
            case "mp4":
            case "m4v":
            case "mkv":
            case "mov":
            case "mpeg":
            case "mpg":
            case "mpe":
            case "rm":
            case "rmvb":
            case "wmv":
            case "asf":
            case "asx":
            case "dat":
            case "vob":
            case "m3u8":
                return true;
            default: return false;
        }
    }

    //获取Uri真实地址
    public static @Nullable String getRealFilePath(Context context, Uri uri) {
        boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            switch (Objects.requireNonNull(uri.getAuthority())){
                case EXTERNAL_STORAGE:
                    String docId = DocumentsContract.getDocumentId(uri);
                    String[] exSplit = docId.split(":");
                    String type = exSplit[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        return Environment.getExternalStorageDirectory() + "/" + exSplit[1];
                    }
                    break;
                case DOWNLOAD_DOCUMENT:
                    String id = DocumentsContract.getDocumentId(uri);
                    Uri documentUri = ContentUris.withAppendedId(Uri.parse(DOWNLOAD_URI), Long.valueOf(id));
                    return getDataColumn(context, documentUri, null, null);
                case MEDIA_DOCUMENT:
                    String[] split = DocumentsContract.getDocumentId(uri).split(":");
                    Uri contentUri = null;
                    switch (split[0]){
                        case "image":
                            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "video":
                            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                            break;
                        case "audio":
                            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                            break;
                    }
                    return getDataColumn(context, contentUri, "_id=?", new String[]{split[1]});
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        context.grantUriPermission(context.getPackageName(), uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
        try (Cursor cursor = context.getContentResolver().query(uri, new String[]{"_data"}, selection, selectionArgs, null)) {
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow("_data"));
            }
        }
        return null;
    }

    /**
     * 通过当前时间获取当前文件名
     * @param header 头部："COV"
     * @param tail  尾部：".jpg";
     * @return  "/COV_20190227_090701.jpg"
     */
    @SuppressLint("SimpleDateFormat")
    public static String getCurrentFileName(String header, String tail){
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd_HHmmss");
        Date curDate = new Date(System.currentTimeMillis());//获取当前时间
        String curTime  = formatter.format(curDate);
        return "/"+header+"_"+curTime+tail;
    }
}
