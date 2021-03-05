package com.google.android.exoplayer2.ext

import android.content.Context
import android.os.Handler
import com.google.android.exoplayer2.DefaultRenderersFactory
import com.google.android.exoplayer2.Renderer
import com.google.android.exoplayer2.audio.AudioRendererEventListener
import com.google.android.exoplayer2.audio.AudioSink
import com.google.android.exoplayer2.ext.ffmpeg.FfmpegAudioRenderer
import com.google.android.exoplayer2.mediacodec.MediaCodecSelector
import java.util.*

/**
 * Created by xyoye on 2020/12/25.
 */

class FfmpegRenderersFactory(context: Context) : DefaultRenderersFactory(context) {

    init {
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_PREFER)
    }

    override fun buildAudioRenderers(
        context: Context,
        extensionRendererMode: Int,
        mediaCodecSelector: MediaCodecSelector,
        enableDecoderFallback: Boolean,
        audioSink: AudioSink,
        eventHandler: Handler,
        eventListener: AudioRendererEventListener,
        out: ArrayList<Renderer>
    ) {
        out.add(FfmpegAudioRenderer())
        super.buildAudioRenderers(
            context,
            extensionRendererMode,
            mediaCodecSelector,
            enableDecoderFallback,
            audioSink,
            eventHandler,
            eventListener,
            out
        )
    }
}