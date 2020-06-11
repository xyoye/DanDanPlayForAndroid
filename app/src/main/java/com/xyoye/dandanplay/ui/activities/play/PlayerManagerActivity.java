package com.xyoye.dandanplay.ui.activities.play;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.bean.BindResourceBean;
import com.xyoye.dandanplay.bean.params.BindResourceParam;
import com.xyoye.dandanplay.bean.params.PlayParam;
import com.xyoye.dandanplay.ui.weight.dialog.DanmuSelectDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.database.DataBaseManager;

import java.io.File;

import butterknife.BindView;

/**
 * Created by xyoye on 2019/4/26.
 */

public class PlayerManagerActivity extends BaseMvcActivity {
    private static final int SELECT_DANMU = 102;

    //视频来源-外部
    public static final int SOURCE_ORIGIN_OUTSIDE = 1000;
    //视频来源-本地文件
    public static final int SOURCE_ORIGIN_LOCAL = 1001;
    //视频来源-串流
    public static final int SOURCE_ORIGIN_STREAM = 1002;
    //视频来源-局域网
    public static final int SOURCE_ORIGIN_SMB = 1003;
    //视频来源-远程连接
    public static final int SOURCE_ORIGIN_REMOTE = 1004;
    //视频来源-在线播放
    public static final int SOURCE_ONLINE_PREVIEW = 1005;

    private String videoPath;
    private String videoTitle;
    private String danmuPath;
    private String zimuPath;
    private long currentPosition;
    private int episodeId;
    private long thunderTaskId;
    private int sourceOrigin;
    private String searchWord;

