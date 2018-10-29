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
import android.os.StrictMode;
import android.support.multidex.MultiDex;
import android.util.Base64;
import android.util.Log;

import com.blankj.utilcode.util.LogUtils;
import com.player.ijkplayer.utils.PlayerConfigShare;
import com.tencent.bugly.Bugly;
import com.xyoye.core.BaseApplication;
import com.xyoye.core.db.DataBaseHelper;
import com.xyoye.core.db.DataBaseInfo;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.core.utils.KeyUtil;
import com.xyoye.core.utils.TLog;
import com.xyoye.dandanplay.utils.TorrentStorage;
import com.xyoye.dandanplay.utils.torrent.Torrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import libtorrent.Libtorrent;

public class IApplication extends BaseApplication {
    static ThreadPoolExecutor executor;
    public static List<Torrent> torrentList = new ArrayList<>();
    public static TorrentStorage torrentStorage = new TorrentStorage();

    public static Handler mainHandler;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onCreate() {
        TLog.i("onCreate");
        super.onCreate();
        MultiDex.install(this);
        Bugly.init(getApplicationContext(), KeyUtil.getAppId2(getApplicationContext()), false);
        initDatabase(new DataBaseHelper(this));
        PlayerConfigShare.initPlayerConfigShare(getApplicationContext());
        initLibTorrent();
        loadTorrent();
    }

    private void strictModeConfig() {
        if (DEBUG) {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                    .detectAll()
                    .penaltyDialog()
                    .build());
            StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                    .detectAll()
                    .build());
        }
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
    }

    private void loadTorrent(){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        String sql = "SELECT * FROM "+DataBaseInfo.getTableNames()[6];
        Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{});
        while (cursor.moveToNext()){
            Torrent torrent = new Torrent();
            String path = cursor.getString(1);
            String state = cursor.getString(2);
            byte[] stateByte = Base64.decode(state, Base64.DEFAULT);
            long id = Libtorrent.loadTorrent(path, stateByte);
            if (id == -1) {
                LogUtils.e(Libtorrent.error());
                continue;
            }
            String hash = Libtorrent.torrentHash(id);
            torrent.setDone(false);
            torrent.setId(id);
            torrent.setHash(hash);
            torrent.setPath(path);
            torrent.setTitle(Libtorrent.torrentName(id));
            torrent.setStatus(Libtorrent.torrentStatus(id));
            torrent.setSize(Libtorrent.torrentBytesLength(id));
            if (path.contains("/")){
                int folderS = path.lastIndexOf("/");
                String folder = path.substring(0, folderS);
                if (folder.contains("/torrent"))
                    folder = folder.replace("/torrent", "");
                torrent.setFolder(folder+"/");
            }else {
                torrent.setFolder(path+"/");
            }
            torrentList.add(torrent);
            torrentStorage.addHash(hash, torrent);
        }
        cursor.close();
    }

    public static void saveTorrent(Torrent torrent){
        SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
        byte[] b = Libtorrent.saveTorrent(torrent.getId());
        String state = Base64.encodeToString(b, Base64.DEFAULT);
        ContentValues values=new ContentValues();
        values.put(DataBaseInfo.getFieldNames()[6][1], torrent.getPath());
        values.put(DataBaseInfo.getFieldNames()[6][2], state);
        sqLiteDatabase.insert(DataBaseInfo.getTableNames()[6], null, values);
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
        TLog.i("onTerminate");
        super.onTerminate();
    }

    @Override
    public void onLowMemory() {
        // 低内存的时候执行
        TLog.i("onLowMemory");
        super.onLowMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        // 程序在内存清理的时候执行
        TLog.i("onTrimMemory_" + level);
        super.onTrimMemory(level);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(context);
        MultiDex.install(this);
    }
}