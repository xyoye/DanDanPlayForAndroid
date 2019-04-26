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
import com.blankj.utilcode.util.ToastUtils;
import com.player.danmaku.danmaku.model.BaseDanmaku;
import com.player.exoplayer.ExoPlayerView;
import com.player.exoplayer.PlayerViewListener;
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

public class PlayerActivity extends AppCompatActivity implements PlayerReceiverListener {
    PlayerViewListener mPlayer;

    private boolean isInit = false;
    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private long currentPosition;
    private int episodeId;

    //电源广播
    private BatteryBroadcastReceiver batteryReceiver;
    //锁屏广播
    private ScreenBroadcastReceiver screenReceiver;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);

        //播放器类型
        if (AppConfig.getInstance().getPlayerType() == com.player.ijkplayer.utils.Constants.IJK_EXO_PLAYER){
            mPlayer = new ExoPlayerView(this);
            setContentView((ExoPlayerView)mPlayer);
        }else {
            mPlayer = new IjkPlayerView(this);
            setContentView((IjkPlayerView)mPlayer);
        }

        //注册监听广播
        batteryReceiver = new BatteryBroadcastReceiver(this);
        screenReceiver = new ScreenBroadcastReceiver(this);
        PlayerActivity.this.registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        PlayerActivity.this.registerReceiver(screenReceiver, new IntentFilter(Intent.ACTION_SCREEN_OFF));

        //获取播放参数
        videoPath = getIntent().getStringExtra("video_path");
        videoTitle = getIntent().getStringExtra("video_title");
        danmuPath = getIntent().getStringExtra("danmu_path");
        currentPosition = getIntent().getIntExtra("current_position", 0);
        episodeId = getIntent().getIntExtra("episode_id", 0);

        //初始化播放器
        initPlayer();
    }

    //初始化播放器
    private void initPlayer() {
        //获取弹幕数据流
        InputStream inputStream = null;
        if (!TextUtils.isEmpty(danmuPath) && FileUtils.isFileExists(danmuPath)) {
            try {
                inputStream = new FileInputStream(new File(danmuPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }

        //初始化不同的播放器
        if (AppConfig.getInstance().getPlayerType() == com.player.ijkplayer.utils.Constants.IJK_EXO_PLAYER) {
            initExoPlayer(inputStream);
        }else {
            initIjkPlayer(inputStream);
        }

        isInit = true;
        //添加播放历史
        if (episodeId > 0 && AppConfig.getInstance().isLogin())
            addPlayHistory(episodeId);
    }

    private void initIjkPlayer(InputStream inputStream){
        //ijk播放器配置
        boolean mediaCodeC = AppConfig.getInstance().isOpenMediaCodeC();
        boolean mediaCodeCH265 = AppConfig.getInstance().isOpenMediaCodeCH265();
        boolean openSLES = AppConfig.getInstance().isOpenSLES();
        boolean surfaceRenders = AppConfig.getInstance().isSurfaceRenders();
        int playerType = AppConfig.getInstance().getPlayerType();
        String pixelFormat = AppConfig.getInstance().getPixelFormat();

        IjkPlayerView ijkPlayerView = (IjkPlayerView) mPlayer;

        ijkPlayerView.init()
                //初始化ijk配置
                .initVideoView(mediaCodeC, mediaCodeCH265, openSLES, surfaceRenders, playerType, pixelFormat)
                //总是全屏
                .alwaysFullScreen()
                //开启旋屏
                .enableOrientation()
                //设置云屏蔽数据
                .setCloudFilterData(IApplication.cloudFilterList)
                //设置云屏蔽启用状态
                .setCloudFilterStatus(AppConfig.getInstance().isCloudDanmuFilter())
                //设置视频路径
                .setVideoPath(videoPath)
                //启用弹幕
                .enableDanmaku()
                //设置弹幕数据源
                .setDanmakuSource(inputStream)
                //默认展示弹幕
                .showOrHideDanmaku(true)
                //设置标题
                .setTitle(videoTitle)
                //跳转至上一次播放进度
                .setSkipTip(currentPosition)
                //内部事件回调
                .setOnInfoListener((mp, what, extra) -> {
                    //选择弹幕事件
                    if (what == IjkPlayerView.INTENT_OPEN_SUBTITLE){
                        new FileManagerDialog(
                                PlayerActivity.this,
                                videoPath,
                                FileManagerDialog.SELECT_SUBTITLE,
                                ijkPlayerView::setSubtitlePath
                        ).show();
                    }
                    return true;
                })
                //弹幕事件回调
                .setDanmakuListener(new OnDanmakuListener<BaseDanmaku>() {
                    @Override
                    public boolean isValid() {
                        //是否可发送弹幕
                        return (AppConfig.getInstance().isLogin() && episodeId != 0);
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {
                        //上传弹幕
                        uploadDanmu(data);
                    }

                    @Override
                    public void setCloudFilter(boolean isOpen) {
                        //启用或关闭云屏蔽
                        AppConfig.getInstance().setCloudDanmuFilter(isOpen);
                    }

                });
        //启动播放
        ijkPlayerView.start();
    }

    private void initExoPlayer(InputStream inputStream){
        ExoPlayerView exoPlayerView = (ExoPlayerView)mPlayer;

        exoPlayerView.init()
                //总是全屏
                .alwaysFullScreen()
                //开启旋屏
                .enableOrientation()
                //设置云屏蔽数据
                .setCloudFilterData(IApplication.cloudFilterList)
                //设置云屏蔽启用状态
                .setCloudFilterStatus(AppConfig.getInstance().isCloudDanmuFilter())
                //设置视频路径
                .setVideoPath(videoPath)
                //启用弹幕
                .enableDanmaku()
                //设置弹幕数据源
                .setDanmakuSource(inputStream)
                //默认展示弹幕
                .showOrHideDanmaku(true)
                //设置标题
                .setTitle(videoTitle)
                //内部事件回调
                .setOnInfoListener((mp, what, extra) -> {
                    //选择弹幕事件
                    if (what == IjkPlayerView.INTENT_OPEN_SUBTITLE){
                        new FileManagerDialog(PlayerActivity.this,
                                videoPath,
                                FileManagerDialog.SELECT_SUBTITLE,
                                exoPlayerView::setSubtitlePath
                        ).show();
                    }
                    return true;
                })
                //弹幕事件回调
                .setDanmakuListener(new OnDanmakuListener<BaseDanmaku>() {
                    @Override
                    public boolean isValid() {
                        //是否可发送弹幕
                        return (AppConfig.getInstance().isLogin() && episodeId != 0);
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {
                        //上传弹幕
                        uploadDanmu(data);
                    }

                    @Override
                    public void setCloudFilter(boolean isOpen) {
                        //启用或关闭云屏蔽
                        AppConfig.getInstance().setCloudDanmuFilter(isOpen);
                    }

                });
        //启动播放
        exoPlayerView.start();
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
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        saveCurrent(mPlayer.onDestroy());
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
        event.setCurrentPosition(currentPosition);
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
