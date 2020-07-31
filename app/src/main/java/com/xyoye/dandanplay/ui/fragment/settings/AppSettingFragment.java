package com.xyoye.dandanplay.ui.fragment.settings;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.preference.Preference;
import androidx.preference.PreferenceDataStore;

import com.blankj.utilcode.util.ToastUtils;
import com.taobao.sophix.SophixManager;
import com.tencent.bugly.beta.Beta;
import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.bean.event.PatchFixEvent;
import com.xyoye.dandanplay.ui.activities.WebViewActivity;
import com.xyoye.dandanplay.ui.activities.personal.FeedbackActivity;
import com.xyoye.dandanplay.ui.weight.dialog.FileManagerDialog;
import com.xyoye.dandanplay.ui.weight.dialog.PatchHisDialog;
import com.xyoye.dandanplay.ui.weight.preference.LongClickPreference;
import com.xyoye.dandanplay.utils.AppConfig;
import com.xyoye.dandanplay.utils.CommonUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class AppSettingFragment extends BaseSettingsFragment {
    private final AppSettingDataStore dataStore = new AppSettingDataStore();
    private Context mAttachContext;

    @Override
    public String getTitle() {
        return "系统设置";
    }

    @Override
    public void onAttach(Context context) {
        this.mAttachContext = context;
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
        EventBus.getDefault().register(this);

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

        LongClickPreference patch = (LongClickPreference) findPreference("patch");
        patch.setSummary(AppConfig.getInstance().getPatchVersion() + "");
        patch.setOnPreferenceLongClickListener(preference -> {
            new PatchHisDialog(mAttachContext, R.style.Dialog).show();
            return true;
        });
        patch.setOnPreferenceClickListener(preference -> {
            SophixManager.getInstance().queryAndLoadNewPatch();
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

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPatchEvent(PatchFixEvent event) {
        ToastUtils.showShort(event.getMsg());
        Preference preference = findPreference("path");
        if (preference != null) {
            preference.setSummary(AppConfig.getInstance().getPatchVersion() + "");
        }
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
