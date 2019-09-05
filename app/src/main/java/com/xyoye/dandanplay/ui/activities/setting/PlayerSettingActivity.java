package com.xyoye.dandanplay.ui.activities.setting;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;
import com.xyoye.dandanplay.ui.activities.personal.PlayerSettingTipsActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by xyoye on 2018/9/29.
 */

public class PlayerSettingActivity extends BaseMvcActivity {

    @BindView(R.id.player_type_tv)
    TextView playerTypeTv;
    @BindView(R.id.ijk_setting_ll)
    LinearLayout ijkSettingLL;
    @BindView(R.id.media_code_c_cb)
    CheckBox mediaCodeCCb;
    @BindView(R.id.open_sl_es_cb)
    CheckBox openSLESCb;
    @BindView(R.id.media_code_c_h265_cb)
    CheckBox mediaCodeCH265Cb;
    @BindView(R.id.surface_renders_cb)
    CheckBox surfaceRendersCb;
    @BindView(R.id.pixel_format_tv)
    TextView pixelFormatTv;
    @BindView(R.id.outer_china_danmu_cb)
    CheckBox outerChinaDanmuCb;
    @BindView(R.id.auto_load_danmu_cb)
    CheckBox autoLoadDanmuCb;
    @BindView(R.id.danmu_cloud_block_cb)
    CheckBox danmuCloudBlockCb;
    @BindView(R.id.network_subtitle_cb)
    CheckBox networkSubtitleCb;
    @BindView(R.id.auto_load_local_subtitle_cb)
    CheckBox autoLoadLocalSubtitleCb;
    @BindView(R.id.auto_load_network_subtitle_cb)
    CheckBox autoLoadNetworkSubtitleCb;
    @BindView(R.id.auto_load_network_subtitle_rl)
    RelativeLayout autoLoadNetworkSubtitleRl;

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_player_setting;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void initPageView() {
        setTitle("播放器设置");

        int playerType = AppConfig.getInstance().getPlayerType();
        switch (playerType) {
            case com.xyoye.player.commom.utils.Constants.EXO_PLAYER:
                playerTypeTv.setText("EXO Player");
                ijkSettingLL.setVisibility(View.GONE);
                break;
            case com.xyoye.player.commom.utils.Constants.IJK_ANDROID_PLAYER:
                playerTypeTv.setText("AndroidMedia Player");
                ijkSettingLL.setVisibility(View.VISIBLE);
                break;
            case com.xyoye.player.commom.utils.Constants.IJK_PLAYER:
            default:
                playerTypeTv.setText("IJK Player");
                ijkSettingLL.setVisibility(View.VISIBLE);
                break;
        }
        String pixelType = AppConfig.getInstance().getPixelFormat();
        switch (pixelType) {
            case Constants.PlayerConfig.PIXEL_RGB565:
                pixelFormatTv.setText("RGB 565");
                break;
            case Constants.PlayerConfig.PIXEL_RGB888:
                pixelFormatTv.setText("RGB 888");
                break;
            case Constants.PlayerConfig.PIXEL_RGBX8888:
                pixelFormatTv.setText("RGBX 8888");
                break;
            case Constants.PlayerConfig.PIXEL_YV12:
                pixelFormatTv.setText("YV12");
                break;
            case Constants.PlayerConfig.PIXEL_OPENGL_ES2:
                pixelFormatTv.setText("OpenGL ES2");
                break;
            case Constants.PlayerConfig.PIXEL_AUTO:
            default:
                pixelFormatTv.setText("默认");
                break;
        }
        boolean mediaCodeC = AppConfig.getInstance().isOpenMediaCodeC();
        boolean mediaCodeCH265 = AppConfig.getInstance().isOpenMediaCodeCH265();
        boolean openSLES = AppConfig.getInstance().isOpenSLES();
        boolean surfaceRenders = AppConfig.getInstance().isSurfaceRenders();
        boolean outerChinaDialog = AppConfig.getInstance().isShowOuterChainDanmuDialog();
        boolean autoLoadDanmu = AppConfig.getInstance().isAutoLoadDanmu();
        boolean danmuCloudBlock = AppConfig.getInstance().isCloudDanmuFilter();
        boolean useNetworkSubtitle = AppConfig.getInstance().isUseNetWorkSubtitle();
        boolean autoLoadLocalSubtitle = AppConfig.getInstance().isAutoLoadLocalSubtitle();
        boolean autoLoadNetworkSubtitle = AppConfig.getInstance().isAutoLoadNetworkSubtitle();
        mediaCodeCCb.setChecked(mediaCodeC);
        mediaCodeCH265Cb.setChecked(mediaCodeCH265);
        openSLESCb.setChecked(openSLES);
        surfaceRendersCb.setChecked(surfaceRenders);
        outerChinaDanmuCb.setChecked(outerChinaDialog);
        autoLoadDanmuCb.setChecked(autoLoadDanmu);
        danmuCloudBlockCb.setChecked(danmuCloudBlock);
        networkSubtitleCb.setChecked(useNetworkSubtitle);
        autoLoadLocalSubtitleCb.setChecked(autoLoadLocalSubtitle);
        autoLoadNetworkSubtitleCb.setChecked(autoLoadNetworkSubtitle);
        if (useNetworkSubtitle) {
            autoLoadNetworkSubtitleRl.setVisibility(View.VISIBLE);
        } else {
            autoLoadNetworkSubtitleRl.setVisibility(View.GONE);
        }
    }

