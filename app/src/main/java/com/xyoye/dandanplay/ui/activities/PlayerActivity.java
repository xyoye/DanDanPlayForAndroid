package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
import com.xyoye.core.db.DataBaseManager;
import com.xyoye.dandanplay.bean.UploadDanmuBean;
import com.xyoye.dandanplay.bean.event.SaveCurrentEvent;
import com.xyoye.dandanplay.bean.params.DanmuUploadParam;
import com.xyoye.dandanplay.utils.AppConfigShare;
import com.xyoye.dandanplay.utils.UserInfoShare;
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
    private int episodeId = 0;

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
            currentPosition = getIntent().getIntExtra("current", 0);
            episodeId = getIntent().getIntExtra("episode_id", 0);
        } else {
            //外部打开
            if (getIntent().getDataString() == null || "".equals(getIntent().getDataString())) {
                videoPath = "";
            } else {
                videoPath = getIntent().getDataString();
            }
            //获取标题
            if (videoPath != null && videoPath.contains("/")) {
                int titleLocation = videoPath.lastIndexOf("/") + 1;
                if (titleLocation < videoPath.length())
                    videoTitle = videoPath.substring(titleLocation, videoPath.length());
                else
                    videoTitle = "";
            } else {
                videoTitle = "";
                videoPath = "";

            }
            //获取弹幕：先从数据库根据path拿，未匹配到则从相同目录下拿
            if (videoPath.contains(".") && !StringUtils.isEmpty(videoTitle)) {
                String danmuPathTemp;
                boolean isGetDanmuPath = false;
                String folderPath = FileUtils.getDirName(videoPath);
                SQLiteDatabase sqLiteDatabase = DataBaseManager.getInstance().getSQLiteDatabase();
                String sql = "SELECT danmu_path FROM file WHERE folder_path=? AND file_name=?";
                Cursor cursor = sqLiteDatabase.rawQuery(sql, new String[]{folderPath, videoTitle});
                if (cursor != null && cursor.getCount() != 0) {
                    cursor.moveToNext();
                    danmuPathTemp = cursor.getString(0);
                    episodeId = cursor.getInt(6);
                    cursor.close();
                    if (!StringUtils.isEmpty(danmuPathTemp)) {
                        File damuFile = new File(danmuPathTemp);
                        if (damuFile.exists()) {
                            danmuPath = danmuPathTemp;
                            isGetDanmuPath = true;
                        }
                    }
                }
                if (!isGetDanmuPath) {
                    int extLocation = videoPath.lastIndexOf(".");
                    danmuPathTemp = videoPath.substring(0, extLocation) + ".xml";
                    File damuFile = new File(danmuPathTemp);
                    if (damuFile.exists()) {
                        danmuPath = danmuPathTemp;
                    } else {
                        danmuPath = "";
                    }
                }
            }
            //上次播放默认为0
            currentPosition = 0;

            if (videoPath == null || "".equals(videoPath)) {
                ToastUtils.showShort("解析视频地址失败");
                return;
            }
        }
        initPlayer();
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
                        return (UserInfoShare.getInstance().isLogin() && episodeId != 0);
                    }

                    @Override
                    public void onDataObtain(BaseDanmaku data) {
                        uploadDanmu(data);
                    }

                });
        mPlayer.start();
    }

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

    private void saveCurrent(int currentPosition) {
        SaveCurrentEvent event = new SaveCurrentEvent();
        event.setCurrentPosition(currentPosition);
        event.setFolderPath(FileUtils.getDirName(videoPath));
        event.setVideoName(videoTitle);
        EventBus.getDefault().post(event);
    }
}
