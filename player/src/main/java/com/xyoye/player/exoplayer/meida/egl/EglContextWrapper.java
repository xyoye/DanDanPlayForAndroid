package com.xyoye.player.exoplayer.meida.egl;

import android.opengl.EGL14;
import android.os.Build;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;

/**
 * Created by Chilling on 2016/12/29.
 */

public class EglContextWrapper {

    protected EGLContext eglContextOld;
    protected android.opengl.EGLContext eglContext;
    public static EglContextWrapper EGL_NO_CONTEXT_WRAPPER = new EGLNoContextWrapper();

    public EGLContext getEglContextOld() {
        return eglContextOld;
    }

    public void setEglContextOld(EGLContext eglContextOld) {
        this.eglContextOld = eglContextOld;
    }

    public android.opengl.EGLContext getEglContext() {
        return eglContext;
    }

    public void setEglContext(android.opengl.EGLContext eglContext) {
        this.eglContext = eglContext;
    }


    public static class EGLNoContextWrapper extends EglContextWrapper {

        public EGLNoContextWrapper() {
            eglContextOld = EGL10.EGL_NO_CONTEXT;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                eglContext = EGL14.EGL_NO_CONTEXT;
            }
        }

        @Override
        public void setEglContext(android.opengl.EGLContext eglContext) {
        }

        @Override
        public void setEglContextOld(EGLContext eglContextOld) {
        }
    }
}
