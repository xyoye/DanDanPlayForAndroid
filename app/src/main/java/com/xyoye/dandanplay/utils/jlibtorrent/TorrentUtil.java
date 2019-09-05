package com.xyoye.dandanplay.utils.jlibtorrent;

import android.database.Cursor;
import android.support.annotation.Nullable;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.frostwire.jlibtorrent.TorrentInfo;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentUtil {

    /**
     * 通过种子文件获取种子信息
     */
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

    /**
     * 获取dht地址
     */
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

    /**
     * 删除下载任务中的文件
     */
    public static void deleteTaskFile(Torrent torrent){
        if (StringUtils.isEmpty(torrent.getAnimeTitle())){
            //无番剧标题，删除整个下载文件夹，因为每个下载文件的文件夹大概率不相同
            FileUtils.deleteDir(torrent.getSaveDirPath());
        }else {
            //有番剧标题，判断是否为多个文件
            File childFile = new File(torrent.getTorrentFileList().get(0).getPath());
            File parentFile = childFile.getParentFile();
            //单文件，直接删除文件，多文件删除多文件文件夹
            if (parentFile.getAbsolutePath().endsWith(torrent.getAnimeTitle())){
                FileUtils.delete(torrent.getTorrentFileList().get(0).getPath());
            }else {
                FileUtils.deleteDir(parentFile);
            }
        }
    }

    /**
     * 将种子下载新任务保存到数据库中
     */
    public static void insertNewTask(String torrentFilePath, String torrentHash, String animeTitle, String priorities){
        DataBaseManager.getInstance()
                .selectTable(16)
                .insert()
                .param(1, torrentFilePath)
                .param(2, torrentHash)
                .param(3, animeTitle)
                .param(4, priorities)
                .postExecute();
    }

    /**
     * 更新种子下载任务状态为已完成
     */
    public static void updateDBTorrentFinish(String torrentPath){
        DataBaseManager.getInstance()
                .selectTable(16)
                .update()
                .param(4, 1)
                .where(1, torrentPath)
                .postExecute();
    }

    /**
     * 查询所有种子下载任务
     */
    public static Cursor queryRecoveryTorrent(){
        return DataBaseManager.getInstance()
                .selectTable(16)
                .query()
                .execute();
    }

    /**
     * 删除种子文件下载任务
     */
    public static void deleteDBTorrent(String torrentPath){
        DataBaseManager.getInstance()
                .selectTable(6)
                .delete()
                .where(1, torrentPath)
                .execute();
    }
}
