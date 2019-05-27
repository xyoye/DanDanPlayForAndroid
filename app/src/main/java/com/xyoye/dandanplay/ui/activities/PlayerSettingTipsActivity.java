package com.xyoye.dandanplay.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.jaeger.library.StatusBarUtil;
import com.xyoye.dandanplay.R;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by xyoye on 2018/10/8.
 */

public class PlayerSettingTipsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_setting_tips);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        StatusBarUtil.setColor(this, ContextCompat.getColor(this, R.color.theme_color), 0);

        setTitle("设置说明");
    }
}
