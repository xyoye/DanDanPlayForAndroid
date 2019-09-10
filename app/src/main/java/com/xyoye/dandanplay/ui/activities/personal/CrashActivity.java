package com.xyoye.dandanplay.ui.activities.personal;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.ui.weight.dialog.CrashDialog;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;

/**
 * Created by xyoye on 2019/9/10.
 */

public class CrashActivity extends AppCompatActivity {

    private CaocConfig mCrashConfig;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crash);

        mCrashConfig = CustomActivityOnCrash.getConfigFromIntent(getIntent());
        if (mCrashConfig == null) {
            finish();
        }

        initListener();
    }

    private void initListener() {
        findViewById(R.id.crash_restart_bt).setOnClickListener(v ->
                CustomActivityOnCrash.restartApplication(CrashActivity.this, mCrashConfig));

        findViewById(R.id.crash_log_bt).setOnClickListener(v -> {
            String crashLog = CustomActivityOnCrash.getAllErrorDetailsFromIntent(CrashActivity.this, getIntent());
            new CrashDialog(this, crashLog).show();
        });

    }
}
