package com.player.ijkplayer.widgets;

import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.player.ijkplayer.R;
import com.player.ijkplayer.receiver.BatteryBroadcastReceiver;
import com.player.ijkplayer.utils.AnimHelper;
import com.player.ijkplayer.utils.TimeFormatUtils;

/**
 * Created by xyoye on 2019/4/29.
 */

public class TopBarView extends FrameLayout implements View.OnClickListener{
    private LinearLayout topBarLL;
    private ImageView backIv;
    private MarqueeTextView titleTv;
    private ProgressBar batteryBar;
    private TextView systemTimeTv;
    private ImageView subtitleSettingIv;
    private TextView danmuSettingTv;
    private ImageView playerSettingIv;

    private SettingVideoView playerSettingView;
    private SettingDanmuView danmuSettingView;
    private SettingSubtitleView subtitleSettingView;

    private TopBarListener listener;

    public TopBarView(Context context) {
        super(context);
    }

    public TopBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        View.inflate(context, R.layout.layout_top_bar_v2, this);

        topBarLL = this.findViewById(R.id.top_bar_ll);
        backIv = this.findViewById(R.id.iv_back);
        titleTv = this.findViewById(R.id.tv_title);
        batteryBar = this.findViewById(R.id.pb_battery);
        systemTimeTv = this.findViewById(R.id.tv_system_time);
        subtitleSettingIv = this.findViewById(R.id.subtitle_settings_iv);
        danmuSettingTv = this.findViewById(R.id.danmu_settings_tv);
        playerSettingIv = this.findViewById(R.id.player_settings_iv);

        playerSettingView = this.findViewById(R.id.player_setting_view);
        danmuSettingView = this.findViewById(R.id.danmu_setting_view);
        subtitleSettingView = this.findViewById(R.id.subtitle_setting_view);

        backIv.setOnClickListener(this);
        subtitleSettingIv.setOnClickListener(this);
        danmuSettingTv.setOnClickListener(this);
        playerSettingIv.setOnClickListener(this);

        updateSystemTime();
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.iv_back){
            listener.onBack();
        }else if (id == R.id.subtitle_settings_iv){
            AnimHelper.viewTranslationX(subtitleSettingView, 0);
            listener.topBarItemClick();
        }else if (id == R.id.danmu_settings_tv){
            AnimHelper.viewTranslationX(danmuSettingView, 0);
            listener.topBarItemClick();
        }else if (id == R.id.player_settings_iv){
            AnimHelper.viewTranslationX(playerSettingView, 0);
            listener.topBarItemClick();
        }
    }

    /**
     * 回调接口
     */
    public void setSettingListener(TopBarListener listener){
        this.listener = listener;
    }

    /**
     * 电量改变
     */
    public void setBatteryChanged(int status, int progress){
        if (status == BatteryBroadcastReceiver.BATTERY_STATUS_SPA) {
            batteryBar.setSecondaryProgress(0);
            batteryBar.setProgress(progress);
            batteryBar.setBackgroundResource(R.mipmap.ic_battery_charging);
        } else if (status == BatteryBroadcastReceiver.BATTERY_STATUS_LOW) {
            batteryBar.setSecondaryProgress(progress);
            batteryBar.setProgress(0);
            batteryBar.setBackgroundResource(R.mipmap.ic_battery_red);
        } else if (status == BatteryBroadcastReceiver.BATTERY_STATUS_NOR){
            batteryBar.setSecondaryProgress(0);
            batteryBar.setProgress(progress);
            batteryBar.setBackgroundResource(R.mipmap.ic_battery);
        }
    }

    /**
     * 更新时间
     */
    public void updateSystemTime(){
        systemTimeTv.setText(TimeFormatUtils.getCurFormatTime());
    }

    /**
     * 标题文字
     */
    public void setTitleText(String title){
        titleTv.setText(title);
    }

    /**
     * 顶栏显示状态
     */
    public void setTopBarVisibility(@PlayerNotificationManager.Visibility int visibility){
        topBarLL.setVisibility(visibility);
    }

    /**
     * 隐藏三个item布局
     */
    public void hideItemView(){
        if (playerSettingView.getTranslationX() == 0){
            AnimHelper.viewTranslationX(playerSettingView);
        }
        if (danmuSettingView.getTranslationX() == 0){
            AnimHelper.viewTranslationX(danmuSettingView);
        }
        if (subtitleSettingView.getTranslationX() == 0){
            AnimHelper.viewTranslationX(subtitleSettingView);
        }
    }

    /**
     * 判断是否有Item布局正在显示
     */
    public boolean isItemShowing() {
        return playerSettingView.getTranslationX() == 0 ||
                danmuSettingView.getTranslationX() == 0 ||
                subtitleSettingView.getTranslationX() == 0;
    }

    /**
     * 播放器设置view
     */
    public SettingVideoView getPlayerSettingView(){
        return playerSettingView;
    }

    /**
     * 弹幕设置view
     */
    public SettingDanmuView getDanmuSettingView(){
        return danmuSettingView;
    }

    /**
     * 字幕设置view
     */
    public SettingSubtitleView getSubtitleSettingView(){
        return subtitleSettingView;
    }

    public interface TopBarListener{
        void onBack();

        void topBarItemClick();
    }
}
