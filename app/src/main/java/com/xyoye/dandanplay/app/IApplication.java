package com.xyoye.dandanplay.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import com.blankj.utilcode.util.Utils;
import com.xyoye.dandanplay.ui.activities.SplashActivity;
import com.xyoye.dandanplay.ui.activities.personal.CrashActivity;
import com.xyoye.player.commom.utils.PlayerConfigShare;
import com.taobao.sophix.SophixManager;
import com.tencent.bugly.Bugly;
import com.xunlei.downloadlib.XLTaskHelper;
import com.xyoye.dandanplay.utils.database.DataBaseManager;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.SoUtils;
import com.xyoye.dandanplay.utils.net.okhttp.CookiesManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cat.ereza.customactivityoncrash.config.CaocConfig;
import skin.support.SkinCompatManager;
import skin.support.app.SkinCardViewInflater;
import skin.support.constraint.app.SkinConstraintViewInflater;
import skin.support.design.app.SkinMaterialViewInflater;
import skin.support.flycotablayout.app.SkinFlycoTabLayoutInflater;

/**
 * Created by xyoye on 2019/5/27.
 */

public class IApplication extends Application {
    //tracker列表
    public static List<String> trackers = new ArrayList<>();
    //云屏蔽弹幕列表
    public static List<String> cloudFilterList = new ArrayList<>();
    //应用是否正常的启动
    public static boolean startCorrectlyFlag = false;

    public static Handler mainHandler;
    public static ThreadPoolExecutor executor;
    public static Context _context;
    public static AssetManager _asset;
    public static CookiesManager cookiesManager;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onCreate() {
        super.onCreate();
        _context = this.getApplicationContext();
        _asset = _context.getAssets();

        //AndroidUtilsCode
        Utils.init(this);

        //skins
        SkinCompatManager.withoutActivity(this)                         // 基础控件换肤初始化
                .addInflater(new SkinMaterialViewInflater())            // material design 控件换肤初始化[可选]
                .addInflater(new SkinConstraintViewInflater())          // ConstraintLayout 控件换肤初始化[可选]
                .addInflater(new SkinCardViewInflater())                // CardView v7 控件换肤初始化[可选]
                .addInflater(new SkinFlycoTabLayoutInflater())
                .setSkinStatusBarColorEnable(true)                      // 关闭状态栏换肤，默认打开[可选]
                .setSkinWindowBackgroundEnable(true)                    // 关闭windowBackground换肤，默认打开[可选]
                .setSkinAllActivityEnable(true)
                .loadSkin();

        //Crash
        CaocConfig.Builder.create()
                .backgroundMode(CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM)
                .enabled(true)
                .trackActivities(true)
                .minTimeBetweenCrashesMs(2000)
                .restartActivity(SplashActivity.class)
                .errorActivity(CrashActivity.class)
                .apply();

        //Bugly
        Bugly.init(getApplicationContext(), SoUtils.getInstance().getBuglyAppId(), false);

        //Sophix
        SophixManager.getInstance().setPatchLoadStatusStub(CommonUtils.getPatchLoadListener());

        //thunder
        XLTaskHelper.init(this);

        //数据库
        DataBaseManager.init(this);

        //播放器配置
        PlayerConfigShare.initPlayerConfigShare(this);

        //检查补丁
        if (AppConfig.getInstance().isAutoQueryPatch()){
            SophixManager.getInstance().queryAndLoadNewPatch();
        }

        startCorrectlyFlag = true;

        //严格模式
        //strictMode();
    }

    /**
     * 获取主线程的handler
     */
    public static Handler getMainHandler(){
        if (mainHandler == null){
            return new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    /**
     * 应用内通用一个线程池
     */
    public static ThreadPoolExecutor getExecutor() {
        if (executor == null) {
            executor = new ThreadPoolExecutor(3, 10, 200, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(20));
        }
        return executor;
    }

    /**
     * cookie管理器
     */
    public static CookiesManager getCookiesManager() {
        if (cookiesManager == null) {
            cookiesManager = new CookiesManager(get_context());
        }
        return cookiesManager;
    }

    /**
     * 严格模式启动
     */
    private void strictMode(){
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .detectDiskReads()
                .detectDiskWrites()
                .detectNetwork()
                .penaltyLog()
                .build());
        StrictMode.setVmPolicy(new StrictMode.VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build());
    }

    public static Context get_context() {
        return _context;
    }

    public static AssetManager get_asset() {
        return _asset;
    }

    public static boolean isDebug() {
        return false;
    }

}