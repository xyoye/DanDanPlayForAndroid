package com.xyoye.dandanplay.ui.activities.personal;

import com.xyoye.dandanplay.R;
import com.xyoye.dandanplay.base.BaseMvcActivity;

/**
 * Created by xyoye on 2018/10/8.
 */

public class PlayerSettingTipsActivity extends BaseMvcActivity {

    @Override
    protected int initPageLayoutID() {
        return R.layout.activity_player_setting_tips;
    }

    @Override
    public void initPageView() {
        setTitle("设置说明");
    }

    @Override
    public void initPageViewListener() {

    }
}
