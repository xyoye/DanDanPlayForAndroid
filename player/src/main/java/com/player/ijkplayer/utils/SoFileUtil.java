package com.player.ijkplayer.utils;

import android.content.Context;

import com.blankj.utilcode.util.EncryptUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.ToastUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyy on 2018/12/20.
 */

public class SoFileUtil {
    /**
     * 加载 so 文件
     * @param context
     * @param new_sos_parent so文件原来的目录
     */
    public static void loadSoFile(Context context, String new_sos_parent) {
        //data/user/com.xyoye.dandanplay/app_libs
        File so_root_file = context.getDir("libs", Context.MODE_PRIVATE);
        //data/user/com.xyoye.dandanplay/app_libs/armeabi-v7a
        String so_root_parent= so_root_file.getAbsolutePath() + "/" + android.os.Build.CPU_ABI;
        if (!isLoadSoFile(so_root_parent, new_sos_parent)) {
            copy(new_sos_parent, so_root_parent);
        }
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
        return soList;
    }

    /**
     * 校验md5判断 so 文件是否存在
     */
    private static boolean isLoadSoFile(String so_root_path, String new_sos_parent) {
        File new_sos_parentFile = new File(new_sos_parent);
        File[] oldFiles = new File(so_root_path).listFiles();
        if (oldFiles == null || oldFiles.length == 0)
            return false;

        if (new_sos_parentFile.isDirectory()){
            File[] newFiles = new_sos_parentFile.listFiles();
            if (oldFiles.length != newFiles.length)
                return false;
            List<String> oldMd5List = new ArrayList<>();
            List<String> newMd5List = new ArrayList<>();
            for (File oldFile : oldFiles) {
                oldMd5List.add(EncryptUtils.encryptMD5File2String(oldFile));
            }
            for (File newFile : newFiles) {
                newMd5List.add(EncryptUtils.encryptMD5File2String(newFile));
            }
            int equalsN=0;
            for (String newSo : newMd5List){
                if (oldMd5List.contains(newSo) && checkMd5(newSo))
                    equalsN++;
                else
                    break;
            }
            return equalsN == oldMd5List.size();
        }else if (new_sos_parentFile.isFile()) {
            if (oldFiles.length != 1) return false;
            String oldMd5 = EncryptUtils.encryptMD5File2String(oldFiles[0]);
            String newMd5 = EncryptUtils.encryptMD5File2String(new_sos_parentFile);
            return newMd5.equals(oldMd5);
        }
        return false;
    }

    /**
     *
     * @param fromFile 指定的下载目录
     * @param toFile 应用的包路径
     * @return
     */
    private static void copy(String fromFile, String toFile) {
        //要复制的文件目录
        File[] currentFiles;
        File root = new File(fromFile);
        //如同判断SD卡是否存在或者文件是否存在,如果不存在则 return出去
        if (!root.exists()) {
            return;
        }
        //如果存在则获取当前目录下的全部文件 填充数组
        currentFiles = root.listFiles();

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

    private static boolean checkMd5(String md5){
        switch (android.os.Build.CPU_ABI){
            case "armeabi-v7a":
                return md5.equals(Constants.ARMEABI_V7A.ijkffmpeg_md5) ||
                        md5.equals(Constants.ARMEABI_V7A.ijkplayer_md5) ||
                        md5.equals(Constants.ARMEABI_V7A.ijksdl_md5);
            case "armeabi":
                return md5.equals(Constants.ARMEABI.ijkffmpeg_md5) ||
                        md5.equals(Constants.ARMEABI.ijkplayer_md5) ||
                        md5.equals(Constants.ARMEABI.ijksdl_md5);
            case "arm64-v8a":
                return md5.equals(Constants.ARM64_V8A.ijkffmpeg_md5) ||
                        md5.equals(Constants.ARM64_V8A.ijkplayer_md5) ||
                        md5.equals(Constants.ARM64_V8A.ijksdl_md5);
            case "x86":
                return md5.equals(Constants.X86.ijkffmpeg_md5) ||
                        md5.equals(Constants.X86.ijkplayer_md5) ||
                        md5.equals(Constants.X86.ijksdl_md5);
            case "x86_64":
                return md5.equals(Constants.X86_64.ijkffmpeg_md5) ||
                        md5.equals(Constants.X86_64.ijkplayer_md5) ||
                        md5.equals(Constants.X86_64.ijksdl_md5);
            default:
                return false;
        }
    }
}
