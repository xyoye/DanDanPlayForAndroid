package com.xyoye.dandanplay.app;

import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.player.commom.utils.ContextUtil;
import com.player.commom.utils.PlayerConfigShare;
import com.taobao.sophix.PatchStatus;
import com.taobao.sophix.SophixManager;
import com.taobao.sophix.listener.PatchLoadStatusListener;
import com.tencent.bugly.Bugly;
import com.xyoye.dandanplay.bean.event.PatchFixEvent;
import com.xyoye.dandanplay.database.DataBaseInfo;
import com.xyoye.dandanplay.database.DataBaseManager;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.JsonUtil;
import com.xyoye.dandanplay.utils.KeyUtil;
import com.xyoye.dandanplay.utils.jlibtorrent.BtTask;
import com.xyoye.dandanplay.utils.jlibtorrent.Torrent;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import me.yokeyword.fragmentation.BuildConfig;
import me.yokeyword.fragmentation.Fragmentation;

/**
 * Created by xyoye on 2019/5/27.
 */

public class IApplication extends BaseApplication {
    public static List<BtTask> taskList = new ArrayList<>();
    public static Map<String, Integer> taskMap = new HashMap<>();
    public static List<String> trackers = new ArrayList<>();
    public static List<String> cloudFilterList = new ArrayList<>();
    public static List<String> normalFilterList = new ArrayList<>();
    public static boolean isUpdateUserInfo = true;
    //是否第一次打开任务管理界面
    public static boolean isFirstOpenTaskPage = true;

    public static Handler mainHandler;

    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    @Override
    public void onCreate() {
        super.onCreate();

        //Bugly
        Bugly.init(getApplicationContext(), KeyUtil.getBuglyAppId(getApplicationContext()), false);

        //Sophix
        SophixManager.getInstance().setPatchLoadStatusStub(getPatchLoadListener());

        //数据库
        DataBaseManager.init(this);

        //播放器配置
        PlayerConfigShare.initPlayerConfigShare(this);

        new Thread(() -> {
            //首次打开App
            if (AppConfig.getInstance().isFirstStart()) {
                //扫描文件夹
                SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                ContentValues values = new ContentValues();
                values.put(DataBaseInfo.getFieldNames()[11][1], AppConfig.getInstance().getDownloadFolder());
                sqLiteDatabase.insert(DataBaseInfo.getTableNames()[11], null, values);
            }
        }).start();

        //Fragmentation
        Fragmentation.builder()
                .stackViewMode(Fragmentation.NONE)
                .install();

        //检查补丁
        if (AppConfig.getInstance().isAutoQueryPatch()){
            SophixManager.getInstance().queryAndLoadNewPatch();
        }

        //Context 工具
        ContextUtil.getInstans().initContext(this.getApplicationContext());
    }

    public static Handler getMainHandler(){
        if (mainHandler == null){
            return new Handler(Looper.getMainLooper());
        }
        return mainHandler;
    }

    public PatchLoadStatusListener getPatchLoadListener(){
        return (mode, code, serviceMsg, version) -> {
            String msg = "";
            List<PatchFixEvent> eventList = JsonUtil.getObjectList(SPUtils.getInstance().getString("patch_his"), PatchFixEvent.class);
            if (eventList == null) eventList = new ArrayList<>();
            AppConfig.getInstance().setPatchVersion(version);
            PatchFixEvent event = new PatchFixEvent();
            event.setVersion(version);
            event.setTime(TimeUtils.date2String(new java.util.Date()));
            switch (code){
                //加载阶段, 成功
                case PatchStatus.CODE_LOAD_SUCCESS:
                    msg = "加载补丁成功";
                    break;
                //加载阶段, 失败设备不支持
                case PatchStatus.CODE_ERR_INBLACKLIST:
                    msg = "加载失败，设备不支持";
                    break;
                //查询阶段, 没有发布新补丁
                case PatchStatus.CODE_REQ_NOUPDATE:
                    msg = "已是最新补丁";
                    event.setCode(-2);
                    break;
                //查询阶段, 补丁不是最新的
                case PatchStatus.CODE_REQ_NOTNEWEST:
                    msg = "开始下载补丁";
                    break;
                //查询阶段, 补丁下载成功
                case PatchStatus.CODE_DOWNLOAD_SUCCESS:
                    msg = "补丁下载成功";
                    break;
                //查询阶段, 补丁文件损坏下载失败
                case PatchStatus.CODE_DOWNLOAD_BROKEN:
                    msg = "补丁文件损坏下载失败";
                    break;
                //查询阶段, 补丁解密失败
                case PatchStatus.CODE_UNZIP_FAIL:
                    msg = "补丁解密失败";
                    break;
                //预加载阶段, 需要重启
                case PatchStatus.CODE_LOAD_RELAUNCH:
                    msg = "加载成功，重启应用后生效";
                    break;
                //查询阶段, appid异常
                case PatchStatus.CODE_REQ_APPIDERR:
                    msg = "加载失败，appid异常";
                    break;
                //查询阶段, 签名异常
                case PatchStatus.CODE_REQ_SIGNERR:
                    msg = "加载失败，签名异常";
                    break;
                //查询阶段, 系统无效
                case PatchStatus.CODE_REQ_UNAVAIABLE:
                    msg = "加载失败，系统无效";
                    break;
                //查询阶段, 系统异常
                case PatchStatus.CODE_REQ_SYSTEMERR:
                    msg = "加载失败，系统异常";
                    break;
                //查询阶段, 一键清除补丁
                case PatchStatus.CODE_REQ_CLEARPATCH:
                    msg = "一键清除成功";
                    break;
                //加载阶段, 补丁格式非法
                case PatchStatus.CODE_PATCH_INVAILD:
                    msg = "加载失败，补丁格式非法";
                    break;
                default:
                    event.setCode(code);
                    break;
            }
            LogUtils.e(msg);
            event.setMsg(msg);
            eventList.add(event);
            EventBus.getDefault().post(event);
            SPUtils.getInstance().put("patch_his", JsonUtil.toJson(eventList));
        };
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
    }
}