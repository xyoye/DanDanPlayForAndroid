package com.player.exoplayer.meida.egl;

/**
 * Created by Chilling on 2016/12/29.
 */
public interface IEglHelper {
    EglContextWrapper start(EglContextWrapper eglContext);

    boolean createSurface(Object surface);

    int swap();

    void destroySurface();

    void finish();

    void setPresentationTime(long nsecs);
}
