package com.xyoye.dandanplay.utils.jlibtorrent;

import android.database.Cursor;

import com.blankj.utilcode.util.FileUtils;
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
    public static void deleteTaskFile(String saveDirPath) {
        FileUtils.deleteDir(saveDirPath);
    }

    /**
     * 将下载任务中迁移到已完成任务
     */
    public static void transferDownloaded(Torrent torrent, TorrentTask torrentTask) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.getDefault());
        DataBaseManager.getInstance()
                .selectTable("downloaded_task")
                .insert()
                .param("task_title", torrent.getTitle())
                .param("save_dir_path", torrent.getSaveDirPath())
                .param("torrent_file_path", torrent.getTorrentPath())
                .param("torrent_hash", torrent.getHash())
                .param("total_length", torrentTask.getTotalWanted())
                .param("complete_time", simpleDateFormat.format(new Date()))
                .postExecute();

        FileStorage fileStorage = torrentTask.getTorrentFiles();
        for (int i = 0; i < fileStorage.numFiles(); i++) {
            String filePath = torrent.getSaveDirPath() + "/" + fileStorage.filePath(i);
            DataBaseManager.getInstance()
                    .selectTable("downloaded_file")
                    .insert()
                    .param("task_torrent_hash", torrent.getHash())
                    .param("file_path", filePath)
                    .param("file_length", fileStorage.fileSize(i))
                    .postExecute();
        }

        deleteDownloadingData(torrent.getHash());
    }

    /**
     * 移除数据库中正在下载的文件
     */
    public static void deleteDownloadingData(String torrentHash) {
        DataBaseManager.getInstance()
                .selectTable("downloading_task")
                .delete()
                .where("task_torrent_hash", torrentHash)
                .postExecute();
    }

    /**
     * 将种子下载新任务保存到数据库中
     */
    public static void insertNewTask(Torrent torrent) {

        Cursor cursor = DataBaseManager.getInstance()
                .selectTable("downloading_task")
                .query()
                .where("task_torrent_hash", torrent.getHash())
                .execute();

        if (cursor.getCount() == 0) {

            DataBaseManager.getInstance()
                    .selectTable("downloading_task")
                    .insert()
                    .param("task_torrent_hash", torrent.getHash())
                    .param("torrent_file_path", torrent.getTorrentPath())
                    .param("save_dir_path", torrent.getSaveDirPath())
                    .param("priorities", torrent.getPriorityStr())
                    .postExecute();
        }


    }

    /**
     * 查询所有未完成的任务
     */
    public static List<Torrent> queryRestoreTorrentList() {
        Cursor cursor = DataBaseManager.getInstance()
                .selectTable("downloading_task")
                .query()
                .execute();

        List<Torrent> torrentList = new ArrayList<>();

        while (cursor.moveToNext()) {
            Torrent torrent = new Torrent(
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
