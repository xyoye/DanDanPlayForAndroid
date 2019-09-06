package com.xyoye.dandanplay.utils.jlibtorrent;

import android.database.Cursor;

import com.frostwire.jlibtorrent.ErrorCode;
import com.frostwire.jlibtorrent.FileStorage;
import com.xyoye.dandanplay.utils.Constants;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentUtil {
    //Magnet Header
    public static final String MAGNET_HEADER = "magnet:?xt=urn:btih:";

    /**
     * 删除下载任务中的文件
     */
    public static void deleteTaskFile(Torrent torrent) {
//        if (StringUtils.isEmpty(torrent.getAnimeTitle())) {
//            //无番剧标题，删除整个下载文件夹，因为每个下载文件的文件夹大概率不相同
//            FileUtils.deleteDir(torrent.getSaveDirPath());
//        } else {
//            //有番剧标题，判断是否为多个文件
//            File childFile = new File(torrent.getTorrentFileList().get(0).getPath());
//            File parentFile = childFile.getParentFile();
//            //单文件，直接删除文件，多文件删除多文件文件夹
//            if (parentFile.getAbsolutePath().endsWith(torrent.getAnimeTitle())) {
//                FileUtils.delete(torrent.getTorrentFileList().get(0).getPath());
//            } else {
//                FileUtils.deleteDir(parentFile);
//            }
//        }
    }

    /**
     * 将下载任务中迁移到已完成任务
     */
    public static void transferDownloaded(Torrent torrent, TorrentTask torrentTask){
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(16)
                .query()
                .execute();
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        while (cursor.moveToNext()){
            DataBaseManager.getInstance()
                    .selectTable(14)
                    .insert()
                    .param(1, torrent.getTitle())
                    .param(2, torrent.getSaveDirPath())
                    .param(3, torrent.getTorrentPath())
                    .param(4, torrent.getHash())
                    .param(5, torrent.getAnimeTitle())
                    .param(6, torrentTask.getTotalWanted())
                    .param(7, simpleDateFormat.format(new Date()))
                    .postExecute();

            FileStorage fileStorage = torrentTask.getTorrentFiles();
            for (int i = 0; i < fileStorage.numFiles(); i++) {
                DataBaseManager.getInstance()
                        .selectTable(15)
                        .insert()
                        .param(1, torrent.getHash())
                        .param(2, fileStorage.filePath(i))
                        .param(3, fileStorage.fileSize(i))
                        .postExecute();
            }
        }

        deleteDownloadingData(torrent.getHash());
    }

    /**
     * 移除数据库中正在下载的文件
     */
    public static void deleteDownloadingData(String torrentHash){
        DataBaseManager.getInstance().selectTable(16)
                .delete()
                .where(1, torrentHash)
                .postExecute();
    }

    /**
     * 将种子下载新任务保存到数据库中
     */
    public static void insertNewTask(String torrentHash,
                                     String torrentFilePath,
                                     String saveDirPath,
                                     String animeTitle,
                                     String priorities) {
        DataBaseManager.getInstance()
                .selectTable(16)
                .insert()
                .param(1, torrentHash)
                .param(2, torrentFilePath)
                .param(3, saveDirPath)
                .param(4, animeTitle)
                .param(5, priorities)
                .postExecute();
    }

    /**
     * 查询所有未完成的任务
     */
    public static List<Torrent> queryRestoreTorrentList() {
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable(16)
                .query()
                .execute();

        List<Torrent> torrentList = new ArrayList<>();

        while (cursor.moveToNext()){
            Torrent torrent = new Torrent(
                    cursor.getString(1),
                    cursor.getString(2),
                    cursor.getString(3),
                    cursor.getString(4)
            );
            torrentList.add(torrent);
        }

        return torrentList;
    }

    /**
     * 转换错误信息
     */
    public static String getErrorMsg(ErrorCode error) {
        return (error == null ? "" : error.message() + ", code " + error.value());
    }

    /**
     * 保存session信息到文件
     */
    public static void saveSessionData(byte[] data) {
        File sessionFile = new File(Constants.DefaultConfig.torrentSessionPath);
        saveDataToFile(sessionFile, data);
    }

    /**
     * 保存session信息到文件
     */
    public static void saveResumeData(byte[] data) {
        File resumeFile = new File(Constants.DefaultConfig.torrentResumeFilePath);
        saveDataToFile(resumeFile, data);
    }

    /**
     * 保存数据到文件
     */
    private static void saveDataToFile(File file, byte[] data) {
        try (OutputStream outputStream = new FileOutputStream(file, false)) {
            outputStream.write(data, 0, data.length);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
