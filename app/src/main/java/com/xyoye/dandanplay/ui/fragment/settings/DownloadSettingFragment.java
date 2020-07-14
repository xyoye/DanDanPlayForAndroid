package com.xyoye.dandanplay.ui.fragment.settings;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.EditTextPreferenceDialogFragmentCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceDataStore;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.utils.CommonUtils;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentConfig;
import com.xyoye.dandanplay.utils.jlibtorrent.TorrentEngine;

public class DownloadSettingFragment extends BaseSettingsFragment {
    private final DownloadSettingsDataStore dataStore = new DownloadSettingsDataStore();

    private final String[] taskCountArray = new String[]{"1", "2", "3", "4", "5"};

    @Override
    public String getTitle() {
        return "下载设置";
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        getPreferenceManager().setPreferenceDataStore(dataStore);
        addPreferencesFromResource(R.xml.settings_download);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        findPreference("download_engine").setSummary(TorrentConfig.getInstance().getDownloadEngine());
        ListPreference taskCount = (ListPreference) findPreference("task_count");
        taskCount.setEntryValues(taskCountArray);
        taskCount.setEntries(taskCountArray);
        taskCount.setSummary(String.valueOf(TorrentConfig.getInstance().getMaxTaskCount()));
        taskCount.setOnPreferenceChangeListener(((preference, o) -> {
            taskCount.setSummary((String) o);
            return true;
        }));
        EditTextPreference downloadRate = (EditTextPreference) findPreference("download_rate");
        downloadRate.setSummary(getDownloadRateSummary(String.valueOf(TorrentConfig.getInstance().getMaxDownloadRate() / 1000)));
        downloadRate.setOnPreferenceChangeListener(((preference, o) -> {
            downloadRate.setSummary(getDownloadRateSummary((String) o));
            return true;
        }));
    }

    private String getDownloadRateSummary(String rate) {
        if (TextUtils.isEmpty(rate) || rate.equals("0")) {
            return "无限制";
        } else if (CommonUtils.isNum(rate)) {
            return Long.parseLong(rate) + " Kb/s";
        } else {
            return "未知速度";
        }
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        if (!preference.getKey().equals("download_rate")) {
            super.onDisplayPreferenceDialog(preference);
        } else if (getFragmentManager().findFragmentByTag("android.support.v14.preference.PreferenceFragment.DIALOG") == null) {
            EditTextPreferenceDialogFragmentCompat fragmentCompat = DownloadRateFragment.newInstance(preference.getKey());
            fragmentCompat.setTargetFragment(this, 0);
            fragmentCompat.show(getFragmentManager(), "android.support.v14.preference.PreferenceFragment.DIALOG");
        }

    }

    public static class DownloadRateFragment extends EditTextPreferenceDialogFragmentCompat {
        private EditText editText;

        public static DownloadRateFragment newInstance(String key) {
            DownloadRateFragment fragment = new DownloadRateFragment();
            Bundle b = new Bundle(1);
            b.putString("key", key);
            fragment.setArguments(b);
            return fragment;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = super.onCreateDialog(savedInstanceState);
            dialog.setOnShowListener(dialogInterface -> {
                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
                    onClick(dialog, AlertDialog.BUTTON_POSITIVE);
                });
            });
            return dialog;
        }

        @Override
        protected void onBindDialogView(View view) {
            super.onBindDialogView(view);
            editText = view.findViewById(android.R.id.edit);
            editText.setTextColor(CommonUtils.getResColor(R.color.text_black));
            editText.setHintTextColor(CommonUtils.getResColor(R.color.text_gray));
            editText.setHint("请输入最大下载速度(k/s)，0为无限制");
        }

        @Override
        public void onClick(DialogInterface dialog, int which) {
            if (which == -1) {
                String content = editText.getEditableText().toString();
                ((AlertDialog) dialog).show();
                if (TextUtils.isEmpty(content)) {
                    editText.setError("请输入下载速度");
                } else if (!CommonUtils.isNum(content)) {
                    editText.setError("请输入正确的速度");
                } else {
                    super.onClick(dialog, which);
                    dialog.dismiss();
                }
            } else {
                super.onClick(dialog, which);
            }
        }
    }

    static class DownloadSettingsDataStore extends PreferenceDataStore {
        @Nullable
        @Override
        public String getString(String key, @Nullable String defValue) {
            switch (key) {
                case "download_rate":
                    if (TorrentConfig.getInstance().getMaxDownloadRate() < 1000) {
                        return "";
                    }
                    return String.valueOf(TorrentConfig.getInstance().getMaxDownloadRate() / 1000);
                case "task_count":
                    return String.valueOf(TorrentConfig.getInstance().getMaxTaskCount());
            }
            return super.getString(key, defValue);
        }

        @Override
        public void putString(String key, @Nullable String value) {
            switch (key) {
                case "download_rate":
                    if (TextUtils.isEmpty(value) || value.equals("0")) {
                        TorrentConfig.getInstance().setMaxDownloadRate(0);
                        TorrentEngine.getInstance().updateSetting();
                    } else if (CommonUtils.isNum(value)) {
                        try {
                            long rate = Long.parseLong(value);
                            TorrentConfig.getInstance().setMaxDownloadRate((int) (rate * 1000));
                            TorrentEngine.getInstance().updateSetting();
                        } catch (NumberFormatException ignore) {
                        }
                    }
                    break;
                case "task_count":
                    if (CommonUtils.isNum(value)) {
                        try {
                            int count = Integer.parseInt(value);
                            TorrentConfig.getInstance().setMaxTaskCount(count);
                            TorrentEngine.getInstance().updateSetting();
                        } catch (NumberFormatException ignore) {
                        }
                    }
            }
        }

        @Override
        public boolean getBoolean(String key, boolean defValue) {
            if ("only_wifi_download".equals(key)) {
                return TorrentConfig.getInstance().isDownloadOnlyWifi();
            }
            return super.getBoolean(key, defValue);
        }

        @Override
        public void putBoolean(String key, boolean value) {
            if ("only_wifi_download".equals(key)) {
                TorrentConfig.getInstance().setDownloadOnlyWifi(value);
            }
        }
    }
}
