package com.xyoye.dandanplay.utils.torrent;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Base64;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import libtorrent.Libtorrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentUtil {
    //初始化种子工具
    public static void initLibTorrent(Context context){
        String announces = "udp://exodus.desync.com:6969\nudp://tracker.leechers-paradise.org:6969";
        Libtorrent.setDefaultAnnouncesList(announces);
        Libtorrent.setVersion("dandanplay-beta");
        Libtorrent.setBindAddr(":0");
        Libtorrent.torrentStorageSet(IApplication.torrentStorage);
        if (!Libtorrent.create())
            throw new RuntimeException(Libtorrent.error());
        Libtorrent.setUploadRate(-1);
        Libtorrent.setDownloadRate(-1);
        if (AppConfig.getInstance().isFirstStart()) {
            IApplication.trackers = CommonUtils.readTracker(context);
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            for (String tracker : IApplication.trackers){
                ContentValues values=new ContentValues();
                values.put(DataBaseInfo.getFieldNames()[8][1], tracker);
                sqLiteDatabase.insert(DataBaseInfo.getTableNames()[8], null, values);
            }
        }else {
            SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
            String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[8];
            Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{});
            while (cursor.moveToNext()){
                String tracker = cursor.getString(1);
                IApplication.trackers.add(tracker);
            }
            cursor.close();
        }
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
            String danmuPath = cursor.getString(5);
            int episodeId = cursor.getInt(6);
            String magnet = cursor.getString(7);
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
            torrent.setDanmuPath(danmuPath);
            torrent.setEpisodeId(episodeId);
            torrent.setMagnet(magnet);

            torrent.setTitle(Libtorrent.torrentName(id));
            torrent.setStatus(Libtorrent.torrentStatus(id));
            torrent.setSize(Libtorrent.torrentBytesLength(id));
            long fileCount = Libtorrent.torrentFilesCount(id);
            List<Torrent.TorrentFile> torrentFileList = new ArrayList<>();
            for (int i=0; i<fileCount; i++){
                libtorrent.File libFile = Libtorrent.torrentFiles(id, i);
                Torrent.TorrentFile torrentFile = new Torrent.TorrentFile();
                torrentFile.setId(i);
                torrentFile.setTorrentId(id);
                torrentFile.setCheck(libFile.getCheck());
                torrentFile.setName(FileUtils.getFileName(libFile.getPath()));
                torrentFile.setPath(libFile.getPath());
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
            //如果以文件夹形式保存种子和视频只需删除整个文件夹
            //否则删除对应下载文件与种子文件
            if (StringUtils.isEmpty(torrent.getAnimeTitle())){
                File torrentFile = new File(torrent.getPath());
                if (torrentFile.exists()){
                    torrentFile.delete();
                }
                for(Torrent.TorrentFile realFile : torrent.getTorrentFileList()){
                    File file = new File(realFile.getPath());
                    if (file.exists()){
                        file.delete();
                    }
                }
            }else {
                File folderFile = new File(torrent.getAnimeTitle());
                if (folderFile.exists())
                    folderFile.delete();
            }

        }
    }

    //保存种子记录
    public static void saveTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[6][1], torrent.getPath());
        values.put(DataBaseInfo.getFieldNames()[6][2], torrent.getAnimeTitle());
        values.put(DataBaseInfo.getFieldNames()[6][3], state);
        values.put(DataBaseInfo.getFieldNames()[6][4], torrent.isDone() ? 1 : 0);
        values.put(DataBaseInfo.getFieldNames()[6][5], torrent.getDanmuPath());
        values.put(DataBaseInfo.getFieldNames()[6][6], torrent.getEpisodeId());
        values.put(DataBaseInfo.getFieldNames()[6][7], torrent.getMagnet());
        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[6], null, values);
    }

    //更新种子记录
    public static void updateTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        ContentValues values=new ContentValues();
        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        values.put(DataBaseInfo.getFieldNames()[6][3], state);
        values.put(DataBaseInfo.getFieldNames()[6][4], torrent.isDone() ? 1 : 0);
        values.put(DataBaseInfo.getFieldNames()[6][5], torrent.getDanmuPath());
        values.put(DataBaseInfo.getFieldNames()[6][6], torrent.getEpisodeId());
        values.put(DataBaseInfo.getFieldNames()[6][7], torrent.getMagnet());
        sqLiteDatabase.update(DataBaseInfo.getTableNames()[6], values, "torrent_path = ?", new String[]{torrent.getPath()});
    }

    //解析种子
    public static boolean prepareTorrentFromBytes(Torrent torrent, Uri parentFolder, byte[] buf) {
        long id = Libtorrent.addTorrentFromBytes(parentFolder.toString(), buf);
        if (id == -1) return false;
        String downloadFolder = StringUtils.isEmpty(torrent.getAnimeTitle())
                ? AppConfig.getInstance().getDownloadFolder()
                : AppConfig.getInstance().getDownloadFolder() + "/" + torrent.getAnimeTitle();
        torrent.setHash(Libtorrent.torrentHash(id));
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
            torrentFile.setName(libFile.getPath());
            torrentFile.setPath(downloadFolder + "/" +libFile.getPath());
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
    private static String formatTime(int tt) {
        return String.format("%02d", tt);
    }
}
