package com.google.android.exoplayer2.ext.ffmpeg.exception;

import com.google.android.exoplayer2.audio.AudioDecoderException;

/**
 * Created by xyoye on 2019/7/10.
 */

public class AudioSoftDecoderException extends AudioDecoderException {

    public AudioSoftDecoderException(String message) {
        super(message);
    }

    public AudioSoftDecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
