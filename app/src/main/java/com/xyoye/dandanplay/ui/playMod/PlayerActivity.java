package com.xyoye.dandanplay.ui.playMod;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.ijkplayer.danmaku.OnDanmakuListener;
import com.player.ijkplayer.media.IjkPlayerView;
import com.xyoye.dandanplay.event.SaveCurrentEvent;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.player.danmaku.danmaku.model.BaseDanmaku;

/**
 * Created by YE on 2018/7/4 0004.
 */


public class PlayerActivity extends AppCompatActivity {

    com.player.ijkplayer.media.IjkPlayerView mPlayer;
    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private int currentPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        mPlayer = new IjkPlayerView(this);
        setContentView(mPlayer);

        videoPath = getIntent().getStringExtra("path");
        videoTitle = getIntent().getStringExtra("title");
        danmuPath = getIntent().getStringExtra("danmu_path");
        currentPosition = getIntent().getIntExtra("current",0);

        initPlayer();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if(getIntent() != null && getIntent().getData() != null)
        {
            videoPath = getIntent().getData().getPath();
            if (videoPath.contains("/")){
                int titleLocation = videoPath.lastIndexOf("/");
                videoTitle = videoPath.substring(titleLocation, videoPath.length());
            }else {
                videoTitle = "";
            }
            if (videoPath.contains(".")){
                int extLocation = videoPath.lastIndexOf(".");
                String danmuPathTemp = videoPath.substring(0, extLocation) + ".xml";
                File damuFile = new File(danmuPathTemp);
                if (damuFile.exists()){
                    danmuPath = danmuPathTemp;
                }else {
                    danmuPath = "";
                }
            }
            currentPosition = 0;

            initPlayer();
        }else {
            ToastUtils.showShort("解析视频信息失败");
        }
    }

    private void initPlayer(){
        InputStream inputStream = null;
        if (!TextUtils.isEmpty(danmuPath) && FileUtils.isFileExists(danmuPath)) {
            try {
                inputStream = new FileInputStream(new File(danmuPath));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        mPlayer.init().alwaysFullScreen();
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
                        return false;
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {

                    }

                });
        mPlayer.start();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEvent(String text){
        mPlayer.removeBlock(text);
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

    private void saveCurrent(int currentPosition){
        SaveCurrentEvent event = new SaveCurrentEvent();
        event.setCurrentPosition(currentPosition);
        event.setFolderPath(FileUtils.getDirName(videoPath));
        event.setVideoName(videoTitle);
        EventBus.getDefault().post(event);
    }
}
