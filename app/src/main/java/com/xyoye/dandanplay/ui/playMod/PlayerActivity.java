package com.xyoye.dandanplay.ui.playMod;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.player.danmaku.danmaku.model.BaseDanmaku;
import com.player.ijkplayer.danmaku.OnDanmakuListener;
import com.player.ijkplayer.media.IjkPlayerView;
import com.player.ijkplayer.utils.Constants;
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.dandanplay.event.SaveCurrentEvent;
import com.xyoye.dandanplay.utils.AppConfigShare;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

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

        if (!Intent.ACTION_VIEW.equals(getIntent().getAction())) {
            videoPath = getIntent().getStringExtra("path");
            videoTitle = getIntent().getStringExtra("title");
            danmuPath = getIntent().getStringExtra("danmu_path");
            currentPosition = getIntent().getIntExtra("current",0);
        }else {
            //外部打开
            //从uri.getPath否则path被转译了
            if (getIntent().getDataString() == null || "".equals(getIntent().getDataString())){
                videoPath = "";
            }else {
                Uri uri = Uri.parse(getIntent().getDataString());
                videoPath = uri.getPath();
            }
            //获取标题
            if (videoPath != null && videoPath.contains("/")){
                int titleLocation = videoPath.lastIndexOf("/") + 1;
                if (titleLocation < videoPath.length())
                    videoTitle = videoPath.substring(titleLocation, videoPath.length());
                else
                    videoTitle = "";
            }else {
                videoTitle = "";
                videoPath = "";

            }
            //获取弹幕：先从数据库根据path拿，未匹配到则从相同目录下拿
            if (videoPath.contains(".") && !StringUtils.isEmpty(videoTitle)){
                String danmuPathTemp;
                boolean isGetDanmuPath = false;
                String folderPath = FileUtils.getDirName(videoPath);
                SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                String sql = "SELECT danmu_path FROM file WHERE folder_path=? AND file_name=?";
                Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath, videoTitle});
                if (cursor.getCount() != 0){
                    cursor.moveToNext();
                    danmuPathTemp = cursor.getString(0);
                    cursor.close();
                    if (!StringUtils.isEmpty(danmuPathTemp)){
                        File damuFile = new File(danmuPathTemp);
                        if (damuFile.exists()){
                            danmuPath = danmuPathTemp;
                            isGetDanmuPath = true;
                        }
                    }
                }
                if (!isGetDanmuPath){
                    int extLocation = videoPath.lastIndexOf(".");
                    danmuPathTemp = videoPath.substring(0, extLocation) + ".xml";
                    File damuFile = new File(danmuPathTemp);
                    if (damuFile.exists()){
                        danmuPath = danmuPathTemp;
                    }else {
                        danmuPath = "";
                    }
                }
            }
            //上次播放默认为0
            currentPosition = 0;

            if ("".equals(videoPath)){
                ToastUtils.showShort("解析视频地址失败");
                return;
            }
        }
        initPlayer();
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

        boolean mediaCodeC = AppConfigShare.getInstance().isOpenMediaCodeC();
        boolean mediaCodeCH265 = AppConfigShare.getInstance().isOpenMediaCodeCH265();
        boolean openSLES = AppConfigShare.getInstance().isOpenSLES();
        boolean surfaceRenders = AppConfigShare.getInstance().isSurfaceRenders();
        int playerType = AppConfigShare.getInstance().getPlayerType();
        String pixelFormat = AppConfigShare.getInstance().getPixelFormat();
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
