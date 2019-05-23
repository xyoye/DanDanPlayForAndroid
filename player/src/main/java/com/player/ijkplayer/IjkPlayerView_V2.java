package com.player.ijkplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Color;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorListenerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.SPUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.ToastUtils;
import com.google.android.exoplayer2.text.CaptionStyleCompat;
import com.player.commom.listener.OnDanmakuListener;
import com.player.commom.listener.PlayerViewListener;
import com.player.commom.utils.AnimHelper;
import com.player.commom.utils.CommonPlayerUtils;
import com.player.commom.utils.Constants;
import com.player.commom.utils.PlayerConfigShare;
import com.player.commom.widgets.BottomBarView;
import com.player.commom.widgets.DanmuBlockView;
import com.player.commom.widgets.DanmuPostView;
import com.player.commom.widgets.DialogScreenShot;
import com.player.commom.widgets.SettingDanmuView;
import com.player.commom.widgets.SettingPlayerView;
import com.player.commom.widgets.SettingSubtitleView;
import com.player.commom.widgets.SkipTipView;
import com.player.commom.widgets.TopBarView;
import com.player.danmaku.controller.DrawHandler;
import com.player.danmaku.controller.IDanmakuView;
import com.player.danmaku.danmaku.loader.ILoader;
import com.player.danmaku.danmaku.loader.IllegalDataException;
import com.player.danmaku.danmaku.loader.android.BiliDanmakuLoader;
import com.player.danmaku.danmaku.model.BaseDanmaku;
import com.player.danmaku.danmaku.model.DanmakuTimer;
import com.player.danmaku.danmaku.model.android.DanmakuContext;
import com.player.danmaku.danmaku.parser.BaseDanmakuParser;
import com.player.danmaku.danmaku.parser.BiliDanmakuParser;
import com.player.danmaku.danmaku.parser.IDataSource;
import com.player.ijkplayer.media.IjkVideoView;
import com.player.ijkplayer.media.MediaPlayerParams;
import com.player.ijkplayer.media.VideoInfoTrack;
import com.player.subtitle.SubtitleParser;
import com.player.subtitle.SubtitleView;
import com.player.subtitle.util.TimedTextObject;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

import static com.player.commom.utils.TimeFormatUtils.generateTime;

/**
 * Created by xyoye on 2019/5/9.
 */

public class IjkPlayerView_V2 extends FrameLayout implements PlayerViewListener {
    //正常播放时，隐藏所有
    private static final int HIDE_VIEW_ALL = 0;
    //自动消失时，隐藏上下控制栏、截图、锁屏、亮度声音跳转
    private static final int HIDE_VIEW_AUTO = 1;
    //点击锁屏时，隐藏除锁屏外所有
    private static final int HIDE_VIEW_LOCK_SCREEN = 2;
    //手势结束时，隐藏、亮度声音跳转
    private static final int HIDE_VIEW_END_GESTURE = 3;
    //点击屏幕、点击返回时，隐藏三个设置、发送弹幕、屏蔽弹幕
    private static final int HIDE_VIEW_EDIT = 4;

    // 进度条最大值
    private static final int MAX_VIDEO_SEEK = 1000;
    // 默认隐藏控制栏时间
    private static final int DEFAULT_HIDE_TIMEOUT = 5000;
    // 更新进度消息
    private static final int MSG_UPDATE_SEEK = 10086;
    // 延迟屏幕翻转消息
    private static final int MSG_ENABLE_ORIENTATION = 10087;
    // 更新字幕消息
    private static final int MSG_UPDATE_SUBTITLE = 10088;
    //设置字幕源
    private static final int MSG_SET_SUBTITLE_SOURCE = 10089;
    // 无效变量
    private static final int INVALID_VALUE = -1;

    //视频View
    private IjkVideoView mVideoView;
    //字幕View
    private SubtitleView mSubtitleView;
    //弹幕View
    private IDanmakuView mDanmakuView;
    //顶部布局
    private TopBarView topBarView;
    //底部布局
    private BottomBarView bottomBarView;
    //跳转提示
    private SkipTipView skipTipView;
    //字幕选取提示
    private SkipTipView skipSubView;
    //弹幕屏蔽View
    private DanmuBlockView danmuBlockView;
    //弹幕发送View
    private DanmuPostView danmuPostView;
    // 加载
    private ProgressBar mLoadingView;
    // 音量
    private TextView mTvVolume;
    // 亮度
    private TextView mTvBrightness;
    // 滑动时间转提示
    private TextView mSkipTimeTv;
    // 触摸信息布局
    private FrameLayout mFlTouchLayout;
    // 整个视频框架布局
    private FrameLayout mFlVideoBox;
    // 锁屏键
    private ImageView mIvPlayerLock;
    // 截屏键
    private ImageView mIvScreenShot;

    // 关联的Activity
    private AppCompatActivity mAttachActivity;
    // 音量控制
    private AudioManager mAudioManager;
    // 手势控制
    private GestureDetector mGestureDetector;
    // 屏幕旋转角度监听
    private OrientationEventListener mOrientationListener;
    // 外部监听器
    private IMediaPlayer.OnOutsideListener mOutsideListener;
    // 弹幕控制相关
    private DanmakuContext mDanmakuContext;
    // 弹幕解析器
    private BaseDanmakuParser mDanmakuParser;
    // 弹幕加载器
    private ILoader mDanmakuLoader;
    // 弹幕监听器
    private OnDanmakuListener mDanmakuListener;

    // 最大音量
    private int mMaxVolume;
    // 锁屏
    private boolean mIsForbidTouch = false;
    // 是否显示控制栏
    private boolean mIsShowBar = true;
    // 是否正在拖拽进度条
    private boolean mIsSeeking;
    // 目标进度
    private long mTargetPosition = INVALID_VALUE;
    // 当前进度
    private long mCurPosition = INVALID_VALUE;
    // 当前音量
    private int mCurVolume = INVALID_VALUE;
    // 当前亮度
    private float mCurBrightness = INVALID_VALUE;
    // 初始高度
    private int mInitHeight;
    // 屏幕宽/高度
    private int mWidthPixels;
    // 进来还未播放
    private boolean mIsNeverPlay = true;
    //上次播放跳转时间
    private long mSkipPosition = INVALID_VALUE;
    //是否展示字幕
    private boolean isShowSubtitle = false;
    // 是否播放结束
    private boolean mIsPlayComplete = false;
    //是否使用surfaceView
    private boolean isUseSurfaceView;
    //是否使用硬解码
    private boolean isUsemediaCodeC;
    //是否使用h265硬解码
    private boolean isUsemediaCodeCH265;
    //是否使用OpenSLES
    private boolean isUseOpenSLES;
    //使用播放器的类型
    private int usePlayerType;
    //使用播放器的格式
    private String usePixelFormat;
    // 弹幕要跳转的目标位置，等视频播放再跳转，不然老出现只有弹幕在动的情况
    private long mDanmakuTargetPosition = INVALID_VALUE;
    // 播放器是否已准备好
    private boolean mIsIjkPlayerReady = false;
    // 这个用来控制弹幕启动和视频同步
    private boolean mIsRenderingStart = false;
    //是否查询网络字幕
    private boolean isQueryNetworkSubtitle = false;
    //是否自动加载同名字幕
    private boolean isAutoLoadLocalSubtitle = false;
    //是否自动加载网络字幕
    private boolean isAutoLoadNetworkSubtitle = false;

    //云屏蔽数据
    private List<String> cloudFilterList = new ArrayList<>();
    //音频流数据
    private List<VideoInfoTrack> audioTrackList = new ArrayList<>();
    //字幕流数据
    private List<VideoInfoTrack> subtitleTrackList = new ArrayList<>();

