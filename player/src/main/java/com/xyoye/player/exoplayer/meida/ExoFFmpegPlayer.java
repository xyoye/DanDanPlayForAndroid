package com.xyoye.player.exoplayer.meida;

import android.content.Context;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.PlayerMessage;
import com.google.android.exoplayer2.Renderer;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.analytics.AnalyticsCollector;
import com.google.android.exoplayer2.ext.ffmpeg.video.Constant;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.Clock;
import com.google.android.exoplayer2.util.Util;

import java.util.ArrayList;
import java.util.List;

import static com.google.android.exoplayer2.ext.ffmpeg.video.Constant.MSG_PLAY_RELEASED;

/**
 * Created by xyy on 2019/7/10.
 */

public class ExoFFmpegPlayer extends SimpleExoPlayer implements Player.VideoComponent{

    public ExoFFmpegPlayer(Context context) {

        super(context,
                new SimpleRendersFactory(context),
                new DefaultTrackSelector(),
                new DefaultLoadControl(),
                null,
                new DefaultBandwidthMeter.Builder(context).build(),
                new AnalyticsCollector.Factory(),
                Clock.DEFAULT,
                Util.getLooper());
    }

    public ExoFFmpegPlayer(Context context, TrackSelector trackSelector) {

        super(context,
                new SimpleRendersFactory(context),
                trackSelector,
                new DefaultLoadControl(),
                null,
                new DefaultBandwidthMeter.Builder(context).build(),
                new AnalyticsCollector.Factory(),
                Clock.DEFAULT,
                Util.getLooper());
    }

    private class InnerSurfaceTextureListener implements TextureView.SurfaceTextureListener{
        private TextureView.SurfaceTextureListener surfaceTextureListener;

        public InnerSurfaceTextureListener(TextureView.SurfaceTextureListener surfaceTextureListener){
            this.surfaceTextureListener = surfaceTextureListener;
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            surfaceTextureListener.onSurfaceTextureAvailable(surface, width, height);
            onSurfaceSizeChanged(width, height);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            surfaceTextureListener.onSurfaceTextureSizeChanged(surface, width, height);
            onSurfaceSizeChanged(width, height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return  surfaceTextureListener.onSurfaceTextureDestroyed(surface);
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            surfaceTextureListener.onSurfaceTextureUpdated(surface);
        }
    }

    private void onSurfaceSizeChanged(int width, int height) {
        List<PlayerMessage> messages = new ArrayList<>();
        Point size = new Point(width, height);

        for (Renderer renderer : renderers) {
            if (renderer.getTrackType() == C.TRACK_TYPE_VIDEO) {
                messages.add(createMessage(renderer).setType(Constant.MSG_SURFACE_SIZE_CHANGED).setPayload(size).send());
            }
        }

        try {
            for (PlayerMessage message : messages){
                message.blockUntilDelivered();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private SurfaceHolder.Callback innerSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            onSurfaceSizeChanged(width, height);
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }
    };

    private TextureView textureView = null;
    private SurfaceHolder surfaceHolder = null;

    private void clearListener() {
        if (surfaceHolder != null)
            this.surfaceHolder.removeCallback(innerSurfaceCallback);
    }

    @Override
    public void setVideoTextureView(TextureView textureView) {
        clearListener();
        this.textureView = textureView;
        this.setVideoTextureView(textureView);

        if (textureView.isAvailable()) {
            onSurfaceSizeChanged(textureView.getWidth(), textureView.getHeight());
        }
        textureView.setSurfaceTextureListener(new InnerSurfaceTextureListener(textureView.getSurfaceTextureListener()));
    }

    @Override
    public void clearVideoTextureView(TextureView textureView) {

    }

    @Override
    public void setVideoSurfaceView(SurfaceView surfaceView) {
        clearListener();
        this.surfaceHolder = surfaceView.getHolder();
        setVideoSurfaceHolder(surfaceHolder);
        surfaceHolder.addCallback(innerSurfaceCallback);
    }

    @Override
    public void release() {
        clearListener();
        onPlayReleased();
        super.release();
    }

    private void onPlayReleased() {

        List<PlayerMessage> messages = new ArrayList<>();

        for (Renderer renderer : renderers) {
            messages.add(createMessage(renderer).setType(MSG_PLAY_RELEASED).send());
        }

        try {
            for (PlayerMessage message : messages){
                message.blockUntilDelivered();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
