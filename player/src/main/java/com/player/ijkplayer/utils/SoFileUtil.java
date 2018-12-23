package com.player.ijkplayer.utils;

import android.content.Context;
import android.os.Build;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.blankj.utilcode.util.ZipUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by xyy on 2018/12/20.
 */

public class SoFileUtil {
    public static IOException exception;
    /**
     * 加载 so 文件
     * @param context
     */
    public static boolean loadSoFile(Context context, String zipFilePath) {
        //data/user/com.xyoye.dandanplay/app_libs
        File so_root_file = context.getDir("libs", Context.MODE_PRIVATE);
        //data/user/com.xyoye.dandanplay/app_libs/armeabi-v7a
        String so_root_parent= so_root_file.getAbsolutePath() + "/" + android.os.Build.CPU_ABI;
        try {
            copy(zipFilePath, so_root_parent);
        } catch (IOException e) {
            e.printStackTrace();
            exception = e;
            return false;
        }
        return true;
    }

    public static List<String> getLoadedFile(){
        if (!SPUtils.getInstance().getBoolean("use_extra_so"))
            return new ArrayList<>();
        File dir = ContextUtil.getInstans().getContext().getDir("libs", Context.MODE_PRIVATE);
        File rootFile = new File(dir.getAbsolutePath() + "/" + android.os.Build.CPU_ABI);
        List<String> soList = new ArrayList<>();
        for (File file : rootFile.listFiles()){
            soList.add(file.getAbsolutePath());
        }
        Collections.sort(soList);
        return soList;
    }

    /**
     *
     * @param zipFilePath 压缩包路径
     * @param toFile 应用的包路径
     * 注：这里没有考虑解压文件时手机存储不足的情况
     */
    private static void copy(String zipFilePath, String toFile) throws IOException {
        //要复制的文件目录
        File root = new File(zipFilePath);

        //如同判断SD卡是否存在或者文件是否存在
        if (!root.exists()) {
            return;
        }
        //如果解压zip文件存在，解压so文件
        File unZipFolder = new File(FileUtils.getDirName(zipFilePath)+"/"+ Build.CPU_ABI);
        if (unZipFolder.exists())
            FileUtils.deleteAllInDir(unZipFolder);
        ZipUtils.unzipFile(zipFilePath, FileUtils.getDirName(zipFilePath));
        File[] currentFiles = unZipFolder.listFiles();

        //目标目录
        File targetDir = new File(toFile);
        //创建目录
        if (!targetDir.exists()) {
            targetDir.mkdirs();
        }
        //遍历要复制该目录下的全部文件
        for (int i = 0; i < currentFiles.length; i++) {
            if (currentFiles[i].isDirectory()) {
                //如果当前项为子目录 进行递归
                copy(currentFiles[i].getPath() + "/", toFile + currentFiles[i].getName() + "/");
            } else {
                //如果当前项为文件则进行文件拷贝
                if (currentFiles[i].getName().contains(".so")) {
                    copySdcardFile(currentFiles[i].getPath(), toFile + File.separator + currentFiles[i].getName());
                }
            }
        }
        FileUtils.deleteAllInDir(unZipFolder);
    }

    //文件拷贝
    //要复制的目录下的所有非子目录(文件夹)文件拷贝
    private static void copySdcardFile(String fromFile, String toFile) {
        try {
            FileInputStream fosfrom = new FileInputStream(fromFile);
            FileOutputStream fosto = new FileOutputStream(toFile);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = -1;
            while ((len = fosfrom.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            // 从内存到写入到具体文件
            fosto.write(baos.toByteArray());
            // 关闭文件流
            baos.close();
            fosto.close();
            fosfrom.close();
        } catch (Exception ex) {
            LogUtils.e(ex);
            ToastUtils.showShort("复制so库出错："+ex);
        }
    }

    //检测zip文件MD5，判断有无必要下载扩展包
    public static boolean checkZipSoMd5(String md5){
        switch (android.os.Build.CPU_ABI){
            case "armeabi-v7a":
                return md5.equals(Constants.ijk_armeabi_v7a);
            case "armeabi":
                return md5.equals(Constants.ijk_armeabi);
            case "arm64-v8a":
                return md5.equals(Constants.ijk_arm64_v8a);
            case "x86":
                return md5.equals(Constants.ijk_x86);
            case "x86_64":
                return md5.equals(Constants.ijk_x86_64);
            default:
                return false;
        }
    }
}
