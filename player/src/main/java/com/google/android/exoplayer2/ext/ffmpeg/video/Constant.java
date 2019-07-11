package com.google.android.exoplayer2.ext.ffmpeg.video;

import static com.google.android.exoplayer2.C.MSG_CUSTOM_BASE;

/**
 * Created by joffy on 18/2/17.
 */

public class Constant {
    public static final int BUFFER_FLAG_DECODE_AGAIN = 0x00800000;

    public static final int MSG_SURFACE_SIZE_CHANGED = MSG_CUSTOM_BASE + 100;
    public static final int MSG_PLAY_RELEASED = MSG_CUSTOM_BASE + 101;
}
