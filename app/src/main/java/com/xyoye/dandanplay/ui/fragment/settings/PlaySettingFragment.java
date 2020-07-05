package com.xyoye.dandanplay.ui.fragment.settings;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.PreferenceDataStore;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.activities.personal.PlayerSettingTipsActivity;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.Constants;

public class PlaySettingFragment extends BaseSettingsFragment {

    static final String[] pixelFormatArray = new String[]{
            "默认格式",
            "RGB 565",
            "RGB 888",
            "RGBX 8888",
            "YV12",
            "OpenGL ES2"
    };

    static final String[] pixelFormatArrayValue = new String[]{
            Constants.PlayerConfig.PIXEL_AUTO,
            Constants.PlayerConfig.PIXEL_RGB565,
            Constants.PlayerConfig.PIXEL_RGB888,
            Constants.PlayerConfig.PIXEL_RGBX8888,
            Constants.PlayerConfig.PIXEL_YV12,
            Constants.PlayerConfig.PIXEL_OPENGL_ES2
    };

    static final String[] playerTypeArray = new String[]{
            "IJK Player",
            "EXO Player",
            "Android Media Player"
    };

    static final String[] playerTypeValue = new String[]{
            String.valueOf(com.xyoye.player.commom.utils.Constants.IJK_PLAYER),
            String.valueOf(com.xyoye.player.commom.utils.Constants.EXO_PLAYER),
            String.valueOf(com.xyoye.player.commom.utils.Constants.IJK_ANDROID_PLAYER)
    };

    private final String[] keySetOfInvisiblePreferenceOnExoPlayerEnable = new String[]{
            "media_code_c",
            "media_code_c_h265",
            "open_sl_es",
            "pixel_format_type"
    };

    private final PlaySettingDataStore dataStore = new PlaySettingDataStore();

    @Override
    String getTitle() {
        return "播放器设置";
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setPreferenceDataStore(dataStore);
        addPreferencesFromResource(R.xml.settings_play);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        //设置顶部工具栏的帮助菜单
        Toolbar toolbar = getToolbar();
        if (toolbar != null) {
            toolbar.inflateMenu(R.menu.menu_player_setting_tips);
            toolbar.setOnMenuItemClickListener(item -> {
                switch (item.getItemId()) {
                    case R.id.setting_tips:
                        startActivity(new Intent(toolbar.getContext(), PlayerSettingTipsActivity.class));
                        break;
                }
                return true;
            });
        }

        //绑定列表选择项
        ListPreference pixelFormat = (ListPreference) findPreference("pixel_format_type");
        pixelFormat.setEntries(pixelFormatArray);
        pixelFormat.setEntryValues(pixelFormatArrayValue);
        pixelFormat.setSummary(pixelFormat.getEntry());
        pixelFormat.setOnPreferenceChangeListener((preference, o) -> {
            for (int i = 0; i < pixelFormatArrayValue.length; i++) {
                if (pixelFormatArrayValue[i].equals(o)) {
                    pixelFormat.setSummary(pixelFormatArray[i]);
                }
            }
            return true;
        });

        ListPreference playerType = (ListPreference) findPreference("player_type");
        playerType.setEntries(playerTypeArray);
        playerType.setEntryValues(playerTypeValue);
        playerType.setSummary(playerType.getEntry());
        playerType.setOnPreferenceChangeListener(((preference, o) -> {
            Log.e("playerType", "source: " + playerType.getValue() + " new: " + o);
            for (int i = 0; i < playerTypeValue.length; i++) {
                if (playerTypeValue[i].equals(o)) {
                    playerType.setSummary(playerTypeArray[i]);
                }
            }
            boolean isExoPlayer = o.equals(String.valueOf(com.xyoye.player.commom.utils.Constants.EXO_PLAYER));
            for (String key : keySetOfInvisiblePreferenceOnExoPlayerEnable) {
                findPreference(key).setVisible(!isExoPlayer);
            }
            return true;
        }));
        boolean isExoPlayer = playerType.getValue().equals(String.valueOf(com.xyoye.player.commom.utils.Constants.EXO_PLAYER));
        for (String key : keySetOfInvisiblePreferenceOnExoPlayerEnable) {
            findPreference(key).setVisible(!isExoPlayer);
        }

        super.onViewCreated(view, savedInstanceState);
    }

    static class PlaySettingDataStore extends PreferenceDataStore {
        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            switch (key) {
                case "pixel_format_type":
                    return AppConfig.getInstance().getPixelFormat();
                case "player_type":
                    return String.valueOf(AppConfig.getInstance().getPlayerType());
            }
            return super.getString(key, defValue);
        }

        @Override
        public void putString(String key, @Nullable String value) {
            switch (key) {
                case "pixel_format_type":
                    for (String type : pixelFormatArrayValue) {
                        if (type.equals(value)) {
                            AppConfig.getInstance().setPixelFormat(value);
                        }
                    }
                    break;
                case "player_type":
                    for (String type : playerTypeValue) {
                        if (type.equals(value)) {
                            AppConfig.getInstance().setPlayerType(Integer.parseInt(type));
                        }
                    }
                    break;
            }
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            switch (key) {
                case "media_code_c":
                    return AppConfig.getInstance().isOpenMediaCodeC();
                case "media_code_c_h265":
                    return AppConfig.getInstance().isOpenMediaCodeCH265();
                case "open_sl_es":
                    return AppConfig.getInstance().isOpenSLES();
                case "surface_renders":
                    return AppConfig.getInstance().isSurfaceRenders();
                case "auto_load_danmu":
                    return AppConfig.getInstance().isAutoLoadDanmu();
                case "danmu_cloud_block":
                    return AppConfig.getInstance().isCloudDanmuFilter();
                case "outer_chain_danmu":
                    return AppConfig.getInstance().isOuterChainDanmuSelect();
                case "online_play_log":
                    return AppConfig.getInstance().isOnlinePlayLogEnable();
                case "auto_load_local_subtitle":
                    return AppConfig.getInstance().isAutoLoadLocalSubtitle();
                case "auto_load_network_subtitle":
                    return AppConfig.getInstance().isAutoLoadNetworkSubtitle();
                case "network_subtitle":
                    return AppConfig.getInstance().isUseNetWorkSubtitle();
            }
            return super.getBoolean(key, defValue);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            switch (key) {
                case "media_code_c":
                    AppConfig.getInstance().setOpenMediaCodeC(value);
                    break;
                case "media_code_c_h265":
                    AppConfig.getInstance().setOpenMediaCodeCH265(value);
                    break;
                case "open_sl_es":
                    AppConfig.getInstance().setOpenSLES(value);
                    break;
                case "surface_renders":
                    AppConfig.getInstance().setSurfaceRenders(value);
                    break;
                case "auto_load_danmu":
                    AppConfig.getInstance().setAutoLoadDanmu(value);
                    break;
                case "danmu_cloud_block":
                    AppConfig.getInstance().setCloudDanmuFilter(value);
                    break;
                case "outer_chain_danmu":
                    AppConfig.getInstance().setOuterChainDanmuSelect(value);
                    break;
                case "online_play_log":
                    AppConfig.getInstance().setOnlinePlayLogEnable(value);
                    break;
                case "auto_load_local_subtitle":
                    AppConfig.getInstance().setAutoLoadLocalSubtitle(value);
                    break;
                case "auto_load_network_subtitle":
                    AppConfig.getInstance().setAutoLoadNetworkSubtitle(value);
                    break;
                case "network_subtitle":
                    AppConfig.getInstance().setUseNetWorkSubtitle(value);
                    break;
            }
        }
    }
}
