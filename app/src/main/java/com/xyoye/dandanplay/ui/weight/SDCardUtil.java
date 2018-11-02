package com.xyoye.dandanplay.ui.weight;

import android.content.Context;
import android.net.Uri;
import android.support.v4.provider.DocumentFile;

import com.blankj.utilcode.util.CloseUtils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by xyy on 2018/11/1.
 */

public class SDCardUtil {

    //判断文件是否在sd卡中存在，并同时创建不在的文件夹，返回创建的文件夹或者文件路径。
    public static DocumentFile isFileExist(Context context, String rootPath, String path){
        String fileName = "";
        //获取文件名
        int end = path.lastIndexOf("/");
        if (end != -1){
            fileName = path.substring(end+1, path.length());
        }
        //获取需要查询的文件夹
        if (path.startsWith(rootPath))
            path = path.substring(rootPath.length(), path.length());
        //去除开头的“/”
        if (path.startsWith("/"))
            path = path.substring(1, path.length());
        DocumentFile rootFile = DocumentFile.fromTreeUri(context, Uri.parse(rootPath));
        String[] folderFiles = path.split("/");
        for(int i=0; i<folderFiles.length; i++){
            String folder = folderFiles[i];
            DocumentFile folderFile = getFolderDocumentFile(rootFile, folder);
            if (folderFile == null){
                if (folder.equals(fileName) && i == folderFiles.length-1){
                    rootFile = rootFile.createFile("*/torrent", fileName);
                    break;
                }else {
                    rootFile = rootFile.createDirectory(folder);
                }
            }else {
                rootFile = folderFile;
                if(folderFile.isFile())
                    break;
            }
        }
        return rootFile;
    }

    private static DocumentFile getFolderDocumentFile(DocumentFile documentFiles, String folder){
        for (DocumentFile documentFile : documentFiles.listFiles()){
            if (documentFile.isDirectory() && documentFile.getName().equals(folder)){
                return documentFile;
            }else if (documentFile.isFile() && folder.contains(".")){
                if (documentFile.getName().equals(folder))
                    return documentFile;
            }
        }
        return null;
    }

    public static String createNewFile(Context context, DocumentFile torrentFile, InputStream inputStream){
        if (torrentFile != null){
            try {
                OutputStream outputStream = context.getContentResolver().openOutputStream(torrentFile.getUri());
                if (outputStream == null) return "";
                try {
                    byte data[] = new byte[8192];
                    int len;
                    while ((len = inputStream.read(data, 0, 8192)) != -1) {
                        outputStream.write(data, 0, len);
                    }
                    outputStream.flush();
                    return torrentFile.getUri().toString();
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    CloseUtils.closeIO(inputStream, outputStream);
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return "";
    }
}
