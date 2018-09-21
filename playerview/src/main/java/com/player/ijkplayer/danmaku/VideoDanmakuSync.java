package com.player.ijkplayer.danmaku;

import android.util.Log;

import com.player.ijkplayer.media.IjkPlayerView;

import com.player.danmaku.danmaku.model.AbsDanmakuSync;

/**
 * Created by long on 2016/12/15.
 * 同步弹幕和video，貌似没法保持同步，可能我用的有问题- -
 */
@Deprecated
public class VideoDanmakuSync extends AbsDanmakuSync {

    private final IjkPlayerView mPlayerView;

    public VideoDanmakuSync(IjkPlayerView playerView) {
        mPlayerView = playerView;
    }


    @Override
    public long getUptimeMillis() {
        if (mPlayerView != null) {
            Log.i("VideoDanmakuSync", ""+mPlayerView.getCurPosition());
            return mPlayerView.getCurPosition();
        }
        return -1L;
    }

    @Override
    public int getSyncState() {
        if (mPlayerView.isPlaying()) {
            Log.e("VideoDanmakuSync", "SYNC_STATE_PLAYING");
            return SYNC_STATE_PLAYING;
        } else {
            Log.e("VideoDanmakuSync", "SYNC_STATE_HALT");
            return SYNC_STATE_HALT;
        }
    }

    @Override
    public boolean isSyncPlayingState() {
        return true;
    }
}