    @Override
    public void initPageViewListener() {
        mediaCodeCCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setOpenMediaCodeC(isChecked));
        mediaCodeCH265Cb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setOpenMediaCodeCH265(isChecked));
        openSLESCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setOpenSLES(isChecked));
        surfaceRendersCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setSurfaceRenders(isChecked));
        outerChinaDanmuCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setShowOuterChainDanmuDialog(isChecked));
        autoLoadDanmuCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setAutoLoadDanmu(isChecked));
        danmuCloudBlockCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setCloudDanmuFilter(isChecked));
        autoLoadLocalSubtitleCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setAutoLoadLocalSubtitle(isChecked));
        autoLoadNetworkSubtitleCb.setOnCheckedChangeListener((buttonView, isChecked) ->
                AppConfig.getInstance().setAutoLoadNetworkSubtitle(isChecked));
        networkSubtitleCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setUseNetWorkSubtitle(isChecked);
            autoLoadNetworkSubtitleRl.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if (!isChecked) {
                autoLoadNetworkSubtitleCb.setChecked(false);
                AppConfig.getInstance().setAutoLoadNetworkSubtitle(false);
            }
        });
    }

    @OnClick({R.id.select_player_type_ll, R.id.select_pixel_format_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.select_player_type_ll:
                showSelectPlayerDialog();
                break;
            case R.id.select_pixel_format_ll:
                showSelectPixelDialog();
                break;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
            case R.id.setting_tips:
                startActivity(new Intent(PlayerSettingActivity.this, PlayerSettingTipsActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_player_setting_tips, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void showSelectPlayerDialog() {
        final String[] playTypes = {"IJK Player", "EXO Player", "AndroidMedia Player"};

        new AlertDialog.Builder(this)
                .setTitle("选择播放器")
                .setItems(playTypes, (dialog, which) -> {
                    if (which == 0) {
                        ijkSettingLL.setVisibility(View.VISIBLE);
                        AppConfig.getInstance().setPlayerType(com.xyoye.player.commom.utils.Constants.IJK_PLAYER);
                    } else if (which == 1) {
                        ijkSettingLL.setVisibility(View.GONE);
                        AppConfig.getInstance().setPlayerType(com.xyoye.player.commom.utils.Constants.EXO_PLAYER);
                    } else if (which == 3) {
                        ijkSettingLL.setVisibility(View.VISIBLE);
                        AppConfig.getInstance().setPlayerType(com.xyoye.player.commom.utils.Constants.IJK_ANDROID_PLAYER);
                    }
                    playerTypeTv.setText(playTypes[which]);
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void showSelectPixelDialog(){
        final String[] pixelTypes = {"默认格式", "RGB 565", "RGB 888", "RGBX 8888", "YV12", "OpenGL ES2"};

        new AlertDialog.Builder(this)
                .setTitle("选择像素格式")
                .setItems(pixelTypes, (dialog, which) -> {
                    switch (which){
                        case 0:
                            AppConfig.getInstance().setPixelFormat(Constants.PlayerConfig.PIXEL_AUTO);
                            break;
                        case 1:
                            AppConfig.getInstance().setPixelFormat(Constants.PlayerConfig.PIXEL_RGB565);
                            break;
                        case 2:
                            AppConfig.getInstance().setPixelFormat(Constants.PlayerConfig.PIXEL_RGB888);
                            break;
                        case 3:
                            AppConfig.getInstance().setPixelFormat(Constants.PlayerConfig.PIXEL_RGBX8888);
                            break;
                        case 4:
                            AppConfig.getInstance().setPixelFormat(Constants.PlayerConfig.PIXEL_YV12);
                            break;
                        case 5:
                            AppConfig.getInstance().setPixelFormat(Constants.PlayerConfig.PIXEL_OPENGL_ES2);
                            break;
                    }
                    playerTypeTv.setText(pixelTypes[which]);
                })
                .setNegativeButton("取消", null)
                .show();
    }
}
