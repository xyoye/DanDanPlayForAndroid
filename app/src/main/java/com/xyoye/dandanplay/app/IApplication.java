/*******************************************************************************
 * Copyright 2011-2013 Sergey Tarasevich
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
package com.xyoye.dandanplay.app;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.support.multidex.MultiDex;
import android.util.Base64;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.player.ijkplayer.utils.PlayerConfigShare;
import com.tencent.bugly.Bugly;
import com.xyoye.dandanplay.database.DataBaseHelper;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.KeyUtil;
import com.xyoye.dandanplay.utils.torrent.Torrent;
import com.xyoye.dandanplay.utils.torrent.TorrentStorage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import libtorrent.Libtorrent;
import me.yokeyword.fragmentation.BuildConfig;
import me.yokeyword.fragmentation.Fragmentation;

public class IApplication extends BaseApplication {
    static ThreadPoolExecutor executor;
    public static List<Torrent> torrentList = new ArrayList<>();
    public static TorrentStorage torrentStorage = new TorrentStorage();
    public static List<String> trackers = new ArrayList<>();

    public static Handler mainHandler;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onCreate() {

        LogUtils.i("onCreate");
        super.onCreate();
        MultiDex.install(this);
        Bugly.init(getApplicationContext(), KeyUtil.getAppId2(getApplicationContext()), false);
        initDatabase(new DataBaseHelper(this));
        PlayerConfigShare.initPlayerConfigShare(getApplicationContext());
        initLibTorrent();
        loadTorrent();

        Fragmentation.builder()
                .stackViewMode(Fragmentation.NONE)
                .debug(BuildConfig.DEBUG)
                .install();
    }

    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(3, 3, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(20));
        }
        return executor;
    }

    public static Handler getMainHander(){
        if (mainHandler == null){
            return new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    private void initLibTorrent(){
        String announces = "udp://exodus.desync.com:6969\nudp://tracker.leechers-paradise.org:6969";
        Libtorrent.setDefaultAnnouncesList(announces);
        Libtorrent.setVersion("dandanplay-beta");
        Libtorrent.setBindAddr(":0");
        Libtorrent.torrentStorageSet(IApplication.torrentStorage);
        if (!Libtorrent.create())
            throw new RuntimeException(Libtorrent.error());
        Libtorrent.setUploadRate(-1);
        Libtorrent.setDownloadRate(-1);
        trackers = CommonUtils.readTracker(getApplicationContext());
    }

    private void loadTorrent(){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[6];
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{});
        while (cursor.moveToNext()){
            Torrent torrent = new Torrent();
            String path = cursor.getString(1);
            String state = cursor.getString(2);
            Boolean isDone = cursor.getInt( 3) == 1;
            String danmuPath = cursor.getString(4);
            int episodeId = cursor.getInt(5);
            String magnet = cursor.getString(6);
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
            torrent.setDanmuPath(danmuPath);
            torrent.setEpisodeId(episodeId);
            torrent.setMagnet(magnet);
            torrent.setTitle(Libtorrent.torrentName(id));
            torrent.setStatus(Libtorrent.torrentStatus(id));
            torrent.setSize(Libtorrent.torrentBytesLength(id));
            if (path.contains("/")){
                String folder;
                if (path.contains("/torrent/")){
                    int end = path.indexOf("/torrent/");
                    folder = path.substring(0, end);
                }else {
                    int end = path.lastIndexOf("/");
                    folder = path.substring(0, end);
                }
                torrent.setFolder(folder+"/");
            }else {
                torrent.setFolder(path+"/");
            }
            torrentList.add(torrent);
            torrentStorage.addHash(hash, torrent);
        }
        cursor.close();
    }

    public static void deleteTorrent( Torrent torrent, boolean isDeleteFile){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        sqLiteDatabase.delete("torrent", "torrent_path=?" , new String[]{torrent.getPath()});
        Libtorrent.removeTorrent(torrent.getId());

        if (isDeleteFile){
            File folderFile = new File(torrent.getFolder());
            if (folderFile.exists()){
                folderFile.delete();
            }
        }
    }

    public static void saveTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[6][1], torrent.getPath());
        values.put(DataBaseInfo.getFieldNames()[6][2], state);
        values.put(DataBaseInfo.getFieldNames()[6][3], torrent.isDone() ? 1 : 0);
        values.put(DataBaseInfo.getFieldNames()[6][4], torrent.getDanmuPath());
        values.put(DataBaseInfo.getFieldNames()[6][5], torrent.getEpisodeId());
        values.put(DataBaseInfo.getFieldNames()[6][6], torrent.getMagnet());
        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[6], null, values);
    }

    public static void updateTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        ContentValues values=new ContentValues();
        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        values.put(DataBaseInfo.getFieldNames()[6][2], state);
        values.put(DataBaseInfo.getFieldNames()[6][3], torrent.isDone() ? 1 : 0);
        values.put(DataBaseInfo.getFieldNames()[6][4], torrent.getDanmuPath());
        values.put(DataBaseInfo.getFieldNames()[6][5], torrent.getEpisodeId());
        values.put(DataBaseInfo.getFieldNames()[6][6], torrent.getMagnet());
        sqLiteDatabase.update(DataBaseInfo.getTableNames()[6], values, "torrent_path = ?", new String[]{torrent.getPath()});
    }

    @Override
    public String getPackageName() {
        if(Log.getStackTraceString(new Throwable()).contains("com.xunlei.downloadlib")) {
            return "com.xunlei.downloadprovider";
        }
        return super.getPackageName();
    }
    @Override
    public PackageManager getPackageManager() {
        return super.getPackageManager();
    }

    @Override
    public void onTerminate() {
        // 程序终止的时候执行
        LogUtils.i("onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        LogUtils.i("onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        LogUtils.i("onTrimMemory_" + level);
        super.onTrimMemory(level);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}