    @BindView(R.id.error_tv)
    TextView errorTv;
    @BindView(R.id.back_iv)
    ImageView backIv;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_player_manager;
    }

    @Override
    public void initPageView() {

    }

    @Override
    public void initPageViewListener() {
        View decorView = this.getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        errorTv.setVisibility(View.GONE);
        backIv.setOnClickListener(v -> PlayerManagerActivity.this.finish());

        initIntent();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SELECT_DANMU) {
            if (resultCode == RESULT_OK) {
                BindResourceBean bindResourceBean = data.getParcelableExtra("bind_data");
                if (bindResourceBean != null) {
                    danmuPath = bindResourceBean.getDanmuPath();
                    episodeId = bindResourceBean.getEpisodeId();
                }
            }
            if (TextUtils.isEmpty(videoPath)) {
                ToastUtils.showShort("解析视频地址失败");
                errorTv.setVisibility(View.VISIBLE);
            } else {
                launchPlayerActivity();
            }
        }
    }

    private void initIntent() {
        Intent openIntent = getIntent();
        videoTitle = openIntent.getStringExtra("video_title");
        videoPath = openIntent.getStringExtra("video_path");
        danmuPath = openIntent.getStringExtra("danmu_path");
        zimuPath = openIntent.getStringExtra("zimu_path");
        currentPosition = openIntent.getLongExtra("current_position", 0L);
        episodeId = openIntent.getIntExtra("episode_id", 0);
        thunderTaskId = openIntent.getLongExtra("thunder_task_id", -1L);
        searchWord = openIntent.getStringExtra("search_word");

        sourceOrigin = openIntent.getIntExtra("source_origin", SOURCE_ORIGIN_LOCAL);

        //本地文件播放可预先绑定弹幕，其它播放来源都应弹窗询问是否选择弹幕
        boolean showSelectDanmuDialog = AppConfig.getInstance().isShowOuterChainDanmuDialog();
        boolean autoLaunchDanmuPage = AppConfig.getInstance().isOuterChainDanmuSelect();

        //外部打开
        if (Intent.ACTION_VIEW.equals(openIntent.getAction())) {
            sourceOrigin = SOURCE_ORIGIN_OUTSIDE;
            //获取视频地址
            Uri data = getIntent().getData();
            if (data != null) {
                videoPath = CommonUtils.getRealFilePath(PlayerManagerActivity.this, data);
            }
        }

        //检查视频地址
        if (TextUtils.isEmpty(videoPath)) {
            ToastUtils.showShort("解析视频地址失败");
            errorTv.setVisibility(View.VISIBLE);
            return;
        }

        //检查弹幕地址
        if (!TextUtils.isEmpty(danmuPath) && danmuPath.toLowerCase().endsWith(".xml")) {
            File danmuFile = new File(danmuPath);
            if (!danmuFile.exists() || !danmuFile.isFile()) {
                danmuPath = "";
            }
        }

        //检查字幕地址
        if (!TextUtils.isEmpty(zimuPath)) {
            File zimuFile = new File(zimuPath);
            if (!zimuFile.exists() || !zimuFile.isFile()) {
                zimuPath = "";
            }
        }

        //检查视频标题
        videoTitle = TextUtils.isEmpty(videoTitle) ? FileUtils.getFileName(videoPath) : videoTitle;
        searchWord = TextUtils.isEmpty(searchWord) ? FileUtils.getFileNameNoExtension(videoPath) : searchWord;

        //选择弹幕弹窗及跳转
        if (sourceOrigin != SOURCE_ORIGIN_LOCAL) {
            if (showSelectDanmuDialog) {
                new DanmuSelectDialog(this, isSelectDanmu -> {
                    if (isSelectDanmu) {
                        launchDanmuSelect(searchWord);
                    } else {
                        launchPlayerActivity();
                    }
                }).show();
                return;
            }
            if (autoLaunchDanmuPage) {
                launchDanmuSelect(searchWord);
                return;
            }
        }
        launchPlayerActivity();
    }

    /**
     * 跳转至选择弹幕页面
     */
    private void launchDanmuSelect(String searchWord) {
        BindResourceParam param = new BindResourceParam(searchWord, true);
        Intent intent = new Intent(PlayerManagerActivity.this, BindDanmuActivity.class);
        intent.putExtra("bind_param", param);
        startActivityForResult(intent, SELECT_DANMU);
    }

    /**
     * 启动播放器
     */
    private void launchPlayerActivity() {
        saveDatabase();

        PlayParam playParam = new PlayParam();
        playParam.setVideoTitle(videoTitle);
        playParam.setVideoPath(videoPath);
        playParam.setDanmuPath(danmuPath);
        playParam.setZimuPath(zimuPath);
        playParam.setEpisodeId(episodeId);
        playParam.setCurrentPosition(currentPosition);
        playParam.setSourceOrigin(sourceOrigin);

        if (sourceOrigin == SOURCE_ONLINE_PREVIEW) {
            playParam.setThunderTaskId(thunderTaskId);
        }

        Intent intent = new Intent(this, PlayerActivity.class);
        intent.putExtra("video_data", playParam);
        this.startActivity(intent);
        PlayerManagerActivity.this.finish();
    }

    /**
     * 保存播放历史
     */
    public void saveDatabase() {
        DataBaseManager.getInstance()
                .selectTable("local_play_history")
                .query()
                .where("video_path", videoPath)
                .where("source_origin", String.valueOf(sourceOrigin))
                .postExecute(cursor -> {
                    if (cursor.getCount() > 0) {
                        DataBaseManager.getInstance()
                                .selectTable("local_play_history")
                                .update()
                                .param("video_title", videoTitle)
                                .param("danmu_path", danmuPath)
                                .param("zimu_path", zimuPath)
                                .param("episode_id", episodeId)
                                .param("play_time", System.currentTimeMillis())
                                .where("video_path", videoPath)
                                .where("source_origin", String.valueOf(sourceOrigin))
                                .executeAsync();
                    } else {
                        DataBaseManager.getInstance()
                                .selectTable("local_play_history")
                                .insert()
                                .param("video_path", videoPath)
                                .param("video_title", videoTitle)
                                .param("danmu_path", danmuPath)
                                .param("zimu_path", zimuPath)
                                .param("episode_id", episodeId)
                                .param("source_origin", sourceOrigin)
                                .param("play_time", System.currentTimeMillis())
                                .executeAsync();
                    }
                });
    }

    /**
     * 播放本地文件
     */
    public static void launchPlayerLocal(Context context, String title, String path, String danmu, String zimu, long position, int episodeId) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
        intent.putExtra("danmu_path", danmu);
        intent.putExtra("zimu_path", zimu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", SOURCE_ORIGIN_LOCAL);
        context.startActivity(intent);
    }

    /**
     * 播放局域网文件
     */
    public static void launchPlayerSmb(Context context, String title, String path, String zimu) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
        intent.putExtra("danmu_path", "");
        intent.putExtra("zimu_path", zimu);
        intent.putExtra("current_position", 0L);
        intent.putExtra("episode_id", 0);
        intent.putExtra("source_origin", SOURCE_ORIGIN_SMB);
        context.startActivity(intent);
    }

    /**
     * 播放串流链接
     */
    public static void launchPlayerStream(Context context, String title, String path, String danmu, long position, int episodeId) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
        intent.putExtra("danmu_path", danmu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", SOURCE_ORIGIN_STREAM);
        context.startActivity(intent);
    }

    /**
     * 播放远程文件
     */
    public static void launchPlayerRemote(Context context, String title, String path, String danmu, long position, int episodeId) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
        intent.putExtra("danmu_path", danmu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", SOURCE_ORIGIN_REMOTE);
        context.startActivity(intent);
    }

    /**
     * 在线播放文件
     */
    public static void launchPlayerOnline(Context context, String title, String path, String danmu, long position, int episodeId, long thunderTaskId, String searchWord) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
        intent.putExtra("danmu_path", danmu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("thunder_task_id", thunderTaskId);
        intent.putExtra("search_word", searchWord);
        intent.putExtra("source_origin", SOURCE_ONLINE_PREVIEW);
        context.startActivity(intent);
    }

    /**
     * 播放历史记录
     */
    public static void launchPlayerHistory(Context context, String title, String path, String danmu, String zimu, long position, int episodeId, int sourceOrigin) {
        Intent intent = new Intent(context, PlayerManagerActivity.class);
        intent.putExtra("video_title", title);
        intent.putExtra("video_path", path);
        intent.putExtra("danmu_path", danmu);
        intent.putExtra("zimu_path", zimu);
        intent.putExtra("current_position", position);
        intent.putExtra("episode_id", episodeId);
        intent.putExtra("source_origin", sourceOrigin);
        context.startActivity(intent);
    }

}
