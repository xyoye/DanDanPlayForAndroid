package com.xyoye.dandanplay.utils.torrent;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Base64;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.Constants;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import libtorrent.Libtorrent;
import libtorrent.StatsTorrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentUtil {
    //初始化种子工具
    public static void initLibTorrent(){
        String announces = "udp://exodus.desync.com:6969\nudp://tracker.leechers-paradise.org:6969";
        Libtorrent.setDefaultAnnouncesList(announces);
        Libtorrent.setVersion("dandanplay-beta");
        Libtorrent.setBindAddr(":0");
        Libtorrent.torrentStorageSet(IApplication.torrentStorage);
        if (!Libtorrent.create())
            throw new RuntimeException(Libtorrent.error());
        Libtorrent.setUploadRate(-1);
        Libtorrent.setDownloadRate(-1);
    }

    //加载种子历史
    public static void loadTorrent(){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[6];
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{});
        while (cursor.moveToNext()){
            Torrent torrent = new Torrent();
            String path = cursor.getString(1);
            String animaTitle = cursor.getString(2);
            String state = cursor.getString(3);
            Boolean isDone = cursor.getInt( 4) == 1;
            String magnet = cursor.getString(5);
            byte[] stateByte = Base64.decode(state, Base64.DEFAULT);
            long id = Libtorrent.loadTorrent(path, stateByte);
            if (id == -1) {
                LogUtils.e(Libtorrent.error());
                continue;
            }
            String hash = Libtorrent.torrentHash(id);

            torrent.setDone(isDone);
            torrent.setId(id);
            torrent.setHash(hash);
            torrent.setPath(path);
            torrent.setAnimeTitle(animaTitle);
            torrent.setMagnet(magnet);
            torrent.setTitle(Libtorrent.torrentName(id));
            torrent.setStatus(Libtorrent.torrentStatus(id));
            torrent.setSize(Libtorrent.torrentBytesLength(id));

            long fileCount = Libtorrent.torrentFilesCount(id);
            List<Torrent.TorrentFile> torrentFileList = new ArrayList<>();
            for (int i=0; i<fileCount; i++){
                libtorrent.File libFile = Libtorrent.torrentFiles(id, i);
                if (!libFile.getCheck()) continue;

                Torrent.TorrentFile torrentFile = new Torrent.TorrentFile();
                String filePath = torrent.getParentFolder() +"/"+ libFile.getPath();

                //获取绑定的弹幕记录
                String fileSql = "SELECT * FROM " + DataBaseInfo.getTableNames()[12] + " WHERE torrent_path=? AND torrent_file_path=?";
                Cursor fileCursor = sqLiteDatabase.rawQuery(fileSql, new String[]{torrent.getPath(), filePath});
                while (fileCursor.moveToNext()){
                    String danmuPath = cursor.getString(3);
                    String episodeId = cursor.getString(4);
                    torrentFile.setDanmuPath(danmuPath);
                    torrentFile.setTorrentId(Long.parseLong(episodeId));
                }
                fileCursor.close();

                torrentFile.setId(i);
                torrentFile.setTorrentId(id);
                torrentFile.setCheck(libFile.getCheck());
                torrentFile.setName(FileUtils.getFileName(libFile.getPath()));
                torrentFile.setPath(filePath);
                torrentFile.setOriginPath(libFile.getPath());
                torrentFile.setLength(libFile.getLength());
                torrentFileList.add(torrentFile);
            }
            torrent.setTorrentFileList(torrentFileList);

            IApplication.torrentList.add(torrent);
            IApplication.torrentStorage.addHash(hash, torrent);
        }
        cursor.close();
    }

    //删除种子记录
    public static void deleteTorrent( Torrent torrent, boolean isDeleteFile){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        sqLiteDatabase.delete("torrent", "torrent_path=?" , new String[]{torrent.getPath()});
        Libtorrent.removeTorrent(torrent.getId());

        if (isDeleteFile){
            if (StringUtils.isEmpty(torrent.getAnimeTitle())){
                //删除下载文件
                for (Torrent.TorrentFile torrentDetailFile : torrent.getTorrentFileList()){
                    File realFile = new File(torrentDetailFile.getPath());
                    realFile.deleteOnExit();
                }

                String folderPath = AppConfig.getInstance().getDownloadFolder() + "/" +torrent.getTitle();
                FileUtils.deleteDir(folderPath);

                //下载目录为空则删除
//                String folderPath = AppConfig.getInstance().getDownloadFolder() + "/" +torrent.getTitle();
//                File folderFile = new File(folderPath);
//                if (folderFile.listFiles().length == 0)
//                    folderFile.deleteOnExit();

            }else {
                //将种子文件移动到缓存目录下torrent文件夹
                File torrentFile = new File(torrent.getPath());
                if (torrentFile.exists()){
                    File destFolder = new File(AppConfig.getInstance().getDownloadFolder() + Constants.DefaultConfig.torrentFolder);
                    if (!destFolder.exists() || (destFolder.exists() && !destFolder.isDirectory())){
                        destFolder.mkdirs();
                    }
                    File destFile = new File(destFolder, FileUtils.getFileName(torrentFile));
                    FileUtils.moveFile(torrentFile, destFile);
                    torrentFile.delete();
                    //如果文件夹为空删除文件夹
                    File torrentFolder = new File(FileUtils.getDirName(torrent.getPath()));
                    if (torrentFolder.exists() && torrentFolder.listFiles().length == 0){
                        torrentFolder.delete();
                    }
                }
                //删除下载文件
                for (Torrent.TorrentFile torrentDetailFile : torrent.getTorrentFileList()){
                    File realFile = new File(torrentDetailFile.getPath());
                    if (realFile.exists())
                        realFile.delete();
                }

                String folderPath = AppConfig.getInstance().getDownloadFolder() + "/" +torrent.getAnimeTitle();
                FileUtils.deleteDir(folderPath);

                //下载目录为空则删除
//                String folderPath = AppConfig.getInstance().getDownloadFolder() + "/" +torrent.getTitle();
//                File detailFolder = new File(folderPath);
//                if (detailFolder.exists() && detailFolder.isDirectory() && detailFolder.listFiles().length == 0){
//                    if (detailFolder.exists()) detailFolder.delete();
//                }
//
//                folderPath = AppConfig.getInstance().getDownloadFolder() + "/" +torrent.getAnimeTitle();
//                File folderFile = new File(folderPath);
//                if (folderFile.listFiles().length == 0){
//                    if (folderFile.exists()) folderFile.delete();
//                }
            }
        }
    }

    //保存种子记录
    public static void saveTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        ContentValues values = new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[6][1], torrent.getPath());
        values.put(DataBaseInfo.getFieldNames()[6][2], torrent.getAnimeTitle());
        values.put(DataBaseInfo.getFieldNames()[6][3], state);
        values.put(DataBaseInfo.getFieldNames()[6][4], 0);
        values.put(DataBaseInfo.getFieldNames()[6][5], torrent.getMagnet());
        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[6], null, values);
        for (Torrent.TorrentFile torrentFile : torrent.getTorrentFileList()){
            ContentValues detailValues = new ContentValues();
            values.put(DataBaseInfo.getFieldNames()[12][1], torrent.getPath());
            values.put(DataBaseInfo.getFieldNames()[12][2], torrentFile.getPath());
            sqLiteDatabase.insert(DataBaseInfo.getTableNames()[12], null, detailValues);
        }
    }

    //更新种子详情记录-绑定弹幕
    public static void updateTorrentDanmu(String torrentPath, String torrent_file_path, String danmuPath, int episodeId){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[12][3], danmuPath);
        values.put(DataBaseInfo.getFieldNames()[12][4], episodeId);
        sqLiteDatabase.update(DataBaseInfo.getTableNames()[12], values, "torrent_path=? and torrent_file_path=?", new String[]{torrentPath, torrent_file_path});
    }

    //更新种子下载进度
    public static void updateTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        ContentValues values = new ContentValues();

        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        values.put(DataBaseInfo.getFieldNames()[6][3], state);

        StatsTorrent stats = Libtorrent.torrentStats(torrent.getId());
        TorrentSpeed.getInstance().getDownloadSpeed(torrent.getId()).step(stats.getDownloaded());
        TorrentSpeed.getInstance().getUploadSpeed(torrent.getId()).step(stats.getUploaded());
        if (Libtorrent.metaTorrent(torrent.getId())) {
            long l = Libtorrent.torrentPendingBytesLength(torrent.getId());
            long c = Libtorrent.torrentPendingBytesCompleted(torrent.getId());
            if (l > 0 && l == c && !torrent.isDone()){
                torrent.setDone(true);
                values.put(DataBaseInfo.getFieldNames()[6][4], 1);
            }else {
                values.put(DataBaseInfo.getFieldNames()[6][4], 0);
            }
        } else {
            torrent.setDone(false);
            values.put(DataBaseInfo.getFieldNames()[6][4], 0);
        }
        sqLiteDatabase.update(DataBaseInfo.getTableNames()[6], values, "torrent_path = ?", new String[]{torrent.getPath()});
    }

    //解析种子
    public static boolean prepareTorrentFromBytes(Torrent torrent, byte[] buf) {
        File parentFolderFile = new File(torrent.getParentFolder());
        if (!parentFolderFile.exists())
            parentFolderFile.mkdirs();
        Uri parentFolderUri = Uri.fromFile(parentFolderFile);

        long id = Libtorrent.addTorrentFromBytes(parentFolderUri.toString(), buf);
        String hash = Libtorrent.torrentHash(id);
        if (id == -1 || StringUtils.isEmpty(hash)) return false;
        torrent.setHash(hash);
        torrent.setId(id);
        torrent.setTitle(Libtorrent.torrentName(id));
        torrent.setStatus(Libtorrent.torrentStatus(id));
        long fileCount = Libtorrent.torrentFilesCount(id);
        List<Torrent.TorrentFile> torrentFileList = new ArrayList<>();
        for (int i=0; i<fileCount; i++){
            libtorrent.File libFile = Libtorrent.torrentFiles(id, i);
            Torrent.TorrentFile torrentFile = new Torrent.TorrentFile();
            torrentFile.setId(i);
            torrentFile.setTorrentId(id);
            torrentFile.setCheck(libFile.getCheck());
            torrentFile.setName(FileUtils.getFileName(libFile.getPath()));
            torrentFile.setPath(torrent.getParentFolder() +"/"+ libFile.getPath());
            torrentFile.setOriginPath(libFile.getPath());
            torrentFile.setLength(libFile.getLength());
            torrentFileList.add(torrentFile);
        }
        torrent.setTorrentFileList(torrentFileList);
        return true;
    }

    //解析进度
    public static String formatDuration(Context context, long diff) {
        int diffSeconds = (int) (diff / 1000 % 60);
        int diffMinutes = (int) (diff / (60 * 1000) % 60);
        int diffHours = (int) (diff / (60 * 60 * 1000) % 24);
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));

        String str;

        if (diffDays > 1)
            str = "48h+";
        else if (diffHours > 0)
            str = formatTime(diffHours) + ":" + formatTime(diffMinutes) + ":" + formatTime(diffSeconds);
        else
            str = formatTime(diffMinutes) + ":" + formatTime(diffSeconds);

        return str;
    }

    //解析时间
    @SuppressLint("DefaultLocale")
    private static String formatTime(int tt) {
        return String.format("%02d", tt);
    }

    public static String getSpeed(Context context, Torrent torrent){
        long DAY = 24 * 60 * 60 * 1000;

        if (torrent.isDone()) return "- · ↓ 0B/s · ↑ 0B/s";
        if (torrent.isError()) return "- · ↓ 0B/s · ↑ 0B/s";
        String str = "";
        switch (Libtorrent.torrentStatus(torrent.getId())) {
            case Libtorrent.StatusQueued:
            case Libtorrent.StatusPaused:
                str += "- · ↓ 0B/s · ↑ 0B/s";
                break;
            case Libtorrent.StatusSeeding:
            case Libtorrent.StatusChecking:
            case Libtorrent.StatusDownloading:
                long c = 0;
                if (Libtorrent.metaTorrent(torrent.getId())) {
                    long p = Libtorrent.torrentPendingBytesLength(torrent.getId());
                    c = p - Libtorrent.torrentPendingBytesCompleted(torrent.getId());
                }

                int a = TorrentSpeed.getInstance().getDownloadSpeed(torrent.getId()).getAverageSpeed();
                String left = "∞";
                if (c > 0 && a > 0) {
                    long diff = c * 1000 / a;
                    int diffDays = (int) (diff / (DAY));
                    if (diffDays < 30)
                        left = "" + TorrentUtil.formatDuration(context, diff) + "";
                }
                str += left;

                int downloadCurrentSpeed = TorrentSpeed.getInstance().getDownloadSpeed(torrent.getId()).getCurrentSpeed();
                int uploadCurrentSpeed = TorrentSpeed.getInstance().getUploadSpeed(torrent.getId()).getCurrentSpeed();
                str += " · ↓ " + CommonUtils.convertFileSize(downloadCurrentSpeed) + context.getString(R.string.per_second);
                str += " · ↑ " + CommonUtils.convertFileSize(uploadCurrentSpeed) + context.getString(R.string.per_second);
                break;
        }

        return str.trim();
    }
}
