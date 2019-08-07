package com.xyoye.dandanplay.app;

import android.annotation.TargetApi;
import android.app.Application;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.Utils;
import com.player.commom.utils.PlayerConfigShare;
import com.taobao.sophix.SophixManager;
import com.tencent.bugly.Bugly;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.KeyUtil;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.net.okhttp.CookiesManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by xyoye on 2019/5/27.
 */

public class IApplication extends Application {
    //任务列表
    public static List<BtTask> taskList = new ArrayList<>();
    //任务列表
    public static List<String> taskFinishHashList = new ArrayList<>();
    //任务集合，key为hash，value为任务列表中序号
    public static Map<String, Integer> taskMap = new HashMap<>();
    //tracker列表
    public static List<String> trackers = new ArrayList<>();
    //云屏蔽弹幕列表
    public static List<String> cloudFilterList = new ArrayList<>();
    //是否更新用户信息
    public static boolean isUpdateUserInfo = true;
    //是否第一次打开任务管理界面
    public static boolean isFirstOpenTaskPage = true;
    //应用是否正常的启动
    public static boolean startCorrectlyFlag = false;

    public static Handler mainHandler;
    public static ThreadPoolExecutor executor;
    public static Context _context;
    public static Resources _resource;
    public static AssetManager _asset;
    public static CookiesManager cookiesManager;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onCreate() {
        super.onCreate();
        _context = this.getApplicationContext();
        _resource = _context.getResources();
        _asset = _context.getAssets();

        //AndroidUtilsCode
        Utils.init(this);

//        CrashHandleUtils.getInstance().init(() -> {
//            //重启到WelcomeActivity
//            Intent restartIntent = new Intent(_context, MainActivity.class);
//            restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);//必须添加FLAG_ACTIVITY_CLEAR_TASK否则会无线重启
//            startActivity(restartIntent);
//
//            //杀死当前进程
//            android.os.Process.killProcess(android.os.Process.myPid());
//        });

        //Bugly
        Bugly.init(getApplicationContext(), KeyUtil.getBuglyAppId(getApplicationContext()), false);

        //Sophix
        SophixManager.getInstance().setPatchLoadStatusStub(CommonUtils.getPatchLoadListener());

        //数据库
        DataBaseManager.init(this);

        //播放器配置
        PlayerConfigShare.initPlayerConfigShare(this);

        //检查补丁
        if (AppConfig.getInstance().isAutoQueryPatch()){
            SophixManager.getInstance().queryAndLoadNewPatch();
        }

        startCorrectlyFlag = true;
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

    public static Context get_context() {
        return _context;
    }

    public static Resources get_resource() {
        return _resource;
    }

    public static AssetManager get_asset() {
        return _asset;
    }

    public static boolean isDebug() {
        return false;
    }

}