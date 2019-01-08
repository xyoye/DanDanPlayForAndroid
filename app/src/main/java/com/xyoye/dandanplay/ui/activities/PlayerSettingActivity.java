package com.xyoye.dandanplay.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.PlayerSettingEvent;
import com.xyoye.dandanplay.ui.weight.dialog.PlayerSettingDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by xyy on 2018/9/29.
 */

public class PlayerSettingActivity extends AppCompatActivity {

    @BindView(R.id.player_type_tv)
    TextView playerTypeTv;
    @BindView(R.id.select_player_type_ll)
    LinearLayout selectPlayerTypeLl;
    @BindView(R.id.media_code_c_cb)
    CheckBox mediaCodeCCb;
    @BindView(R.id.open_sles_cb)
    CheckBox openSlesCb;
    @BindView(R.id.media_code_c_h265_cb)
    CheckBox mediaCodeCH265Cb;
    @BindView(R.id.surface_renders_cb)
    CheckBox surfaceRendersCb;
    @BindView(R.id.pixel_format_tv)
    TextView pixelFormatTv;
    @BindView(R.id.select_pixel_format_ll)
    LinearLayout selectPixelFormatLl;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.outer_china_danmu_cb)
    CheckBox outerChinaDanmuCb;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setting);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.theme_color), 0);

        setTitle("播放器设置");

        initView();

        initListener();
    }

    private void initView() {
        int playerType = AppConfig.getInstance().getPlayerType();
        switch (playerType) {
            case com.player.ijkplayer.utils.Constants.IJK_EXO_PLAYER:
                playerTypeTv.setText("IJK_EXO Player");
                break;
            case com.player.ijkplayer.utils.Constants.IJK_ANDROID_PLAYER:
                playerTypeTv.setText("AndroidMedia Player");
                break;
            case com.player.ijkplayer.utils.Constants.IJK_PLAYER:
            default:
                playerTypeTv.setText("IJK Player");
                break;
        }
        String pixelType = AppConfig.getInstance().getPixelFormat();
        switch (pixelType) {
            case Constants.PIXEL_RGB565:
                pixelFormatTv.setText("RGB 565");
                break;
            case Constants.PIXEL_RGB888:
                pixelFormatTv.setText("RGB 888");
                break;
            case Constants.PIXEL_RGBX8888:
                pixelFormatTv.setText("RGBX 8888");
                break;
            case Constants.PIXEL_YV12:
                pixelFormatTv.setText("YV12");
                break;
            case Constants.PIXEL_OPENGL_ES2:
                pixelFormatTv.setText("OpenGL ES2");
                break;
            case Constants.PIXEL_AUTO:
            default:
                pixelFormatTv.setText("默认");
                break;
        }
        boolean mediaCodeC = AppConfig.getInstance().isOpenMediaCodeC();
        boolean mediaCodeCH265 = AppConfig.getInstance().isOpenMediaCodeCH265();
        boolean openSLES = AppConfig.getInstance().isOpenSLES();
        boolean surfaceRenders = AppConfig.getInstance().isSurfaceRenders();
        boolean outerChinaDialog = AppConfig.getInstance().isShowOuterChainDanmuDialog();
        mediaCodeCCb.setChecked(mediaCodeC);
        mediaCodeCH265Cb.setChecked(mediaCodeCH265);
        openSlesCb.setChecked(openSLES);
        surfaceRendersCb.setChecked(surfaceRenders);
        outerChinaDanmuCb.setChecked(outerChinaDialog);
    }

    private void initListener() {
        mediaCodeCCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setOpenMediaCodeC(isChecked);
        });
        mediaCodeCH265Cb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setOpenMediaCodeCH265(isChecked);
        });
        openSlesCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setOpenSLES(isChecked);

        });
        surfaceRendersCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setSurfaceRenders(isChecked);
        });
        outerChinaDanmuCb.setOnCheckedChangeListener((buttonView, isChecked) -> {
            AppConfig.getInstance().setShowOuterChainDanmuDialog(isChecked);
        });
    }

    @OnClick({R.id.select_player_type_ll, R.id.select_pixel_format_ll})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.select_player_type_ll:
                new PlayerSettingDialog(PlayerSettingActivity.this, R.style.Dialog, true).show();
                break;
            case R.id.select_pixel_format_ll:
                new PlayerSettingDialog(PlayerSettingActivity.this, R.style.Dialog, false).show();
                break;
        }
    }

    @Subscribe
    public void onEvent(PlayerSettingEvent event) {
        if (event.isPlayer()) {
            playerTypeTv.setText(event.getName());
        } else {
            pixelFormatTv.setText(event.getName());
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }
}
