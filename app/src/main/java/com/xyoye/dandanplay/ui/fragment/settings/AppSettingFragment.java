package com.xyoye.dandanplay.ui.fragment.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceDataStore;

import com.tencent.bugly.beta.Beta;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.activities.WebViewActivity;
import com.xyoye.dandanplay.ui.activities.personal.FeedbackActivity;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;

public class AppSettingFragment extends BaseSettingsFragment {
    private final AppSettingDataStore dataStore = new AppSettingDataStore();

    @Override
    public String getTitle() {
        return "系统设置";
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setPreferenceDataStore(dataStore);
        addPreferencesFromResource(R.xml.settings_app);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Preference cachePath = findPreference("cache_path");
        cachePath.setSummary(AppConfig.getInstance().getDownloadFolder());
        cachePath.setOnPreferenceClickListener(preference -> {
            new FileManagerDialog(view.getContext(), FileManagerDialog.SELECT_FOLDER, path -> {
                preference.setSummary(path);
                AppConfig.getInstance().setDownloadFolder(path);
            }).hideDefault().show();
            return true;
        });

        Preference version = findPreference("version");
        version.setSummary(CommonUtils.getAppVersion());
        version.setOnPreferenceClickListener(preference -> {
            Beta.checkUpgrade(false, false);
            return true;
        });

        findPreference("about").setOnPreferenceClickListener(preference -> {
            Intent intent_about = new Intent(view.getContext(), WebViewActivity.class);
            intent_about.putExtra("title", "关于我们");
            intent_about.putExtra("link", "file:///android_asset/DanDanPlay.html");
            startActivity(intent_about);
            return true;
        });

        findPreference("feedback").setOnPreferenceClickListener(preference -> {
            startActivity(new Intent(view.getContext(), FeedbackActivity.class));
            return true;
        });
    }


    static class AppSettingDataStore extends PreferenceDataStore {
        @SuppressWarnings("PMD.UncommentedEmptyMethodBody")
        @Override
        public void putString(String key, @Nullable String value) {

        }

        @Override
        public void putBoolean(String key, boolean value) {
            if (key.equals("close_splash_page")) {
                AppConfig.getInstance().setCloseSplashPage(value);
            }
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            if (key.equals("close_splash_page")) {
                return AppConfig.getInstance().isCloseSplashPage();
            }
            return super.getBoolean(key, defValue);
        }
    }
}