    //隐藏控制栏视图Runnable
    private Runnable mHideBarRunnable = () -> hideView(HIDE_VIEW_AUTO);
    //隐藏亮度、声音、跳转视图Runnable
    private Runnable mHideTouchViewRunnable = () -> hideView(HIDE_VIEW_END_GESTURE);
    //隐藏跳转上一次播放提示视图Runnable
    private Runnable mHideSkipTipRunnable = this::_hideSkipTip;
    //隐藏选取字幕提示视图Runnable
    private Runnable mHideSkipSubRunnable = this::_hideSkipSub;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                //更新进度消息
                case MSG_UPDATE_SEEK:
                    long pos = _setProgress();
                    if (!mIsSeeking && mIsShowBar && mVideoView.isPlaying()) {
                        msg = obtainMessage(MSG_UPDATE_SEEK);
                        sendMessageDelayed(msg, 1000 - (pos % 1000));
                    }
                    break;
                //延迟屏幕翻转消息
                case MSG_ENABLE_ORIENTATION:
                    setOrientationEnable(true);
                    break;
                //更新字幕消息
                case MSG_UPDATE_SUBTITLE:
                    if (topBarView.getSubtitleSettingView().isLoadSubtitle() && isShowSubtitle){
                        long position = mVideoView.getCurrentPosition() + (int)(topBarView.getSubtitleSettingView().getTimeOffset() * 1000);
                        mSubtitleView.seekTo(position);
                        msg = obtainMessage(MSG_UPDATE_SUBTITLE);
                        sendMessageDelayed(msg, 1000);
                    }
                    break;
                //设置字幕源
                case MSG_SET_SUBTITLE_SOURCE:
                    TimedTextObject subtitleObj = (TimedTextObject) msg.obj;
                    isShowSubtitle = true;
                    topBarView.getSubtitleSettingView().setLoadSubtitle(true);
                    topBarView.getSubtitleSettingView().setSubtitleLoadStatus(true);
                    mSubtitleView.setData(subtitleObj);
                    mSubtitleView.start();
                    Toast.makeText(getContext(), "加载字幕成功", Toast.LENGTH_LONG).show();
                    break;
            }
        }
    };

    public IjkPlayerView_V2(Context context) {
        this(context, null);
    }

    public IjkPlayerView_V2(Context context, AttributeSet attrs) {
        super(context, attrs);

        initViewBefore(context);

        initView();

        initViewCallBak();

        initViewAfter();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (mInitHeight == 0) {
            mInitHeight = getHeight();
            mWidthPixels = getResources().getDisplayMetrics().widthPixels;
        }
    }

    private void initViewBefore(Context context){
        if (!(context instanceof AppCompatActivity)) {
            throw new IllegalArgumentException("Context must be AppCompatActivity");
        }
        //获取绑定的Activity实例
        mAttachActivity = (AppCompatActivity) context;
        //加载布局
        View.inflate(context, R.layout.layout_ijk_player_view_v2, this);
        //屏幕翻转控制
        mOrientationListener = new OrientationEventListener(mAttachActivity) {
            @Override
            public void onOrientationChanged(int orientation) {
                if (mIsNeverPlay) {
                    return;
                }
                // 根据角度进行横屏切换
                if (orientation >= 60 && orientation <= 120) {
                    mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE);
                } else if (orientation >= 240 && orientation <= 300) {
                    mAttachActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
                }
            }
        };
        //声音管理器
        mAudioManager = (AudioManager) mAttachActivity.getSystemService(Context.AUDIO_SERVICE);
        if (mAudioManager != null)
            mMaxVolume = mAudioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //亮度管理
        try {
            int brightness = Settings.System.getInt(mAttachActivity.getContentResolver(), Settings.System.SCREEN_BRIGHTNESS);
            float progress = 1.0F * (float) brightness / 255.0F;
            WindowManager.LayoutParams layout = mAttachActivity.getWindow().getAttributes();
            layout.screenBrightness = progress;
            mAttachActivity.getWindow().setAttributes(layout);
        } catch (Settings.SettingNotFoundException var7) {
            var7.printStackTrace();
        }

        //是否使用surfaceView
        isUseSurfaceView = SPUtils.getInstance().getBoolean("surface_renders");
        //是否使用硬解码
        isUsemediaCodeC = SPUtils.getInstance().getBoolean("media_code_c");
        //是否使用h265硬解码
        isUsemediaCodeCH265 = SPUtils.getInstance().getBoolean("media_code_c_h265");
        //是否使用OpenSLES
        isUseOpenSLES = SPUtils.getInstance().getBoolean("open_sles");
        //使用播放器的类型
        usePlayerType = SPUtils.getInstance().getInt("player_type");
        //使用播放器的格式
        usePixelFormat = SPUtils.getInstance().getString("pixel_format", "fcc-_es2");
    }

    private void initView() {
        //主要的控件：视频、弹幕、字幕
        mVideoView = findViewById(R.id.ijk_video_view);
        mDanmakuView = findViewById(R.id.sv_danmaku);
        mSubtitleView = findViewById(R.id.subtitle_view);
        //头部、底部、跳转提示
        bottomBarView = findViewById(R.id.bottom_bar_view);
        topBarView = findViewById(R.id.top_bar_view);
        skipTipView = findViewById(R.id.skip_tip_view);
        skipSubView = findViewById(R.id.skip_subtitle_view);
        //loading、声音、亮度、跳转提示
        mLoadingView = findViewById(R.id.pb_loading);
        mTvVolume = findViewById(R.id.tv_volume);
        mTvBrightness = findViewById(R.id.tv_brightness);
        mSkipTimeTv = findViewById(R.id.tv_skip_time);
        mFlTouchLayout = findViewById(R.id.fl_touch_layout);
        //最底层布局
        mFlVideoBox = findViewById(R.id.fl_video_box);
        //锁屏、截屏
        mIvPlayerLock = findViewById(R.id.iv_player_lock);
        mIvScreenShot = findViewById(R.id.iv_player_shot);
        //弹幕屏蔽、弹幕发送
        danmuBlockView = findViewById(R.id.danmu_block_view);
        danmuPostView = findViewById(R.id.danmu_post_view);
        //初始化字幕控制组件
        initSubtitleSettingView();
        //初始化弹幕控制组件
        initDanmuSettingView();
        //初始化播放器控制组件
        initPlayerSettingView();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initViewCallBak(){
        //底层框架的触摸事件
        View.OnTouchListener mPlayerTouchListener = (v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mHandler.removeCallbacks(mHideBarRunnable);
                    break;
                case MotionEvent.ACTION_UP:
                    _endGesture();
                    break;
            }
            return mGestureDetector.onTouchEvent(event);
        };
        //手势监听回调
        GestureDetector.OnGestureListener mPlayerGestureListener = new GestureDetector.SimpleOnGestureListener() {
            // 是否是按下的标识，默认为其他动作，true为按下标识，false为其他动作
            private boolean isDownTouch;
            // 是否声音控制,默认为亮度控制，true为声音控制，false为亮度控制
            private boolean isVolume;
            // 是否横向滑动，默认为纵向滑动，true为横向滑动，false为纵向滑动
            private boolean isLandscape;

            @Override
            public boolean onDown(MotionEvent e) {
                isDownTouch = true;
                return super.onDown(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (!mIsForbidTouch && !mIsNeverPlay) {
                    float mOldX = e1.getX(), mOldY = e1.getY();
                    float deltaY = mOldY - e2.getY();
                    float deltaX = mOldX - e2.getX();
                    if (isDownTouch) {
                        // 判断左右或上下滑动
                        isLandscape = Math.abs(distanceX) >= Math.abs(distanceY);
                        // 判断是声音或亮度控制
                        isVolume = mOldX > getResources().getDisplayMetrics().widthPixels * 0.5f;
                        isDownTouch = false;
                    }

                    if (isLandscape) {
                        _onProgressSlide(-deltaX / mVideoView.getWidth());
                    } else {
                        float percent = deltaY / mVideoView.getHeight();
                        if (isVolume) {
                            _onVolumeSlide(percent);
                        } else {
                            _onBrightnessSlide(percent);
                        }
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                _toggleControlBar();
                return true;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                // 如果未进行播放或从弹幕编辑状态返回则不执行双击操作
                if (mIsNeverPlay) {
                    return true;
                }
                if (!mIsForbidTouch) {
                    _refreshHideRunnable();
                    _togglePlayStatus();
                }
                return true;
            }
        };
        //进度条回调
        SeekBar.OnSeekBarChangeListener mSeekListener = new SeekBar.OnSeekBarChangeListener() {

            private long curPosition;

            @Override
            public void onStartTrackingTouch(SeekBar bar) {
                mIsSeeking = true;
                _showControlBar(3600000);
                mHandler.removeMessages(MSG_UPDATE_SEEK);
                curPosition = mVideoView.getCurrentPosition();
            }

            @Override
            public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {
                if (!fromUser) {
                    return;
                }
                long duration = mVideoView.getDuration();
                // 计算目标位置
                mTargetPosition = (duration * progress) / MAX_VIDEO_SEEK;
                int deltaTime = (int) ((mTargetPosition - curPosition) / 1000);
                String desc;
                // 对比当前位置来显示快进或后退
                if (mTargetPosition > curPosition) {
                    desc = generateTime(mTargetPosition) + "/" + generateTime(duration) + "\n" + "+" + deltaTime + "秒";
                } else {
                    desc = generateTime(mTargetPosition) + "/" + generateTime(duration) + "\n" + deltaTime + "秒";
                }
                setSkipTimeText(desc);
            }

            @Override
            public void onStopTrackingTouch(SeekBar bar) {
                hideView(HIDE_VIEW_AUTO);
                mIsSeeking = false;
                // 视频跳转
                seekTo((int) mTargetPosition);
                mTargetPosition = INVALID_VALUE;
                _setProgress();
                _showControlBar(DEFAULT_HIDE_TIMEOUT);
            }
        };
        //播放器解析事件回调
        IMediaPlayer.OnPreparedListener ijkPreparedCallback = iMediaPlayer -> {
            ITrackInfo[] info = mVideoView.getTrackInfo();
            if (info != null){
                int selectAudioTrack = mVideoView.getSelectedTrack(IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO);
                int audioN = 1;
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getTrackType() == IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                        VideoInfoTrack videoInfoTrack = new VideoInfoTrack();
                        String name = TextUtils.isEmpty(info[i].getTitle())
                                ? (TextUtils.isEmpty(info[i].getLanguage())
                                ? "UND"
                                : info[i].getLanguage())
                                : info[i].getTitle();
                        videoInfoTrack.setName("音频流#" + audioN + "（" + name + "）");
                        videoInfoTrack.setStream(i);
                        if (i == selectAudioTrack) videoInfoTrack.setSelect(true);
                        audioN++;
                        audioTrackList.add(videoInfoTrack);
                    }
                }
                VideoInfoTrack videoInfoTrack = new VideoInfoTrack();
                videoInfoTrack.setName("IJK播放器不支持字幕流管理");
                videoInfoTrack.setStream(-1);
                videoInfoTrack.setSelect(true);
                subtitleTrackList.add(videoInfoTrack);
                //ijk不提供内置字幕管理
                topBarView.getSubtitleSettingView().setInnerSubtitleCtrl(false);
                topBarView.getPlayerSettingView().setSubtitleTrackList(subtitleTrackList);
                topBarView.getPlayerSettingView().setVideoTrackList(audioTrackList);
            }
        };
        //播放器错误事件回调
        IMediaPlayer.OnErrorListener ijkErrorCallback = (iMediaPlayer, i, i1) -> {
            Toast.makeText(getContext(), "播放错误，试试切换其它播放器", Toast.LENGTH_LONG).show();
            mLoadingView.setVisibility(GONE);
            return false;
        };
        //播放器播放事件回调
        IMediaPlayer.OnInfoListener ijkPlayInfoCallback = (iMediaPlayer, status, extra) -> {
            _switchStatus(status);
            return true;
        };
        //弹幕view绘制事件回调
        DrawHandler.Callback drawHandlerCallBack = new DrawHandler.Callback() {
            @Override
            public void prepared() {
                mAttachActivity.runOnUiThread(() -> {
                    if (mVideoView.isPlaying() && mIsIjkPlayerReady) {
                        mDanmakuView.start();
                    }
                });
            }

            @Override
            public void updateTimer(DanmakuTimer timer) {

            }

            @Override
            public void danmakuShown(BaseDanmaku danmaku) {
            }

            @Override
            public void drawingFinished() {
            }
        };

        //顶部view事件回调
        TopBarView.TopBarListener topBarCallBack = new TopBarView.TopBarListener() {
            @Override
            public void onBack() {
                stop();
                mAttachActivity.finish();
            }

            @Override
            public void topBarItemClick() {
                hideView(HIDE_VIEW_ALL);
            }
        };
        //底部view事件回调
        BottomBarView.BottomBarListener bottomBarCallBack = new BottomBarView.BottomBarListener() {
            @Override
            public void onPlayClick() {
                _togglePlayStatus();
            }

            @Override
            public void onDanmuCtrlClick() {
                showOrHideDanmaku(bottomBarView.getDanmuIvStatus());
            }

            @Override
            public void onDanmuSendClick() {
                if (mDanmakuListener == null || mDanmakuListener.isValid()) {
                    pause();
                    hideView(HIDE_VIEW_ALL);
                    danmuPostView.setParserDensity(mDanmakuParser.getDisplayer().getDensity());
                    danmuPostView.setVisibility(VISIBLE);
                }
            }
        };
        //跳转提示事件回调
        SkipTipView.SkipTipListener skipTipCallBack = new SkipTipView.SkipTipListener() {
            @Override
            public void onCancel() {
                mHandler.removeCallbacks(mHideSkipTipRunnable);
                _hideSkipTip();
            }

            @Override
            public void onSkip() {
                mLoadingView.setVisibility(VISIBLE);
                // 视频跳转
                seekTo(mSkipPosition);
                mHandler.removeCallbacks(mHideSkipTipRunnable);
                _hideSkipTip();
                _setProgress();
            }
        };
        //字幕选取事件回调
        SkipTipView.SkipTipListener skipSubCallBack = new SkipTipView.SkipTipListener() {
            @Override
            public void onCancel() {
                mHandler.removeCallbacks(mHideSkipSubRunnable);
                _hideSkipSub();
            }

            @Override
            public void onSkip() {
                _hideSkipSub();
                mOutsideListener.onAction( Constants.INTENT_SELECT_SUBTITLE, 0);
            }
        };
        //弹幕屏蔽事件回调
        DanmuBlockView.DanmuBlockListener danmuBlockCallBack = new DanmuBlockView.DanmuBlockListener() {
            @Override
            public void removeBlock(String text) {
                //从数据库移除
                if (mDanmakuListener != null){
                    mDanmakuListener.deleteBlock(text);
                }
                //弹幕中移除
                mDanmakuContext.removeKeyWordBlackList(text);
                ToastUtils.showLong("已移除“ "+ text +" ”");
            }

            @Override
            public void addBlock(String blockText) {
                //添加到数据库
                if(mDanmakuListener != null){
                    mDanmakuListener.addBlock(blockText);
                }
                //添加到弹幕屏蔽
                mDanmakuContext.addBlockKeyWord(blockText);
            }

            @Override
            public void onCloseView() {
                danmuBlockView.setVisibility(GONE);
            }
        };
        //弹幕发送事件回调
        DanmuPostView.DanmuPostListener danmuPostCallBack = new DanmuPostView.DanmuPostListener() {
            @Override
            public void postDanmu(String text, float size, int type, int color) {
                sendDanmaku(text, size, type, color);
            }

            @Override
            public void onClose() {

            }
        };

        mFlVideoBox.setOnTouchListener(mPlayerTouchListener);
        mGestureDetector = new GestureDetector(mAttachActivity, mPlayerGestureListener);
        bottomBarView.setSeekCallBack(mSeekListener);
        mVideoView.setOnPreparedListener(ijkPreparedCallback);
        mVideoView.setOnErrorListener(ijkErrorCallback);
        mVideoView.setOnInfoListener(ijkPlayInfoCallback);
        mDanmakuView.setCallback(drawHandlerCallBack);

        topBarView.setCallBack(topBarCallBack);
        bottomBarView.setCallBack(bottomBarCallBack);
        skipTipView.setCallBack(skipTipCallBack);
        skipSubView.setCallBack(skipSubCallBack);
        danmuBlockView.setCallBack(danmuBlockCallBack);
        danmuPostView.setCallBack(danmuPostCallBack);

        mIvPlayerLock.setOnClickListener(v -> _togglePlayerLock());
        mIvScreenShot.setOnClickListener(v -> {
            if (!isUseSurfaceView){
                pause();
                new DialogScreenShot(mAttachActivity, mVideoView.getScreenshot()).show();
            }else {
                ToastUtils.showShort("当前渲染器不支持截屏");
            }
        });

        //监听进度条触摸放开事件，隐藏跳转View
        bottomBarView.setSeekBarTouchCallBack((view, motionEvent) -> {
            if (motionEvent.getAction() == MotionEvent.ACTION_UP){
                hideView(HIDE_VIEW_END_GESTURE);
            }
            return false;
        });
        //播放器view的触摸事件
        mVideoView.setOnTouchListener((v, event) -> {
            //关闭编辑的view
            //不在手势操作处拦截的原因是
            //在手势操作处拦截会触发view的touch事件
            if(isEditViewVisible()){
                hideView(HIDE_VIEW_EDIT);
                return true;
            }
            return false;
        });
    }

    private void initViewAfter(){
        mVideoView.setIsUsingMediaCodec(isUsemediaCodeC);
        mVideoView.setIsUsingMediaCodecH265(isUsemediaCodeCH265);
        mVideoView.setPixelFormat(usePixelFormat);
        mVideoView.setIsUsingOpenSLES(isUseOpenSLES);
        mVideoView.setIsUsingSurfaceRenders(isUseSurfaceView);
        mVideoView.setIsUsingPlayerType(usePlayerType);

        //使用openSLES及MediaPlayer时不能变速播放
        if (isUseOpenSLES || usePlayerType == Constants.IJK_ANDROID_PLAYER){
            topBarView.getPlayerSettingView().setSpeedCtrlLLVis(false);
        }

        //设置进度条最大值
        bottomBarView.setSeekMax(MAX_VIDEO_SEEK);
        //启动隐藏上下控制栏线程
        mHandler.post(mHideBarRunnable);
        //底层框架可点击
        mFlVideoBox.setClickable(true);
        //初始化弹幕
        initDanmu();
    }

    /**
     * 初始化弹幕项目
     */
    @SuppressLint("UseSparseArrays")
    private void initDanmu(){
        SettingDanmuView mSettingDanmuView = topBarView.getDanmuSettingView();
        //设置禁止重叠
        Map<Integer, Boolean> overlappingEnablePair = new HashMap<>();
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_LR, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_SCROLL_RL, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_TOP, true);
        overlappingEnablePair.put(BaseDanmaku.TYPE_FIX_BOTTOM, true);
        //弹幕view开启绘制缓存
        mDanmakuView.enableDanmakuDrawingCache(true);
        //创建实例
        mDanmakuContext = DanmakuContext.create();
        //设置防弹幕重叠
        mDanmakuContext.preventOverlapping(overlappingEnablePair);
        //合并重复弹幕
        mDanmakuContext.setDuplicateMergingEnabled(true);
        //弹幕文字大小
        mDanmakuContext.setScaleTextSize(mSettingDanmuView.getmDanmuTextSize());
        //弹幕文字透明度
        mDanmakuContext.setDanmakuTransparency(mSettingDanmuView.getmDanmuTextAlpha());
        //弹幕滚动速度
        mDanmakuContext.setScrollSpeedFactor(2.5f- mSettingDanmuView.getmDanmuSpeed());
        //是否显示滚动弹幕
        mDanmakuContext.setR2LDanmakuVisibility(mSettingDanmuView.isShowMobile());
        //是否显示顶部弹幕
        mDanmakuContext.setFTDanmakuVisibility(mSettingDanmuView.isShowTop());
        //是否显示底部弹幕
        mDanmakuContext.setFBDanmakuVisibility(mSettingDanmuView.isShowBottom());
        //同屏数量限制
        mDanmakuContext.setMaximumVisibleSizeInScreen(mSettingDanmuView.getDanmuNumberLimit());
        //初始化BiliBili弹幕解析器
        mDanmakuParser = new BiliDanmakuParser();
        //初始化弹幕加载器
        mDanmakuLoader = BiliDanmakuLoader.instance();
    }

    /**
     * 初始化字幕设置View
     */
    public void initSubtitleSettingView(){
        int subtitleChineseProgress = PlayerConfigShare.getInstance().getSubtitleChineseSize();
        int subtitleEnglishProgress = PlayerConfigShare.getInstance().getSubtitleEnglishSize();
        float subtitleChineseSize = (float) subtitleChineseProgress / 100 * ConvertUtils.dp2px(18);
        float subtitleEnglishSize = (float) subtitleEnglishProgress / 100 * ConvertUtils.dp2px(18);
        mSubtitleView.setTextSize(subtitleChineseSize, subtitleEnglishSize);
        topBarView.getSubtitleSettingView()
                .initSubtitleCnSize(subtitleChineseProgress)
                .initSubtitleEnSize(subtitleEnglishProgress)
                .initListener(new SettingSubtitleView.SettingSubtitleListener() {
                    @Override
                    public void setSubtitleSwitch(Switch switchView, boolean isChecked) {
                        if (!topBarView.getSubtitleSettingView().isLoadSubtitle() && isChecked){
                            switchView.setChecked(false);
                            Toast.makeText(getContext(), "未加载字幕源", Toast.LENGTH_LONG).show();
                        }
                        if (isChecked){
                            isShowSubtitle = true;
                            mSubtitleView.show();
                            mHandler.sendEmptyMessage(MSG_UPDATE_SUBTITLE);
                        }else {
                            isShowSubtitle = false;
                            mSubtitleView.hide();
                        }
                    }

                    @Override
                    public void setSubtitleCnSize(int progress) {
                        float calcProgress = (float) progress;
                        float textSize = (calcProgress/100) * ConvertUtils.dp2px(18);
                        mSubtitleView.setTextSize(SubtitleView.LANGUAGE_TYPE_CHINA,textSize);
                        PlayerConfigShare.getInstance().setSubtitleChineseSize(progress);
                    }

                    @Override
                    public void setInterSubtitleSize(int progress) {

                    }

                    @Override
                    public void setInterBackground(CaptionStyleCompat compat) {

                    }

                    @Override
                    public void setSubtitleEnSize(int progress) {
                        float calcProgress = (float) progress;
                        float textSize = (calcProgress/100) * ConvertUtils.dp2px(18);
                        mSubtitleView.setTextSize(SubtitleView.LANGUAGE_TYPE_ENGLISH,textSize);
                        PlayerConfigShare.getInstance().setSubtitleEnglishSize(progress);
                    }

                    @Override
                    public void setOpenSubtitleSelector() {
                        mOutsideListener.onAction( Constants.INTENT_OPEN_SUBTITLE, 0);
                    }

                    @Override
                    public void setSubtitleLanguageType(int type) {
                        PlayerConfigShare.getInstance().setSubtitleLanguageType(type);
                        mSubtitleView.setLanguage(type);
                    }

                    @Override
                    public void onShowNetworkSubtitle() {
                        hideView(HIDE_VIEW_ALL);
                        if (mOutsideListener != null)
                            mOutsideListener.onAction(Constants.INTENT_SELECT_SUBTITLE, 0);
                    }
                })
                .init();
    }

    /**
     * 初始化弹幕设置View
     */
    private void initDanmuSettingView() {
        int progressSize = PlayerConfigShare.getInstance().getDanmuSize();
        int progressSpeed = PlayerConfigShare.getInstance().getDanmuSpeed();
        int progressAlpha = PlayerConfigShare.getInstance().getDanmuAlpha();
        int numberLimit = PlayerConfigShare.getInstance().getDanmuNumberLimit();
        boolean isShowMobile = PlayerConfigShare.getInstance().isShowMobileDanmu();
        boolean isShowTop = PlayerConfigShare.getInstance().isShowTopDanmu();
        boolean isShowBottom = PlayerConfigShare.getInstance().isShowBottomDanmu();
        topBarView.getDanmuSettingView()
                .setOffsetTime(0.5f)
                .setDanmuSize(progressSize)
                .setDanmuSpeed(progressSpeed)
                .setDanmuAlpha(progressAlpha)
                .setDanmuNumberLimit(numberLimit)
                .setBlockEnable(isShowMobile, isShowTop, isShowBottom)
                .setListener(new SettingDanmuView.SettingDanmuListener() {
                    @Override
                    public void setDanmuSize(int progress) {
                        mDanmakuContext.setScaleTextSize((float) progress/50);
                        PlayerConfigShare.getInstance().saveDanmuSize(progress);
                    }

                    @Override
                    public void setDanmuSpeed(int progress) {
                        float speed = (float) progress / 40;
                        speed = speed>2.4f ? 2.4f : speed;
                        mDanmakuContext.setScrollSpeedFactor(2.5f - speed);
                        PlayerConfigShare.getInstance().saveDanmuSpeed(progress);
                    }

                    @Override
                    public void setDanmuAlpha(int progress) {
                        mDanmakuContext.setDanmakuTransparency((float) progress/100);
                        PlayerConfigShare.getInstance().saveDanmuAlpha(progress);
                    }

                    @Override
                    public void setExtraTime(int time) {
                        if (mDanmakuView != null && mDanmakuView.isPrepared()){
                            try {
                                mDanmakuView.seekTo(mDanmakuView.getCurrentTime() + time);
                            }catch (Exception e){
                                Toast.makeText(getContext(), "请输入正确的时间", Toast.LENGTH_LONG).show();
                            }
                        }
                    }

                    @Override
                    public void setCloudFilter(boolean isChecked) {
                        mDanmakuListener.setCloudFilter(isChecked);
                        if (isChecked){
                            mDanmakuContext.addBlockKeyWord(cloudFilterList);
                        }else {
                            mDanmakuContext.removeKeyWordBlackList(cloudFilterList);
                        }
                    }

                    @Override
                    public void setDanmuMobEnable(boolean enable) {
                        mDanmakuContext.setR2LDanmakuVisibility(enable);
                        PlayerConfigShare.getInstance().setShowMobileDanmu(enable);
                    }

                    @Override
                    public void setDanmuTopEnable(boolean enable) {
                        mDanmakuContext.setFTDanmakuVisibility(enable);
                        PlayerConfigShare.getInstance().setShowMobileDanmu(enable);
                    }

                    @Override
                    public void setDanmuBotEnable(boolean enable) {
                        mDanmakuContext.setFBDanmakuVisibility(enable);
                        PlayerConfigShare.getInstance().setShowMobileDanmu(enable);
                    }

                    @Override
                    public void setBlockViewShow() {
                        topBarView.hideItemView();
                        danmuBlockView.setVisibility(VISIBLE);
                        pause();
                    }

                    @Override
                    public void setExtraTimeAdd(int time) {
                        if (mDanmakuView != null && mDanmakuView.isPrepared()){
                            mDanmakuView.seekTo(mDanmakuView.getCurrentTime() - time);
                        }
                    }

                    @Override
                    public void setExtraTimeReduce(int time) {
                        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isShown()){
                            mDanmakuView.seekTo(mDanmakuView.getCurrentTime() + time);
                        }
                    }

                    @Override
                        public void setNumberLimit(int num) {
                        PlayerConfigShare.getInstance().setDanmuNumberLimit(num);
                        mDanmakuContext.setMaximumVisibleSizeInScreen(num);
                    }
                })
                .init();
    }

    /**
     * 初始化播放器设置View
     */
    private void initPlayerSettingView(){
        boolean allowOrientationChange = PlayerConfigShare.getInstance().isAllowOrientationChange();
        topBarView.getPlayerSettingView()
                .setOrientationAllow(allowOrientationChange)
                .setSettingListener(new SettingPlayerView.SettingVideoListener() {
                    @Override
                    public void selectTrack(int streamId, String language, boolean isAudio) {
                        mVideoView.selectTrack(streamId);
                        mVideoView.seekTo(mVideoView.getCurrentPosition());
                    }

                    @Override
                    public void deselectTrack(int streamId, String language, boolean isAudio) {
                        mVideoView.deselectTrack(streamId);
                    }

                    @Override
                    public void setSpeed(float speed) {
                        mVideoView.setSpeed(speed);
                        mDanmakuContext.setDanmuTimeRate(speed);
                    }

                    @Override
                    public void setAspectRatio(int type) {
                        mVideoView.setAspectRatio(type);
                    }

                    @Override
                    public void setOrientationStatus(boolean isEnable) {
                        if (mOrientationListener != null) {
                            if (isEnable)
                                mOrientationListener.enable();
                            else
                                mOrientationListener.disable();
                        }
                    }
                });
    }

    /**
     * Activity.onResume() 里调用
     */
    @Override
    public void onResume() {
        if (mCurPosition != INVALID_VALUE) {
            seekTo(mCurPosition);
            mCurPosition = INVALID_VALUE;
        }
    }

    /**
     * Activity.onPause() 里调用
     */
    @Override
    public void onPause() {
        mCurPosition = mVideoView.getCurrentPosition();
        pause();
    }

    /**
     * Activity.onDestroy() 里调用，返回播放进度
     */
    @Override
    public void onDestroy() {
        mVideoView.destroy();
        if (mDanmakuView != null) {
            // don't forget release!
            mDanmakuView.release();
            mDanmakuView = null;
        }
        // 关闭屏幕常亮
        mAttachActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    /**
     * 处理音量键，避免外部按音量键后导航栏和状态栏显示出来退不回去的状态
     */
    @Override
    public boolean handleVolumeKey(int keyCode) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            _setVolume(true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            _setVolume(false);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void configurationChanged(Configuration newConfig) {
        _refreshOrientationEnable();
        hideView(HIDE_VIEW_ALL);
    }

    /**
     * 回退，全屏时退回竖屏
     */
    @Override
    public boolean onBackPressed() {
        if (isEditViewVisible()) {
            hideView(HIDE_VIEW_EDIT);
            return true;
        }
        stop();
        mAttachActivity.finish();
        return true;
    }

    /**
     * 电量改变
     */
    @Override
    public void setBatteryChanged(int status, int progress){
        if (topBarView != null){
            topBarView.setBatteryChanged(status, progress);
        }
    }

    /**
     * 设置字幕源
     */
    @Override
    public void setSubtitlePath(String subtitlePath){
        isShowSubtitle = false;
        topBarView.getSubtitleSettingView().setLoadSubtitle(false);
        topBarView.getSubtitleSettingView().setSubtitleLoadStatus(false);
        new Thread(() -> {
            TimedTextObject subtitleObj = new SubtitleParser(subtitlePath).parser();
            if (subtitleObj != null){
                Message message = new Message();
                message.what = MSG_SET_SUBTITLE_SOURCE;
                message.obj = subtitleObj;
                mHandler.sendMessage(message);
            }
        }).start();
    }

    /**
     * 查询到网络字幕
     */
    @Override
    public void onSubtitleQuery(int size){
        topBarView.getSubtitleSettingView().setNetwoekSubtitleVisible(true);
        if(isAutoLoadNetworkSubtitle)
            mOutsideListener.onAction(Constants.INTENT_AUTO_SUBTITLE, 0);
        else
            _showSkipSub(size);
    }

    /**
     * 屏幕被锁定
     */
    @Override
    public void onScreenLocked(){

    }

    /**
     * 设置视频资源
     */
    public IjkPlayerView_V2 setVideoPath(String videoPath) {
        loadDefaultSubtitle(videoPath);
        mVideoView.setVideoURI(Uri.parse(videoPath));
        seekTo(0);
        return this;
    }

    /**
     * 设置弹幕资源
     */
    public IjkPlayerView_V2 setDanmakuSource(InputStream stream) {
        if (stream == null) {
            return this;
        }
        try {
            mDanmakuLoader.load(stream);
        } catch (IllegalDataException e) {
            e.printStackTrace();
        }
        IDataSource<?> dataSource = mDanmakuLoader.getDataSource();
        mDanmakuParser.load(dataSource);
        return this;
    }

    /**
     * 设置标题
     */
    public IjkPlayerView_V2 setTitle(String title) {
        if (topBarView != null)
            topBarView.setTitleText(title);
        return this;
    }

    /**
     * 设置普通弹幕屏蔽数据
     */
    public IjkPlayerView_V2 setNormalFilterData(List<String> blockList){
        if (blockList != null){
            danmuBlockView.setBlockList(blockList);
            for (String block : blockList){
                mDanmakuContext.addBlockKeyWord(block);
            }
        }else {
            danmuBlockView.setBlockList(new ArrayList<>());
        }
        return this;
    }

    /**
     * 设置云屏蔽数据
     * @param data 数据源
     * @param isOpen 当前是否开启
     */
    public IjkPlayerView_V2 setCloudFilterData(List<String> data, boolean isOpen) {
        cloudFilterList = data;
        topBarView.getDanmuSettingView().setCloudFilterStatus(isOpen);
        return this;
    }

    /**
     * 外部接口回调
     */
    public IjkPlayerView_V2 setOnInfoListener(IMediaPlayer.OnOutsideListener listener) {
        mOutsideListener = listener;
        return this;
    }

    /**
     * 设置弹幕监听器
     */
    public IjkPlayerView_V2 setDanmakuListener(OnDanmakuListener danmakuListener) {
        mDanmakuListener = danmakuListener;
        return this;
    }

    /**
     * 设置跳转提示
     */
    public IjkPlayerView_V2 setSkipTip(long targetPositionMs) {
        if (targetPositionMs > 0){
            mSkipPosition = targetPositionMs;
        }
        return this;
    }

    /**
     * 是否查询网络字幕
     */
    public IjkPlayerView_V2 setNetworkSubtitle(boolean isOpen){
        isQueryNetworkSubtitle = isOpen;
        return this;
    }

    /**
     * 是否自动加载同名字幕
     */
    public IjkPlayerView_V2 setAutoLoadLocalSubtitle(boolean isAuto){
        isAutoLoadLocalSubtitle = isAuto;
        return this;
    }

    /**
     * 是否自动加载网络字幕
     */
    public IjkPlayerView_V2 setAutoLoadNetworkSubtitle(boolean isAuto){
        isAutoLoadNetworkSubtitle = isAuto;
        return this;
    }

    /**
     * 显示/隐藏弹幕
     */
    public IjkPlayerView_V2 showOrHideDanmaku(boolean isShow) {
        if (isShow) {
            bottomBarView.setDanmuIvStatus(false);
            mDanmakuView.show();
        } else {
            bottomBarView.setDanmuIvStatus(true);
            mDanmakuView.hide();
        }
        return this;
    }

    /**
     * 开始播放
     */
    public void start() {
        //第一次播放时
        if (mIsNeverPlay) {
            mIsNeverPlay = false;
            //显示加载动画
            mLoadingView.setVisibility(VISIBLE);
            mIsShowBar = false;
            //装载弹幕
            mDanmakuView.prepare(mDanmakuParser, mDanmakuContext);
            //查询网络字幕
            if (isQueryNetworkSubtitle && mOutsideListener != null){
                mOutsideListener.onAction(Constants.INTENT_QUERY_SUBTITLE, 0);
            }
        }
        //切换播放按钮状态
        bottomBarView.setPlayIvStatus(true);
        //设置可以播放
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
            // 更新进度
            mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        }

        //已播放结束后，再点击播放，重新设置弹幕播放进度
        if (mIsPlayComplete) {
            if (mDanmakuView != null && mDanmakuView.isPrepared()) {
                mDanmakuView.seekTo((long) 0 - topBarView.getDanmuSettingView().getDanmuExtraTime());
                mDanmakuView.pause();
            }
            mIsPlayComplete = false;
        }
        //已加载字幕，则播放字幕
        if (topBarView.getSubtitleSettingView().isLoadSubtitle())
            mSubtitleView.start();
    }

    /**
     * 暂停
     */
    public void pause() {
        bottomBarView.setPlayIvStatus(false);
        if (mVideoView.isPlaying()) {
            mVideoView.pause();
        }
        if (topBarView.getSubtitleSettingView().isLoadSubtitle())
            mSubtitleView.pause();
        _pauseDanmaku();
    }

    /**
     * 停止
     */
    public void stop() {
        pause();
        // 记录播放进度
        if (mOutsideListener != null)
            mOutsideListener.onAction(Constants.INTENT_SAVE_CURRENT, mVideoView.getCurrentPosition());
        mVideoView.stopPlayback();
    }

    /**
     * 跳转
     */
    public void seekTo(long position) {
        Long positionLong = position;
        mVideoView.seekTo(positionLong.intValue());
        if(position != 0)
            mDanmakuTargetPosition = position;
    }

    /**
     * 设置控制栏显示或隐藏
     */
    private void _setControlBarVisible(boolean isShowBar) {
        if (mIsForbidTouch) {
            hideShowLockScreen(isShowBar);
        } else {
            if(isShowBar){
                // 只在显示控制栏的时候才设置时间，因为控制栏通常不显示且单位为分钟，所以不做实时更新
                topBarView.updateSystemTime();
                hideShowBar(true);
                hideShowLockScreen(true);
            }else {
                hideView(HIDE_VIEW_AUTO);
            }
        }
    }

    /**
     * 开关控制栏，单击界面的时候
     */
    private void _toggleControlBar() {
        mIsShowBar = !mIsShowBar;
        _setControlBarVisible(mIsShowBar);
        if (mIsShowBar) {
            // 发送延迟隐藏控制栏的操作
            mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIMEOUT);
            // 发送更新 Seek 消息
            mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        }
    }

    /**
     * 显示控制栏
     */
    private void _showControlBar(int timeout) {
        if (!mIsShowBar) {
            _setProgress();
            mIsShowBar = true;
        }
        _setControlBarVisible(true);
        mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
        // 先移除隐藏控制栏 Runnable，如果 timeout=0 则不做延迟隐藏操作
        mHandler.removeCallbacks(mHideBarRunnable);
        if (timeout != 0) {
            mHandler.postDelayed(mHideBarRunnable, timeout);
        }
    }

    /**
     * 切换播放状态，点击播放按钮时
     */
    private void _togglePlayStatus() {
        if (mVideoView.isPlaying()) {
            pause();
        } else {
            start();
        }
    }

    /**
     * 刷新隐藏控制栏的操作
     */
    private void _refreshHideRunnable() {
        mHandler.removeCallbacks(mHideBarRunnable);
        mHandler.postDelayed(mHideBarRunnable, DEFAULT_HIDE_TIMEOUT);
    }

    /**
     * 切换控制锁
     */
    private void _togglePlayerLock() {
        mIsForbidTouch = !mIsForbidTouch;
        mIvPlayerLock.setSelected(mIsForbidTouch);
        if (mIsForbidTouch) {
            setOrientationEnable(false);
            hideView(HIDE_VIEW_LOCK_SCREEN);
        } else {
            if (topBarView.getPlayerSettingView().isAllowScreenOrientation()) {
                setOrientationEnable(true);
            }
            hideShowBar(true);
        }
    }

    /**
     * 当屏幕执行翻转操作后调用禁止翻转功能，延迟3000ms再使能翻转，避免不必要的翻转
     */
    private void _refreshOrientationEnable() {
        if (topBarView.getPlayerSettingView().isAllowScreenOrientation()) {
            setOrientationEnable(false);
            mHandler.removeMessages(MSG_ENABLE_ORIENTATION);
            mHandler.sendEmptyMessageDelayed(MSG_ENABLE_ORIENTATION, 3000);
        }
    }

    /**
     * 更新进度条
     */
    private long _setProgress() {
        if (mVideoView == null || mIsSeeking) {
            return 0;
        }
        // 视频播放的当前进度
        long position = mVideoView.getCurrentPosition();
        // 视频总的时长
        long duration = mVideoView.getDuration();
        if (duration > 0) {
            // 转换为 Seek 显示的进度值
            long pos = (long) MAX_VIDEO_SEEK * position / duration;
            bottomBarView.setSeekProgress((int) pos);
        }
        // 获取缓冲的进度百分比，并显示在 Seek 的次进度
        int percent = mVideoView.getBufferPercentage();
        bottomBarView.setSeekSecondaryProgress(percent * 10);
        // 更新播放时间
        bottomBarView.setEndTime(generateTime(duration));
        bottomBarView.setCurTime(generateTime(position));
        // 返回当前播放进度
        return position;
    }

    /**
     * 设置跳转时间提示
     */
    private void setSkipTimeText(String time) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mSkipTimeTv.getVisibility() == View.GONE) {
            mSkipTimeTv.setVisibility(View.VISIBLE);
        }
        mSkipTimeTv.setText(time);
    }

    /**
     * 快进或者快退滑动改变进度，这里处理触摸滑动不是拉动 SeekBar
     */
    private void _onProgressSlide(float percent) {
        long position = mVideoView.getCurrentPosition();
        long duration = mVideoView.getDuration();
        // 单次拖拽最大时间差为100秒或播放时长的1/2
        long deltaMax = Math.min(100 * 1000, duration / 2);
        // 计算滑动时间
        long delta = (long) (deltaMax * percent);
        // 目标位置
        mTargetPosition = delta + position;
        if (mTargetPosition > duration) {
            mTargetPosition = duration;
        } else if (mTargetPosition <= 0) {
            mTargetPosition = 0;
        }
        int deltaTime = (int) ((mTargetPosition - position) / 1000);
        String desc;
        // 对比当前位置来显示快进或后退
        if (mTargetPosition > position) {
            desc = generateTime(mTargetPosition) + "/" + generateTime(duration) + "\n" + "+" + deltaTime + "秒";
        } else {
            desc = generateTime(mTargetPosition) + "/" + generateTime(duration) + "\n" + deltaTime + "秒";
        }
        setSkipTimeText(desc);
    }

    /**
     * 设置声音控制显示
     */
    @SuppressLint("SetTextI18n")
    private void _setVolumeInfo(int volume) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvVolume.getVisibility() == View.GONE) {
            mTvVolume.setVisibility(View.VISIBLE);
        }
        mTvVolume.setText((volume * 100 / mMaxVolume) + "%");
    }

    /**
     * 屏幕滑动改变声音大小
     */
    private void _onVolumeSlide(float percent) {
        if (mCurVolume == INVALID_VALUE) {
            mCurVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (mCurVolume < 0) {
                mCurVolume = 0;
            }
        }
        int index = (int) (percent * mMaxVolume) + mCurVolume;
        if (index > mMaxVolume) {
            index = mMaxVolume;
        } else if (index < 0) {
            index = 0;
        }
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, index, 0);
        // 变更进度条
        _setVolumeInfo(index);
    }


    /**
     * 音量键，递增或递减音量，量度按最大音量的 1/15
     */
    private void _setVolume(boolean isIncrease) {
        int curVolume = mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
        if (isIncrease) {
            curVolume += mMaxVolume / 15;
        } else {
            curVolume -= mMaxVolume / 15;
        }
        if (curVolume > mMaxVolume) {
            curVolume = mMaxVolume;
        } else if (curVolume < 0) {
            curVolume = 0;
        }
        // 变更声音
        mAudioManager.setStreamVolume(AudioManager.STREAM_MUSIC, curVolume, 0);
        // 变更进度条
        _setVolumeInfo(curVolume);
        mHandler.removeCallbacks(mHideTouchViewRunnable);
        mHandler.postDelayed(mHideTouchViewRunnable, 1000);
    }

    /**
     * 设置亮度控制显示
     */
    @SuppressLint("SetTextI18n")
    private void _setBrightnessInfo(float brightness) {
        if (mFlTouchLayout.getVisibility() == View.GONE) {
            mFlTouchLayout.setVisibility(View.VISIBLE);
        }
        if (mTvBrightness.getVisibility() == View.GONE) {
            mTvBrightness.setVisibility(View.VISIBLE);
        }
        mTvBrightness.setText(Math.ceil(brightness * 100) + "%");
    }

    /**
     * 滑动改变亮度大小
     */
    private void _onBrightnessSlide(float percent) {
        if (mCurBrightness < 0) {
            mCurBrightness = mAttachActivity.getWindow().getAttributes().screenBrightness;
            if (mCurBrightness < 0.0f) {
                mCurBrightness = 0.5f;
            } else if (mCurBrightness < 0.01f) {
                mCurBrightness = 0.01f;
            }
        }
        WindowManager.LayoutParams attributes = mAttachActivity.getWindow().getAttributes();
        attributes.screenBrightness = mCurBrightness + percent;
        if (attributes.screenBrightness > 1.0f) {
            attributes.screenBrightness = 1.0f;
        } else if (attributes.screenBrightness < 0.01f) {
            attributes.screenBrightness = 0.01f;
        }
        _setBrightnessInfo(attributes.screenBrightness);
        mAttachActivity.getWindow().setAttributes(attributes);
    }

    /**
     * 手势结束调用
     */
    private void _endGesture() {
        if (mTargetPosition >= 0 && mTargetPosition != mVideoView.getCurrentPosition() && mVideoView.getDuration() != 0) {
            // 更新视频播放进度
            seekTo((int) mTargetPosition);
            bottomBarView.setSeekProgress((int) (mTargetPosition * MAX_VIDEO_SEEK / mVideoView.getDuration()));
            mTargetPosition = INVALID_VALUE;
        }
        // 隐藏触摸操作显示图像
        hideView(HIDE_VIEW_END_GESTURE);
        _refreshHideRunnable();
        mCurVolume = INVALID_VALUE;
        mCurBrightness = INVALID_VALUE;
    }

    /**
     * 视频播放状态处理
     */
    private void _switchStatus(int status) {
        switch (status) {
            case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                mIsIjkPlayerReady = false;
                _pauseDanmaku();
                if (!mIsNeverPlay) {
                    mLoadingView.setVisibility(View.VISIBLE);
                }
            case MediaPlayerParams.STATE_PREPARING:
                break;

            case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                mIsRenderingStart = true;
            case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                mIsIjkPlayerReady = true;
                mLoadingView.setVisibility(View.GONE);
                // 更新进度
                mHandler.sendEmptyMessage(MSG_UPDATE_SEEK);
                if (mSkipPosition != INVALID_VALUE) {
                    _showSkipTip(); // 显示跳转提示
                }
                if (mVideoView.isPlaying()) {
                    _resumeDanmaku();   // 开启弹幕
                }
                break;

            case MediaPlayerParams.STATE_PLAYING:
                if (mIsRenderingStart && mIsIjkPlayerReady) {
                    _resumeDanmaku();   // 开启弹幕
                }
                break;
            case MediaPlayerParams.STATE_ERROR:
                _pauseDanmaku();
                break;
            case MediaPlayerParams.STATE_COMPLETED:
                pause();
                mIsPlayComplete = true;
                break;
        }
    }

    /**
     * 显示跳转提示
     */
    private void _showSkipTip() {
        if (mSkipPosition != INVALID_VALUE && skipTipView.getVisibility() == GONE) {
            skipTipView.setVisibility(VISIBLE);
            skipTipView.setSkipTime(generateTime(mSkipPosition));
            AnimHelper.doSlide(skipTipView, mWidthPixels, 0, 800);
            mHandler.postDelayed(mHideSkipTipRunnable, DEFAULT_HIDE_TIMEOUT * 3);
        }
    }

    /**
     * 隐藏跳转提示
     */
    private void _hideSkipTip() {
        if (skipTipView.getVisibility() == GONE) {
            return;
        }
        ViewCompat.animate(skipTipView).translationX(-skipTipView.getWidth()).alpha(0).setDuration(500)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        skipTipView.setVisibility(GONE);
                    }
                }).start();
        mSkipPosition = INVALID_VALUE;
    }

    /**
     * 显示选取字幕提示
     */
    private void _showSkipSub(int size) {
        if (skipSubView.getVisibility() == GONE) {
            skipSubView.setVisibility(VISIBLE);
            skipSubView.setSkipContent(size);
            AnimHelper.doSlide(skipSubView, mWidthPixels, 0, 800);
            mHandler.postDelayed(mHideSkipSubRunnable, DEFAULT_HIDE_TIMEOUT * 3);
        }
    }

    /**
     * 隐藏选取字幕提示
     */
    private void _hideSkipSub() {
        if (skipSubView.getVisibility() == GONE) {
            return;
        }
        ViewCompat.animate(skipSubView).translationX(-skipSubView.getWidth()).alpha(0).setDuration(500)
                .setListener(new ViewPropertyAnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(View view) {
                        skipSubView.setVisibility(GONE);
                    }
                }).start();
    }

    /**
     * 发射弹幕
     */
    public void sendDanmaku(String text, float size, int type, int color) {
        BaseDanmaku danmaku = mDanmakuContext.mDanmakuFactory.createDanmaku(type);
        if (danmaku == null || mDanmakuView == null) {
            return;
        }
        danmaku.text = text;
        danmaku.padding = 5;
        danmaku.isLive = false;
        danmaku.priority = 0;  // 可能会被各种过滤器过滤并隐藏显示
        danmaku.textSize = size;
        danmaku.textColor = color;
        danmaku.underlineColor = Color.GREEN;
        danmaku.setTime(mDanmakuView.getCurrentTime() + 500);
        mDanmakuView.addDanmaku(danmaku);

        if (mDanmakuListener != null) {
            mDanmakuListener.onDataObtain(danmaku);
        }
    }

    /**
     * 控制所有View的隐藏
     */
    private void hideView(int hideType){
        //亮度、声音、跳转，仅手势滑动结束后
        if (hideType == HIDE_VIEW_END_GESTURE){
            if (mFlTouchLayout.getVisibility() == VISIBLE){
                mFlTouchLayout.setVisibility(GONE);
                mSkipTimeTv.setVisibility(View.GONE);
                mTvVolume.setVisibility(View.GONE);
                mTvBrightness.setVisibility(View.GONE);
            }
            return;
        }

        //顶部控制栏
        if (topBarView.getTopBarVisibility() == VISIBLE && hideType != HIDE_VIEW_EDIT){
            hideShowBar(false);
        }
        //锁屏
        if (mIvPlayerLock.getVisibility() == VISIBLE && hideType != HIDE_VIEW_LOCK_SCREEN && hideType != HIDE_VIEW_EDIT){
            hideShowLockScreen(false);
        }
        //三个设置
        if (topBarView.isItemShowing() && hideType != HIDE_VIEW_AUTO){
            topBarView.hideItemView();
        }
        //弹幕屏蔽
        if (danmuBlockView.getVisibility() == VISIBLE && hideType != HIDE_VIEW_AUTO){
            danmuBlockView.setVisibility(GONE);
        }
        //发送弹幕
        if (danmuPostView.getVisibility() == VISIBLE && hideType != HIDE_VIEW_AUTO){
            danmuPostView.setVisibility(GONE);
        }
    }

    /**
     * 是否有不可自动隐藏的view正在显示
     */
    private boolean isEditViewVisible(){
        return topBarView.isItemShowing() ||
                danmuBlockView.getVisibility() == VISIBLE ||
                danmuPostView.getVisibility() == VISIBLE;
    }

    /**
     * 顶栏和底栏以及截图键显示与隐藏
     */
    private void hideShowBar(boolean isShow){
        if (isShow){
            AnimHelper.viewTranslationY(bottomBarView, 0);
            bottomBarView.setVisibility(View.VISIBLE);
            topBarView.setTopBarVisibility(true);
            mIsShowBar = true;
        }else {
            AnimHelper.viewTranslationY(bottomBarView, bottomBarView.getHeight());
            topBarView.setTopBarVisibility(false);
            mIsShowBar = false;
        }
        //截图键与控制栏的显示与隐藏是绑定的
        if (isShow){
            AnimHelper.viewTranslationX(mIvScreenShot, 0,300);
        }else {
            AnimHelper.viewTranslationX(mIvScreenShot, ConvertUtils.dp2px(60),300);
        }
    }

    /**
     * 锁屏键的显示与隐藏
     */
    private void hideShowLockScreen(boolean isShow){
        if (isShow){
            AnimHelper.viewTranslationX(mIvPlayerLock, 0,300);
        }else {
            AnimHelper.viewTranslationX(mIvPlayerLock, -ConvertUtils.dp2px(60),300);
        }
    }

    /**
     * 激活弹幕
     */
    private void _resumeDanmaku() {
        if (mDanmakuView != null && mDanmakuView.isPrepared() && mDanmakuView.isPaused()) {
            if (mDanmakuTargetPosition != INVALID_VALUE) {
                mDanmakuView.seekTo(mDanmakuTargetPosition - topBarView.getDanmuSettingView().getDanmuExtraTime());
                mDanmakuTargetPosition = INVALID_VALUE;
            } else {
                mDanmakuView.resume();
            }
        }
    }

    /**
     * 暂停弹幕
     */
    private void _pauseDanmaku() {
        if (mDanmakuView != null && mDanmakuView.isPrepared()) {
            mDanmakuView.pause();
        }
    }

    /**
     * 默认加载视频同名字幕
     */
    public void loadDefaultSubtitle(String videoPath){
        if (!isAutoLoadLocalSubtitle) return;
        String subtitlePath = CommonPlayerUtils.getSubtitlePath(videoPath);
        if (!StringUtils.isEmpty(subtitlePath)){
            //找到本地同名字幕，不自动加载网络字幕
            isAutoLoadNetworkSubtitle = false;
            setSubtitlePath(subtitlePath);
        }
    }

    /**
     * 是否启用旋屏
     */
    private void setOrientationEnable(boolean isEnable){
        if (topBarView.getPlayerSettingView() != null)
            topBarView.getPlayerSettingView().setOrientationChangeEnable(isEnable);
    }
}
