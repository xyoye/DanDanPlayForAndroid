package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.danmaku.danmaku.model.BaseDanmaku;
import com.player.ijkplayer.danmaku.OnDanmakuListener;
import com.player.ijkplayer.media.IjkPlayerView;
import com.player.ijkplayer.utils.OpenSubtitleFileEvent;
import com.xyoye.dandanplay.bean.UploadDanmuBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.params.DanmuUploadParam;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuSelectDialog;
import com.xyoye.dandanplay.utils.AppConfig;
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


public class PlayerActivity extends AppCompatActivity {
    private static final int SELECT_SUBTITLE = 101;

    com.player.ijkplayer.media.IjkPlayerView mPlayer;
    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private int currentPosition;
    private int episodeId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mPlayer = new IjkPlayerView(this);
        setContentView(mPlayer);

        if (Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            //外部打开
            if (!StringUtils.isEmpty(getIntent().getDataString())) {
                videoPath = getIntent().getDataString();
                videoTitle = FileUtils.getFileName(videoPath);
                currentPosition = 0;
                episodeId = 0;
                new DanmuSelectDialog(this, type -> {
                    switch (type){
                        case "not":
                            danmuPath = "";
                            initPlayer();
                            mPlayer.start();
                            break;
                        case "network":

                            break;
                        case "local":

                            break;
                    }
                });
            } else {
                ToastUtils.showShort("解析视频地址失败");
                return;
            }
        } else {
            videoPath = getIntent().getStringExtra("path");
            videoTitle = getIntent().getStringExtra("title");
            danmuPath = getIntent().getStringExtra("danmu_path");
            currentPosition = getIntent().getIntExtra("current", 0);
            episodeId = getIntent().getIntExtra("episode_id", 0);
            initPlayer();
            mPlayer.start();
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
        boolean mediaCodeC = AppConfig.getInstance().isOpenMediaCodeC();
        boolean mediaCodeCH265 = AppConfig.getInstance().isOpenMediaCodeCH265();
        boolean openSLES = AppConfig.getInstance().isOpenSLES();
        boolean surfaceRenders = AppConfig.getInstance().isSurfaceRenders();
        int playerType = AppConfig.getInstance().getPlayerType();
        String pixelFormat = AppConfig.getInstance().getPixelFormat();
        mPlayer.init()
                .initVideoView(mediaCodeC, mediaCodeCH265, openSLES, surfaceRenders, playerType, pixelFormat)
                .alwaysFullScreen();
        if (currentPosition > 0) {
            mPlayer.setSkipTip(currentPosition);
        }
        mPlayer.enableOrientation()
                .setVideoPath(videoPath)
                .setMediaQuality(IjkPlayerView.MEDIA_QUALITY_HIGH)
                .enableDanmaku()
                .setDanmakuSource(inputStream)
                .showOrHideDanmaku(true)
                .setTitle(videoTitle)
                .setQualityButtonVisibility(false)
                .setDanmakuListener(new OnDanmakuListener<BaseDanmaku>() {
                    @Override
                    public boolean isValid() {
                        return (AppConfig.getInstance().isLogin() && episodeId != 0);
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {
                        uploadDanmu(data);
                    }

                });
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
                Log.i("DanmuUpload", "onSuccess: text：" + data.text + "  cid：" + bean.getCid());
            }

            @Override
            public void onError(int errorCode, String message) {
                ToastUtils.showShort(message);
            }
        }, new NetworkConsumer());
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String text) {
        mPlayer.removeBlock(text);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(OpenSubtitleFileEvent event) {
        Intent intent = new Intent(this, FileManagerActivity.class);
        intent.putExtra("file_type", FileManagerActivity.FILE_SUBTITLE);
        startActivityForResult(intent, SELECT_SUBTITLE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            if (requestCode == SELECT_SUBTITLE) {
                String subtitlePath = data.getStringExtra("subtitle");
                mPlayer.setSubtitleSource("", subtitlePath);
            }
        }
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        saveCurrent(mPlayer.getCurPosition());
        mPlayer.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        mPlayer.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
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

    //保存进度
    private void saveCurrent(int currentPosition) {
        SaveCurrentEvent event = new SaveCurrentEvent();
        event.setCurrentPosition(currentPosition);
        event.setFolderPath(FileUtils.getDirName(videoPath));
        event.setVideoPath(videoPath);
        EventBus.getDefault().post(event);
    }
}
