package com.google.android.exoplayer2.ext.ffmpeg.exception;

/**
 * Created by xyoye on 2019/7/10.
 */

public class VideoSoftDecoderException extends Exception {

    public VideoSoftDecoderException(String message) {
        super(message);
    }

    public VideoSoftDecoderException(String message, Throwable cause) {
        super(message, cause);
    }
}
