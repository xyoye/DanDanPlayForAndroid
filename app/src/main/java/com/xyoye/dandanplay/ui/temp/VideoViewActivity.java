package com.xyoye.dandanplay.ui.temp;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.blankj.utilcode.util.FileUtils;
import com.dl7.player.danmaku.OnDanmakuListener;
import com.dl7.player.media.IjkPlayerView;
import com.xyoye.core.utils.TLog;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import master.flame.danmaku.danmaku.model.BaseDanmaku;

public class VideoViewActivity extends AppCompatActivity implements VideoViewContract.View {
    VideoViewPresenter mVideoViewPresenter;
    com.dl7.player.media.IjkPlayerView mViewSuperPlayer;
    private String videoPath, videoTitle, file_title;
    private boolean hide_danmu = false, isOffLine = false;
    private int episode_id, currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mViewSuperPlayer = new IjkPlayerView(this);
        setContentView(mViewSuperPlayer);
        initData();
        initPlayer();
        initView();

    }


    @Override
    public String getVideoPath() {
        return videoPath;
    }


    private void initData() {
        mVideoViewPresenter = new VideoViewPresenter(this);
        videoPath = getIntent().getStringExtra("path");
        file_title = getIntent().getStringExtra("file_title");
        videoTitle = getIntent().getStringExtra("title");
        hide_danmu = getIntent().getBooleanExtra("hide_danmu", false);
        episode_id = getIntent().getIntExtra("episode_id", -1);
//        //读取播放进度
//        Realm realm = MyApplication.getRealmInstance();
//        VideoFileArgInfo videoFileArgInfo = realm.where(VideoFileArgInfo.class).equalTo("videoPath", videoPath).findFirst();
//        if (videoFileArgInfo!=null){
//            currentPosition = videoFileArgInfo.getSawProgress();
//        }
    }

    private void initView() {
        if (!TextUtils.isEmpty(videoTitle)) {
            mViewSuperPlayer.setTitle(videoTitle);
        } else {
            mViewSuperPlayer.setTitle(file_title);
        }
    }

    private void initPlayer() {
        String danmuxml = "";
        InputStream inputStream = null;
        if (!TextUtils.isEmpty(videoPath)) {
            String ddxml = videoPath.substring(0, videoPath.lastIndexOf(".")) + "dd.xml";
            String bilixml = videoPath.substring(0, videoPath.lastIndexOf(".")) + ".xml";
            if (FileUtils.isFileExists(ddxml)) {
                danmuxml = ddxml;
            } else if (FileUtils.isFileExists(bilixml)) {
                danmuxml = bilixml;
            }
        }

        if (!TextUtils.isEmpty(danmuxml) && FileUtils.isFileExists(danmuxml)) {
            try {
                inputStream = new FileInputStream(new File(danmuxml));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        try {
            //缩略图
//            FFmpegMediaMetadataRetriever fmmr = new FFmpegMediaMetadataRetriever();
//            fmmr.setDataSource(videoPath);
//            Bitmap bitmap = fmmr.getFrameAtTime();
//            String duration=fmmr.extractMetadata(FFmpegMediaMetadataRetriever.METADATA_KEY_DURATION);
//            mViewSuperPlayer.mPlayerThumb.setImageBitmap(bitmap);
//            fmmr.release();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            TLog.e("获取缩略图失败");
        }
        //        Glide.with(this).load(IMAGE_URL).fitCenter().into(mPlayerView.mPlayerThumb); // Show the thumb before play
        mViewSuperPlayer.init()              // Initialize, the first to use
                .alwaysFullScreen();//始终全屏
        if (currentPosition > 0) {
            mViewSuperPlayer.setSkipTip(currentPosition);
        }
//                .setSkipTip(1000 * 60 * 0)  // set the position you want to skip
        mViewSuperPlayer.enableOrientation()    // enable orientation
                .setVideoPath(videoPath)    // set video url
//                .setVideoSource(null, VIDEO_URL, VIDEO_URL, VIDEO_URL, null) // set multiple video url
//                .setMediaQuality(IjkPlayerView.MEDIA_QUALITY_HIGH)  // set the initial video url
                .enableDanmaku()        // enable Danmaku
                .setDanmakuSource(inputStream)
                .showOrHideDanmaku(!hide_danmu)
                .setTitle(file_title)      // set title
                .setQualityButtonVisibility(false)//需要在全屏后使用生效
                .setDanmakuListener(new OnDanmakuListener<BaseDanmaku>() {
                    @Override
                    public boolean isValid() {
                        if (episode_id > -1) {
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {
                        int color = -1;
                        switch (data.textColor) {
                            case -1://白色
                                color = 16777215;
                                break;
                            case -1638382://红色
                                color = 15138834;
                                break;
                            case -69374://浅黄色
                                color = 16707842;
                                break;
                            case -16738237://绿色
                                color = 38979;
                                break;
                            case -16736022://浅蓝色
                                color = 41194;
                                break;
                            case -1965441://粉红
                                color = 14811775;
                                break;
                            case -7290080://青色
                                color = 9487136;
                                break;
                            case -16765326://深蓝色
                                color = 11890;
                                break;
                            case -1004758://棕黄色
                                color = 15772458;
                                break;
                            case -9946501://紫色
                                color = 6830715;
                                break;
                            case -8077109://青灰色
                                color = 8700107;
                                break;
                            case -7177927://灰黄色
                                color = 9599289;
                                break;
                            default:
                                color = 16777215;
                                break;
                        }
                        if (episode_id > -1) {
                            mVideoViewPresenter.sendDanmu(episode_id, data.getTime(), data.getType(), color, data.text.toString());
                        } else {
                            TLog.e("未识别到有效的视频ID,无法发送弹幕到服务器,请检查是否是以在线模式加载");
                        }
                    }

                });
//        if (MyApplication.getSP().getBoolean("auto_play",true)) {
//            mViewSuperPlayer.start();
//        }
        mViewSuperPlayer.start();

    }


    @Override
    public Context getContext() {
        return this;
    }


    @Override
    public void error(String msg) {
        TLog.e("msg");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewSuperPlayer.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //保存播放进度
//        Realm realm = MyApplication.getRealmInstance();
//        VideoFileArgInfo videoFileArgInfo = realm.where(VideoFileArgInfo.class).equalTo("videoPath", videoPath).findFirst();
//        realm.beginTransaction();
//        if (videoFileArgInfo != null) {
//            videoFileArgInfo.setSawProgress(mViewSuperPlayer.getCurPosition());
//        }
//        realm.commitTransaction();
        mViewSuperPlayer.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mViewSuperPlayer.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (mViewSuperPlayer.handleVolumeKey(keyCode)) {
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onBackPressed() {
        if (mViewSuperPlayer.onBackPressed()) {
            return;
        }
        super.onBackPressed();
    }
}
