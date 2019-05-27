package com.xyoye.dandanplay.ui.weight.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xyoye on 2018/9/29.
 */

public class PlayerSettingDialog extends Dialog {

    @BindView(R.id.dialog_title)
    TextView dialogTitle;
    @BindView(R.id.player_ijk_rb)
    RadioButton playerIjkRb;
    @BindView(R.id.player_exo_rb)
    RadioButton playerIjkExoRb;
    @BindView(R.id.player_android_rb)
    RadioButton playerAndroidRb;
    @BindView(R.id.player_group)
    RadioGroup playerGroup;
    @BindView(R.id.pixel_auto)
    RadioButton pixelAuto;
    @BindView(R.id.pixel_rgb565)
    RadioButton pixelRgb565;
    @BindView(R.id.pixel_rgb888)
    RadioButton pixelRgb888;
    @BindView(R.id.pixel_rgbx8888)
    RadioButton pixelRgbx8888;
    @BindView(R.id.pixel_yv12)
    RadioButton pixelYv12;
    @BindView(R.id.pixel_opengl_es2)
    RadioButton pixelOpenglEs2;
    @BindView(R.id.pixel_format_group)
    RadioGroup pixelFormatGroup;
    @BindView(R.id.select_layout)
    RelativeLayout selectLayout;
    @BindView(R.id.dialog_dismiss)
    TextView dialogDismiss;

    private boolean isSelectPlayer;
    private PlayerSettingSelectListener listener;

    public PlayerSettingDialog(@NonNull Context context, boolean isSelectPlayer, PlayerSettingSelectListener listener) {
        super(context,  R.style.Dialog);
        this.isSelectPlayer = isSelectPlayer;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_player_setting);
        ButterKnife.bind(this);

        int playerType = AppConfig.getInstance().getPlayerType();
        switch (playerType){
            case com.player.commom.utils.Constants.EXO_PLAYER:
                playerIjkExoRb.setChecked(true);
                break;
            case com.player.commom.utils.Constants.IJK_ANDROID_PLAYER:
                playerAndroidRb.setChecked(true);
                break;
            case com.player.commom.utils.Constants.IJK_PLAYER:
            default:
                playerIjkRb.setChecked(true);
                break;
        }
        String pixelType = AppConfig.getInstance().getPixelFormat();
        switch (pixelType){
            case Constants.PIXEL_RGB565:
                pixelRgb565.setChecked(true);
                break;
            case Constants.PIXEL_RGB888:
                pixelRgb888.setChecked(true);
                break;
            case Constants.PIXEL_RGBX8888:
                pixelRgbx8888.setChecked(true);
                break;
            case Constants.PIXEL_YV12:
                pixelYv12.setChecked(true);
                break;
            case Constants.PIXEL_OPENGL_ES2:
                pixelOpenglEs2.setChecked(true);
                break;
            case Constants.PIXEL_AUTO:
            default:
                pixelAuto.setChecked(true);
                break;
        }

        init();
    }

    private void init(){
        if (isSelectPlayer){
            dialogTitle.setText("选择播放器");
            playerGroup.setVisibility(View.VISIBLE);
            playerGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String player_name = "";
                switch (checkedId){
                    case R.id.player_ijk_rb:
                        player_name = "IJK Player";
                        AppConfig.getInstance().setPlayerType(com.player.commom.utils.Constants.IJK_PLAYER);
                        break;
                    case R.id.player_exo_rb:
                        player_name = "EXO Player";
                        AppConfig.getInstance().setPlayerType(com.player.commom.utils.Constants.EXO_PLAYER);
                        break;
                    case R.id.player_android_rb:
                        player_name = "AndroidMedia Player";
                        AppConfig.getInstance().setPlayerType(com.player.commom.utils.Constants.IJK_ANDROID_PLAYER);
                        break;
                }
                listener.onSelected(player_name);
                PlayerSettingDialog.this.dismiss();
            });
        }else {
            dialogTitle.setText("选择像素格式");
            pixelFormatGroup.setVisibility(View.VISIBLE);
            pixelFormatGroup.setOnCheckedChangeListener((group, checkedId) -> {
                String pixelType = "";
                switch (checkedId){
                    case R.id.pixel_auto:
                        pixelType = "默认";
                        AppConfig.getInstance().setPixelFormat(Constants.PIXEL_AUTO);
                        break;
                    case R.id.pixel_rgb565:
                        pixelType = "RGB 565";
                        AppConfig.getInstance().setPixelFormat(Constants.PIXEL_RGB565);
                        break;
                    case R.id.pixel_rgb888:
                        pixelType = "RGB 888";
                        AppConfig.getInstance().setPixelFormat(Constants.PIXEL_RGB888);
                        break;
                    case R.id.pixel_rgbx8888:
                        pixelType = "RGBX 8888";
                        AppConfig.getInstance().setPixelFormat(Constants.PIXEL_RGBX8888);
                        break;
                    case R.id.pixel_yv12:
                        pixelType = "YV12";
                        AppConfig.getInstance().setPixelFormat(Constants.PIXEL_YV12);
                        break;
                    case R.id.pixel_opengl_es2:
                        pixelType = "OpenGL ES2";
                        AppConfig.getInstance().setPixelFormat(Constants.PIXEL_OPENGL_ES2);
                        break;
                }
                listener.onSelected(pixelType);
                PlayerSettingDialog.this.dismiss();
            });
        }

        dialogDismiss.setOnClickListener(v -> PlayerSettingDialog.this.dismiss());
    }

    public interface PlayerSettingSelectListener{
        void onSelected(String result);
    }
}
