package com.xyoye.dandanplay.ui.temp;

import android.content.Context;

/**
 * Title: VideoViewContract <br>
 * Description: <br>
 * Copyright (c) 传化物流版权所有 2016 <br>
 * Created DateTime: 2016/10/29 0029 9:42
 * Created by Wentao.Shi.
 */
public class VideoViewContract {
    public interface View extends BaseView {
        Context getContext();
        String getVideoPath();
    }

    public interface Present extends BasePresenter {
        void sendDanmu(int episodeId, double time, int type, int color, String msg);
    }
}
