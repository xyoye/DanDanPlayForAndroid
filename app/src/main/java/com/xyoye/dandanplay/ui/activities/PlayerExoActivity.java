package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.LogUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.danmaku.danmaku.model.BaseDanmaku;
import com.player.exoplayer.ExoPlayerView;
import com.player.ijkplayer.danmaku.OnDanmakuListener;
import com.player.ijkplayer.media.IjkPlayerView;
import com.player.ijkplayer.receiver.BatteryBroadcastReceiver;
import com.player.ijkplayer.receiver.PlayerReceiverListener;
import com.player.ijkplayer.receiver.ScreenBroadcastReceiver;
import com.xyoye.dandanplay.app.IApplication;
import com.xyoye.dandanplay.bean.PlayHistoryBean;
import com.xyoye.dandanplay.bean.UploadDanmuBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.params.DanmuUploadParam;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuSelectDialog;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.net.CommJsonEntity;
import com.xyoye.dandanplay.utils.net.CommJsonObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.math.BigDecimal;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class PlayerExoActivity extends AppCompatActivity implements PlayerReceiverListener {
    private static final int SELECT_DANMU = 102;

    ExoPlayerView mPlayer;
    private boolean isInit = false;
    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private int currentPosition;
    private int episodeId;

    private BatteryBroadcastReceiver batteryReceiver;
    private ScreenBroadcastReceiver screenReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mPlayer = new ExoPlayerView(this);
        setContentView(mPlayer);

        batteryReceiver = new BatteryBroadcastReceiver(this);
        screenReceiver = new ScreenBroadcastReceiver(this);
        PlayerExoActivity.this.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        PlayerExoActivity.this.registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        boolean isSmbPlay = getIntent().getBooleanExtra("extra_smb", false);
        if (isSmbPlay){
            videoPath = getIntent().getStringExtra("path");
            videoTitle = getIntent().getStringExtra("title");
            currentPosition = 0;
            episodeId = 0;
            if (AppConfig.getInstance().isShowOuterChainDanmuDialog()){
                new DanmuSelectDialog(this, isSelectDanmu -> {
                    if (isSelectDanmu){
                        Intent intent = new Intent(PlayerExoActivity.this, DanmuNetworkActivity.class);
                        intent.putExtra("video_path", videoPath);
                        intent.putExtra("is_lan", true);
                        startActivityForResult(intent, SELECT_DANMU);
                    }else {
                        danmuPath = "";
                        initPlayer();
                        mPlayer.start();
                    }
                }).show();
            }else {
                if (AppConfig.getInstance().isOuterChainDanmuSelect()){
                    Intent intent = new Intent(PlayerExoActivity.this, DanmuNetworkActivity.class);
                    intent.putExtra("video_path", videoPath);
                    intent.putExtra("is_lan", true);
                    startActivityForResult(intent, SELECT_DANMU);
                }else {
                    danmuPath = "";
                    initPlayer();
                    mPlayer.start();
                }
            }
            return;
        }

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            //外部打开
            if (!StringUtils.isEmpty(getIntent().getDataString())) {
                videoPath = getIntent().getDataString();
                videoTitle = FileUtils.getFileName(videoPath);
                currentPosition = 0;
                episodeId = 0;
                if (AppConfig.getInstance().isShowOuterChainDanmuDialog()){
                    new DanmuSelectDialog(this, isSelectDanmu -> {
                        if (isSelectDanmu){
                            Intent intent = new Intent(PlayerExoActivity.this, DanmuNetworkActivity.class);
                            intent.putExtra("video_path", videoPath);
                            intent.putExtra("is_lan", true);
                            startActivityForResult(intent, SELECT_DANMU);
                        }else {
                            danmuPath = "";
                            initPlayer();
                            mPlayer.start();
                        }
                    }).show();
                }else {
                    if (AppConfig.getInstance().isOuterChainDanmuSelect()){
                        Intent intent = new Intent(PlayerExoActivity.this, DanmuNetworkActivity.class);
                        intent.putExtra("video_path", videoPath);
                        intent.putExtra("is_lan", true);
                        startActivityForResult(intent, SELECT_DANMU);
                    }else {
                        danmuPath = "";
                        initPlayer();
                        mPlayer.start();
                    }
                }
            } else {
                ToastUtils.showShort("解析视频地址失败");
            }
        } else {
            videoPath = getIntent().getStringExtra("path");
            videoTitle = getIntent().getStringExtra("title");
            danmuPath = getIntent().getStringExtra("danmu_path");
            currentPosition = getIntent().getIntExtra("current", 0);
            episodeId = getIntent().getIntExtra("episode_id", 0);
            initPlayer();
            mPlayer.start();
            if (episodeId != 0 && episodeId != -1 && AppConfig.getInstance().isLogin())
                addPlayHistory(episodeId);
        }
    }

    private void initPlayer() {
        InputStream inputStream = null;
        if (!TextUtils.isEmpty(danmuPath) && FileUtils.isFileExists(danmuPath)) {
            try {
                inputStream = new FileInputStream(new File(danmuPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //播放器配置

        if (currentPosition > 0) {
            mPlayer.setSkipTip(currentPosition);
        }
        mPlayer.init()
                .enableOrientation()
                .alwaysFullScreen()
                .setCloudFilterData(IApplication.cloudFilterList)
                .setCloudFilterStatus(AppConfig.getInstance().isCloudDanmuFilter())
                .setVideoPath(videoPath)
                .enableDanmaku()
                .setDanmakuSource(inputStream)
                .showOrHideDanmaku(true)
                .setTitle(videoTitle)
                .setOnInfoListener((mp, what, extra) -> {
                    if (what == IjkPlayerView.INTENT_OPEN_SUBTITLE){
                        new FileManagerDialog(PlayerExoActivity.this,
                                videoPath,
                                FileManagerDialog.SELECT_SUBTITLE,
                                path -> mPlayer.setSubtitlePath(path)
                        ).show();
                    }
                    return true;
                })
                .setDanmakuListener(new OnDanmakuListener<BaseDanmaku>() {
                    @Override
                    public boolean isValid() {
                        return (AppConfig.getInstance().isLogin() && episodeId != 0);
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {
                        uploadDanmu(data);
                    }

                    @Override
                    public void setCloudFilter(boolean isOpen) {
                        AppConfig.getInstance().setCloudDanmuFilter(isOpen);
                    }

                });
        isInit = true;
    }

    //上传一条弹幕
    private void uploadDanmu(BaseDanmaku data) {
        double dTime = new BigDecimal(data.getTime() / 1000)
                .setScale(3, BigDecimal.ROUND_HALF_UP)
                .doubleValue();
        String time = dTime + "";
        int type = data.getType();
        if (type != 1 && type != 4 && type != 5) {
            type = 1;
        }
        String mode = type + "";
        String color = (data.textColor & 0x00FFFFFF) + "";
        String comment = String.valueOf(data.text);
        DanmuUploadParam uploadParam = new DanmuUploadParam(time, mode, color, comment);
        UploadDanmuBean.uploadDanmu(uploadParam, episodeId + "", new CommJsonObserver<UploadDanmuBean>() {
            @Override
            public void onSuccess(UploadDanmuBean bean) {
                LogUtils.d("upload danmu success: text：" + data.text + "  cid：" + bean.getCid());
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    //增加播放历史
    private void addPlayHistory(int episodeId){
        if (episodeId > 0){
            PlayHistoryBean.addPlayHistory(episodeId, new CommJsonObserver<CommJsonEntity>() {
                @Override
                public void onSuccess(CommJsonEntity commJsonEntity) {
                    LogUtils.d("add history success: episodeId：" + episodeId);
                }

                @Override
                public void onError(int errorCode, String message) {
                    LogUtils.e("add history fail: episodeId：" + episodeId+"  message："+message);
                }
            }, new NetworkConsumer());
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String text) {
        mPlayer.removeBlock(text);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_DANMU){
                danmuPath = data.getStringExtra("path");
                episodeId = data.getIntExtra("episode_id", 0);
                initPlayer();
                mPlayer.start();
                if (episodeId != 0 && episodeId != -1 && AppConfig.getInstance().isLogin())
                    addPlayHistory(episodeId);
            }
        }else if (resultCode == RESULT_CANCELED){
            danmuPath = "";
            episodeId = 0;
            initPlayer();
            mPlayer.start();
            if (episodeId != 0 && episodeId != -1 && AppConfig.getInstance().isLogin())
                addPlayHistory(episodeId);
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        saveCurrent(mPlayer.getCurPosition());
        mPlayer.onDestroy();
        this.unregisterReceiver(batteryReceiver);
        this.unregisterReceiver(screenReceiver);
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (isInit)
            mPlayer.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (isInit)
            mPlayer.onPause();
        super.onPause();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        return mPlayer.handleVolumeKey(keyCode) || super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mPlayer.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mPlayer.configurationChanged(newConfig);
    }

    //保存进度
    private void saveCurrent(long currentPosition) {
        SaveCurrentEvent event = new SaveCurrentEvent();
        event.setCurrentPosition((int)currentPosition);
        event.setFolderPath(FileUtils.getDirName(videoPath));
        event.setVideoPath(videoPath);
        EventBus.getDefault().post(event);
    }

    @Override
    public void onBatteryChanged(int status, int progress) {
        mPlayer.setBatteryChanged(status, progress);
    }

    @Override
    public void onScreenLocked() {
        mPlayer.onScreenLocked();
    }
}
