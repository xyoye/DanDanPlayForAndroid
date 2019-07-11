package com.player.exoplayer.meida.egl;

import android.os.Build;

/**
 * Created by Chilling on 2017/1/2.
 */

public class EglHelperFactory {

    public static IEglHelper create(GLThread.EGLConfigChooser configChooser, GLThread.EGLContextFactory eglContextFactory
            , GLThread.EGLWindowSurfaceFactory eglWindowSurfaceFactory) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return new EglHelperAPI17(configChooser, eglContextFactory, eglWindowSurfaceFactory);
        } else {
            return new EglHelper(configChooser, eglContextFactory, eglWindowSurfaceFactory);
        }
    }

}
