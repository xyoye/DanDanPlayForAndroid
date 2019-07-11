/*
 *
 *  *
 *  *  * Copyright (C) 2016 ChillingVan
 *  *  *
 *  *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  *  * you may not use this file except in compliance with the License.
 *  *  * You may obtain a copy of the License at
 *  *  *
 *  *  * http://www.apache.org/licenses/LICENSE-2.0
 *  *  *
 *  *  * Unless required by applicable law or agreed to in writing, software
 *  *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  *  * See the License for the specific language governing permissions and
 *  *  * limitations under the License.
 *  *
 *
 */

package com.player.exoplayer.meida.egl;

import android.annotation.TargetApi;
import android.opengl.EGL14;
import android.opengl.EGLExt;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.Choreographer;

import java.util.ArrayList;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGL11;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;
import javax.microedition.khronos.egl.EGLSurface;
import javax.microedition.khronos.opengles.GL;

/**
 * Created by Chilling on 2016/10/30.
 */

public class GLThread extends Thread {
    public final static boolean LOG_PAUSE_RESUME = false;
    public final static boolean LOG_SURFACE = false;
    public final static boolean LOG_RENDERER = false;
    public final static boolean LOG_RENDERER_DRAW_FRAME = false;
    public final static boolean LOG_EGL = false;
    public final static boolean LOG_THREADS = false;

    public final static int RENDERMODE_WHEN_DIRTY = 0;
    public final static int RENDERMODE_CONTINUOUSLY = 1;

    private final GLThreadManager sGLThreadManager = new GLThreadManager();


    private int mRenderMode;
    private EGLWindowSurfaceFactory mEGLWindowSurfaceFactory;
    private EGLConfigChooser mEGLConfigChooser;
    private EGLContextFactory mEGLContextFactory;
    private GLViewRenderer mRenderer;
    private Object mSurface;

    private OnCreateGLContextListener onCreateGLContextListener;
    private boolean mPreserveEGLContextOnPause = true;


    // Once the thread is started, all accesses to the following member
    // variables are protected by the sGLThreadManager monitor
    private boolean mShouldExit;
    private boolean mExited;
    private boolean mRequestPaused;
    private boolean mPaused;
    private boolean mHasSurface;
    private boolean mSurfaceIsBad;
    private boolean mWaitingForSurface;
    private boolean mHaveEglContext;
    private boolean mHaveEglSurface;
    private boolean mFinishedCreatingEglSurface;
    private int mWidth;
    private int mHeight;
    private boolean mRequestRender;
    private boolean mWantRenderNotification;
    private boolean mRenderComplete;
    private ArrayList<Runnable> mEventQueue = new ArrayList<>();
    private boolean mSizeChanged = true;
    private boolean changeSurface = false;
    private EglContextWrapper mEglContext = EglContextWrapper.EGL_NO_CONTEXT_WRAPPER;


    private ChoreographerRenderWrapper mChoreographerRenderWrapper = new ChoreographerRenderWrapper(this);
    private long frameTimeNanos;

    GLThread(EGLConfigChooser configChooser, EGLContextFactory eglContextFactory
            , EGLWindowSurfaceFactory eglWindowSurfaceFactory, GLViewRenderer renderer
            , int renderMode, Object surface, EglContextWrapper sharedEglContext) {
        super();
        mWidth = 0;
        mHeight = 0;
        mRequestRender = true;
        mRenderMode = renderMode;
        mWantRenderNotification = false;

        this.mEGLConfigChooser = configChooser;
        mEGLContextFactory = eglContextFactory;
        mEGLWindowSurfaceFactory = eglWindowSurfaceFactory;
        mSurface = surface;
        mRenderer = renderer;
        this.mEglContext = sharedEglContext;
    }

    public void setSurface(@NonNull Object surface) {
        if (mSurface != surface) {
            changeSurface = true;
        }
        this.mSurface = surface;
    }

    @Override
    public void run() {
        setName("GLThread " + getId());
        if (LOG_THREADS) {
            Log.i("GLThread", "starting tid=" + getId());
        }

        try {
            guardedRun();
        } catch (InterruptedException e) {
            // fall thru and exit normally
        } finally {
            sGLThreadManager.threadExiting(this);
        }
    }


    public void setPreserveEGLContextOnPause(boolean mPreserveEGLContextOnPause) {
        this.mPreserveEGLContextOnPause = mPreserveEGLContextOnPause;
    }


    /*
         * This private method should only be called inside a
         * synchronized(sGLThreadManager) block.
         */
    private void stopEglSurfaceLocked() {
        if (mHaveEglSurface) {
            mHaveEglSurface = false;
            mEglHelper.destroySurface();
        }
    }

    /*
     * This private method should only be called inside a
     * synchronized(sGLThreadManager) block.
     */
    private void stopEglContextLocked() {
        if (mHaveEglContext) {
            mEglHelper.finish();
            mHaveEglContext = false;
            sGLThreadManager.releaseEglContextLocked(this);
        }
    }

    private void guardedRun() throws InterruptedException {
        mEglHelper = EglHelperFactory.create(mEGLConfigChooser, mEGLContextFactory, mEGLWindowSurfaceFactory);
        mHaveEglContext = false;
        mHaveEglSurface = false;
        mWantRenderNotification = false;

        try {
            boolean createEglContext = false;
            boolean createEglSurface = false;
            boolean createGlInterface = false;
            boolean lostEglContext = false;
            boolean sizeChanged = false;
            boolean wantRenderNotification = false;
            boolean doRenderNotification = false;
            boolean askedToReleaseEglContext = false;
            int w = 0;
            int h = 0;
            Runnable event = null;

            while (true) {
                synchronized (sGLThreadManager) {
                    while (true) {
                        if (mShouldExit) {
                            return;
                        }

                        if (!mEventQueue.isEmpty() && mHaveEglContext) {
                            event = mEventQueue.remove(0);
                            break;
                        }

                        // Update the pause state.
                        boolean pausing = false;
                        if (mPaused != mRequestPaused) {
                            pausing = mRequestPaused;
                            mPaused = mRequestPaused;
                            sGLThreadManager.notifyAll();
                            if (LOG_PAUSE_RESUME) {
                                Log.i("GLThread", "mPaused is now " + mPaused + " tid=" + getId());
                            }
                        }

                        // Have we lost the EGL context?
                        if (lostEglContext) {
                            stopEglSurfaceLocked();
                            stopEglContextLocked();
                            lostEglContext = false;
                        }

                        // When pausing, release the EGL surface:
                        if (pausing && mHaveEglSurface) {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "releasing EGL surface because paused tid=" + getId());
                            }
                            stopEglSurfaceLocked();
                        }

                        // When pausing, optionally release the EGL Context:
                        if (pausing && mHaveEglContext) {
                            boolean preserveEglContextOnPause = mPreserveEGLContextOnPause;
                            if (!preserveEglContextOnPause) {
                                stopEglContextLocked();
                                if (LOG_SURFACE) {
                                    Log.i("GLThread", "releasing EGL context because paused tid=" + getId());
                                }
                            }
                        }


                        // Have we lost the SurfaceView surface?
                        if ((!mHasSurface) && (!mWaitingForSurface)) {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "noticed surfaceView surface lost tid=" + getId());
                            }
                            if (mHaveEglSurface) {
                                stopEglSurfaceLocked();
                            }
                            mWaitingForSurface = true;
                            mSurfaceIsBad = false;
                            sGLThreadManager.notifyAll();
                        }

                        // Have we acquired the surface view surface?
                        if (mHasSurface && mWaitingForSurface) {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "noticed surfaceView surface acquired tid=" + getId());
                            }
                            mWaitingForSurface = false;
                            sGLThreadManager.notifyAll();
                        }

                        if (doRenderNotification) {
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "sending render notification tid=" + getId());
                            }
                            mWantRenderNotification = false;
                            doRenderNotification = false;
                            mRenderComplete = true;
                            sGLThreadManager.notifyAll();
                        }

                        // Ready to draw?
                        if (readyToDraw()) {

                            // If we don't have an EGL context, try to acquire one.
                            if (!mHaveEglContext) {
                                if (askedToReleaseEglContext) {
                                    askedToReleaseEglContext = false;
                                } else if (sGLThreadManager.tryAcquireEglContextLocked(this)) {
                                    try {
                                        mEglContext = mEglHelper.start(mEglContext);
                                        if (onCreateGLContextListener != null) {
                                            onCreateGLContextListener.onCreate(mEglContext);
                                        }
                                    } catch (RuntimeException t) {
                                        sGLThreadManager.releaseEglContextLocked(this);
                                        throw t;
                                    }
                                    mHaveEglContext = true;
                                    createEglContext = true;

                                    sGLThreadManager.notifyAll();
                                }
                            }

                            if (mHaveEglContext && !mHaveEglSurface) {
                                mHaveEglSurface = true;
                                createEglSurface = true;
                                createGlInterface = true;
                                sizeChanged = true;
                            }

                            if (mHaveEglSurface) {
                                if (mSizeChanged) {
                                    sizeChanged = true;
                                    w = mWidth;
                                    h = mHeight;
                                    mWantRenderNotification = true;
                                    if (LOG_SURFACE) {
                                        Log.i("GLThread",
                                                "noticing that we want render notification tid="
                                                        + getId());
                                    }

                                    // Destroy and recreate the EGL surface.
                                    createEglSurface = true;

                                    mSizeChanged = false;
                                }

                                if (changeSurface) {
                                    createEglSurface = true;
                                    changeSurface = false;
                                }


                                mRequestRender = false;
                                sGLThreadManager.notifyAll();
                                if (mWantRenderNotification) {
                                    wantRenderNotification = true;
                                }
                                break;
                            }
                        }

                        // By design, this is the only place in a GLThread thread where we wait().
                        if (LOG_THREADS) {
                            Log.i("GLThread", "waiting tid=" + getId()
                                    + " mHaveEglContext: " + mHaveEglContext
                                    + " mHaveEglSurface: " + mHaveEglSurface
                                    + " mFinishedCreatingEglSurface: " + mFinishedCreatingEglSurface
                                    + " mPaused: " + mPaused
                                    + " mHasSurface: " + mHasSurface
                                    + " mSurfaceIsBad: " + mSurfaceIsBad
                                    + " mWaitingForSurface: " + mWaitingForSurface
                                    + " mWidth: " + mWidth
                                    + " mHeight: " + mHeight
                                    + " mRequestRender: " + mRequestRender
                                    + " mRenderMode: " + mRenderMode);
                        }
                        sGLThreadManager.wait();
                    }
                } // end of synchronized(sGLThreadManager)

                if (event != null) {
                    event.run();
                    event = null;
                    continue;
                }

                if (createEglSurface) {
                    if (LOG_SURFACE) {
                        Log.w("GLThread", "egl createSurface");
                    }
                    if (mEglHelper.createSurface(mSurface)) {
                        synchronized (sGLThreadManager) {
                            mFinishedCreatingEglSurface = true;
                            sGLThreadManager.notifyAll();
                        }
                    } else {
                        synchronized (sGLThreadManager) {
                            mFinishedCreatingEglSurface = true;
                            mSurfaceIsBad = true;
                            sGLThreadManager.notifyAll();
                        }
                        continue;
                    }
                    createEglSurface = false;
                }

                if (createGlInterface) {

                    createGlInterface = false;
                }

                if (createEglContext) {
                    if (LOG_RENDERER) {
                        Log.w("GLThread", "onSurfaceCreated");
                    }
                    mRenderer.onSurfaceCreated();
                    createEglContext = false;
                }

                if (sizeChanged) {
                    if (LOG_RENDERER) {
                        Log.w("GLThread", "onSurfaceChanged(" + w + ", " + h + ")");
                    }
                    mRenderer.onSurfaceChanged(w, h);
                    sizeChanged = false;
                }

                if (mChoreographerRenderWrapper.canSwap()) {
                    if (LOG_RENDERER_DRAW_FRAME) {
                        Log.w("GLThread", "onDrawFrame tid=" + getId());
                    }
                    mRenderer.onDrawFrame();
                    mEglHelper.setPresentationTime(frameTimeNanos);
                    int swapError = mEglHelper.swap();
                    mChoreographerRenderWrapper.disableSwap();
                    switch (swapError) {
                        case EGL10.EGL_SUCCESS:
                            break;
                        case EGL11.EGL_CONTEXT_LOST:
                            if (LOG_SURFACE) {
                                Log.i("GLThread", "egl context lost tid=" + getId());
                            }
                            lostEglContext = true;
                            break;
                        default:
                            // Other errors typically mean that the current surface is bad,
                            // probably because the SurfaceView surface has been destroyed,
                            // but we haven't been notified yet.
                            // Log the error to help developers understand why rendering stopped.
                            EglHelper.logEglErrorAsWarning("GLThread", "eglSwapBuffers", swapError);

                            synchronized (sGLThreadManager) {
                                mSurfaceIsBad = true;
                                sGLThreadManager.notifyAll();
                            }
                            break;
                    }
                }

                if (wantRenderNotification) {
                    doRenderNotification = true;
                    wantRenderNotification = false;
                }

            }

        } finally {
                /*
                 * clean-up everything...
                 */
            synchronized (sGLThreadManager) {
                stopEglSurfaceLocked();
                stopEglContextLocked();
            }
        }
    }

    @Override
    public synchronized void start() {
        super.start();
        mChoreographerRenderWrapper.start();
    }

    public boolean ableToDraw() {
        return mHaveEglContext && mHaveEglSurface && readyToDraw();
    }

    private boolean readyToDraw() {
        return (!mPaused) && mHasSurface && (!mSurfaceIsBad)
                && (mWidth > 0) && (mHeight > 0)
                && (mRequestRender );
    }

    public EglContextWrapper getEglContext() {
        return mEglContext;
    }

    public void setOnCreateGLContextListener(OnCreateGLContextListener onCreateGLContextListener) {
        this.onCreateGLContextListener = onCreateGLContextListener;
    }

    public interface OnCreateGLContextListener {
        void onCreate(EglContextWrapper eglContext);
    }

    public void setRenderMode(int renderMode) {
        if (!((RENDERMODE_WHEN_DIRTY <= renderMode) && (renderMode <= RENDERMODE_CONTINUOUSLY))) {
            throw new IllegalArgumentException("renderMode");
        }
        synchronized (sGLThreadManager) {
            mRenderMode = renderMode;
            sGLThreadManager.notifyAll();
        }
    }

    public int getRenderMode() {
        return mRenderMode;
    }

    public void requestRender() {
        requestRender(0);
    }

    public void requestRender(long frameTimeNanos) {
        this.frameTimeNanos = frameTimeNanos;
        synchronized (sGLThreadManager) {
            mRequestRender = true;
            sGLThreadManager.notifyAll();
        }
    }

    public void requestRenderAndWait() {
        synchronized (sGLThreadManager) {
            // If we are already on the GL thread, this means a client callback
            // has caused reentrancy, for example via updating the SurfaceView parameters.
            // We will return to the client rendering code, so here we don't need to
            // do anything.
            if (Thread.currentThread() == this) {
                return;
            }

            mWantRenderNotification = true;
            mRequestRender = true;
            mRenderComplete = false;

            sGLThreadManager.notifyAll();

            while (!mExited && !mPaused && !mRenderComplete && ableToDraw()) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }

        }
    }

    public void surfaceCreated() {
        synchronized (sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("GLThread", "surfaceCreated tid=" + getId());
            }
            mHasSurface = true;
            mFinishedCreatingEglSurface = false;
            sGLThreadManager.notifyAll();
            while (mWaitingForSurface
                    && !mFinishedCreatingEglSurface
                    && !mExited) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void surfaceDestroyed() {
        synchronized (sGLThreadManager) {
            if (LOG_THREADS) {
                Log.i("GLThread", "surfaceDestroyed tid=" + getId());
            }
            mHasSurface = false;
            sGLThreadManager.notifyAll();
            while ((!mWaitingForSurface) && (!mExited)) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void onPause() {
        synchronized (sGLThreadManager) {
            if (LOG_PAUSE_RESUME) {
                Log.i("GLThread", "onPause tid=" + getId());
            }
            mRequestPaused = true;
            sGLThreadManager.notifyAll();
            while ((!mExited) && (!mPaused)) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("Main thread", "onPause waiting for mPaused.");
                }
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            mChoreographerRenderWrapper.stop();
        }
    }

    public void onResume() {
        synchronized (sGLThreadManager) {
            if (LOG_PAUSE_RESUME) {
                Log.i("GLThread", "onResume tid=" + getId());
            }
            mRequestPaused = false;
            mRequestRender = true;
            mRenderComplete = false;
            sGLThreadManager.notifyAll();
            while ((!mExited) && mPaused && (!mRenderComplete)) {
                if (LOG_PAUSE_RESUME) {
                    Log.i("Main thread", "onResume waiting for !mPaused.");
                }
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            mChoreographerRenderWrapper.start();
        }
    }

    public void onWindowResize(int w, int h) {
        synchronized (sGLThreadManager) {
            mWidth = w;
            mHeight = h;
            mSizeChanged = true;
            mRequestRender = true;
            mRenderComplete = false;

            // If we are already on the GL thread, this means a client callback
            // has caused reentrancy, for example via updating the SurfaceView parameters.
            // We need to process the size change eventually though and update our EGLSurface.
            // So we set the parameters and return so they can be processed on our
            // next iteration.
            if (Thread.currentThread() == this) {
                return;
            }

            sGLThreadManager.notifyAll();

            // Wait for thread to react to resize and render a frame
            while (!mExited && !mPaused && !mRenderComplete
                    && ableToDraw()) {
                if (LOG_SURFACE) {
                    Log.i("Main thread", "onWindowResize waiting for render complete from tid=" + getId());
                }
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    public void requestExitAndWait() {
        // don't call this from GLThread thread or it is a guaranteed
        // deadlock!
        synchronized (sGLThreadManager) {
            mShouldExit = true;
            sGLThreadManager.notifyAll();
            while (!mExited) {
                try {
                    sGLThreadManager.wait();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /**
     * Queue an "event" to be run on the GL rendering thread.
     *
     * @param r the runnable to be run on the GL rendering thread.
     */
    public void queueEvent(Runnable r) {
        if (r == null) {
            throw new IllegalArgumentException("r must not be null");
        }
        synchronized (sGLThreadManager) {
            mEventQueue.add(r);
            sGLThreadManager.notifyAll();
        }
    }


    // End of member variables protected by the sGLThreadManager monitor.

    private IEglHelper mEglHelper;


    public interface GLWrapper {
        /**
         * Wraps a gl interface in another gl interface.
         *
         * @param gl a GL interface that is to be wrapped.
         * @return either the input argument or another GL object that wraps the input argument.
         */
        GL wrap(GL gl);
    }


    private static class GLThreadManager {
        private GLThread mEglOwner;

        public synchronized void threadExiting(GLThread thread) {
            if (LOG_THREADS) {
                Log.i("GLThread", "exiting tid=" + thread.getId());
            }
            thread.mExited = true;
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

        /*
         * Tries once to acquire the right to use an EGL
         * context. Does not block. Requires that we are already
         * in the sGLThreadManager monitor when this is called.
         *
         * @return true if the right to use an EGL context was acquired.
         */
        public boolean tryAcquireEglContextLocked(GLThread thread) {
            if (mEglOwner == thread || mEglOwner == null) {
                mEglOwner = thread;
                notifyAll();
                return true;
            }
            return true;
        }

        /*
         * Releases the EGL context. Requires that we are already in the
         * sGLThreadManager monitor when this is called.
         */
        public void releaseEglContextLocked(GLThread thread) {
            if (mEglOwner == thread) {
                mEglOwner = null;
            }
            notifyAll();
        }

    }


    public interface EGLConfigChooser {
        /**
         * Choose a configuration from the list. Implementors typically
         * implement this method by calling
         * {@link EGL10#eglChooseConfig} and iterating through the results. Please consult the
         * EGL specification available from The Khronos Group to learn how to call eglChooseConfig.
         *
         * @param egl     the EGL10 for the current display.
         * @param display the current display.
         * @return the chosen configuration.
         */
        EGLConfig chooseConfig(EGL10 egl, EGLDisplay display);

        android.opengl.EGLConfig chooseConfig(android.opengl.EGLDisplay display, boolean recordable);
    }

    private static abstract class BaseConfigChooser
            implements EGLConfigChooser {


        private static final int EGL_RECORDABLE_ANDROID = 0x3142;
        protected int[] mConfigSpec;
        private int contextClientVersion;

        public BaseConfigChooser(int[] configSpec, int contextClientVersion) {
            mConfigSpec = filterConfigSpec(configSpec);
            this.contextClientVersion = contextClientVersion;
        }

        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display) {
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(display, mConfigSpec, null, 0,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig failed");
            }

            int numConfigs = num_config[0];

            if (numConfigs <= 0) {
                throw new IllegalArgumentException(
                        "No configs match configSpec");
            }

            EGLConfig[] configs = new EGLConfig[numConfigs];
            if (!egl.eglChooseConfig(display, mConfigSpec, configs, numConfigs,
                    num_config)) {
                throw new IllegalArgumentException("eglChooseConfig#2 failed");
            }
            EGLConfig config = chooseConfig(egl, display, configs);
            if (config == null) {
                throw new IllegalArgumentException("No config chosen");
            }
            return config;
        }

        abstract EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                        EGLConfig[] configs);


        private int[] filterConfigSpec(int[] configSpec) {
            if (contextClientVersion != 2 && contextClientVersion != 3) {
                return configSpec;
            }
            /* We know none of the subclasses define EGL_RENDERABLE_TYPE.
             * And we know the configSpec is well formed.
             */
            int len = configSpec.length;
            int[] newConfigSpec = new int[len + 2];
            System.arraycopy(configSpec, 0, newConfigSpec, 0, len - 1);
            newConfigSpec[len - 1] = EGL10.EGL_RENDERABLE_TYPE;
            if (contextClientVersion == 2) {
                newConfigSpec[len] = EGL14.EGL_OPENGL_ES2_BIT;  /* EGL_OPENGL_ES2_BIT */
            } else {
                newConfigSpec[len] = EGLExt.EGL_OPENGL_ES3_BIT_KHR; /* EGL_OPENGL_ES3_BIT_KHR */
            }
            newConfigSpec[len + 1] = EGL10.EGL_NONE;
            return newConfigSpec;
        }


        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        public android.opengl.EGLConfig chooseConfig(android.opengl.EGLDisplay display, boolean recordable) {
            int renderableType = EGL14.EGL_OPENGL_ES2_BIT;
            if (contextClientVersion >= 3) {
                renderableType |= EGLExt.EGL_OPENGL_ES3_BIT_KHR;
            }

            // The actual surface is generally RGBA or RGBX, so situationally omitting alpha
            // doesn't really help.  It can also lead to a huge performance hit on glReadPixels()
            // when reading into a GL_RGBA buffer.
            int[] attribList = {
                    EGL14.EGL_RED_SIZE, 8,
                    EGL14.EGL_GREEN_SIZE, 8,
                    EGL14.EGL_BLUE_SIZE, 8,
                    EGL14.EGL_ALPHA_SIZE, 8,
                    //EGL14.EGL_DEPTH_SIZE, 16,
                    //EGL14.EGL_STENCIL_SIZE, 8,
                    EGL14.EGL_RENDERABLE_TYPE, renderableType,
                    EGL14.EGL_NONE, 0,      // placeholder for recordable [@-3]
                    EGL14.EGL_NONE
            };
            if (recordable) {
                attribList[attribList.length - 3] = EGL_RECORDABLE_ANDROID;
                attribList[attribList.length - 2] = 1;
            }
            android.opengl.EGLConfig[] configs = new android.opengl.EGLConfig[1];
            int[] numConfigs = new int[1];
            if (!EGL14.eglChooseConfig(display, attribList, 0, configs, 0, configs.length,
                    numConfigs, 0)) {
                Log.w("GLThread", "unable to find RGB8888 / " + contextClientVersion + " EGLConfig");
                return null;
            }
            return configs[0];
        }
    }

    /**
     * Choose a configuration with exactly the specified r,g,b,a sizes,
     * and at least the specified depth and stencil sizes.
     */
    private static class ComponentSizeChooser extends BaseConfigChooser {
        public ComponentSizeChooser(int redSize, int greenSize, int blueSize,
                                    int alphaSize, int depthSize, int stencilSize, int contextClientVersion) {
            super(new int[]{
                    EGL10.EGL_RED_SIZE, redSize,
                    EGL10.EGL_GREEN_SIZE, greenSize,
                    EGL10.EGL_BLUE_SIZE, blueSize,
                    EGL10.EGL_ALPHA_SIZE, alphaSize,
                    EGL10.EGL_DEPTH_SIZE, depthSize,
                    EGL10.EGL_STENCIL_SIZE, stencilSize,
                    EGL10.EGL_NONE}, contextClientVersion);
            mValue = new int[1];
            mRedSize = redSize;
            mGreenSize = greenSize;
            mBlueSize = blueSize;
            mAlphaSize = alphaSize;
            mDepthSize = depthSize;
            mStencilSize = stencilSize;
        }

        @Override
        public EGLConfig chooseConfig(EGL10 egl, EGLDisplay display,
                                      EGLConfig[] configs) {
            for (EGLConfig config : configs) {
                int d = findConfigAttrib(egl, display, config,
                        EGL10.EGL_DEPTH_SIZE, 0);
                int s = findConfigAttrib(egl, display, config,
                        EGL10.EGL_STENCIL_SIZE, 0);
                if ((d >= mDepthSize) && (s >= mStencilSize)) {
                    int r = findConfigAttrib(egl, display, config,
                            EGL10.EGL_RED_SIZE, 0);
                    int g = findConfigAttrib(egl, display, config,
                            EGL10.EGL_GREEN_SIZE, 0);
                    int b = findConfigAttrib(egl, display, config,
                            EGL10.EGL_BLUE_SIZE, 0);
                    int a = findConfigAttrib(egl, display, config,
                            EGL10.EGL_ALPHA_SIZE, 0);
                    if ((r == mRedSize) && (g == mGreenSize)
                            && (b == mBlueSize) && (a == mAlphaSize)) {
                        return config;
                    }
                }
            }
            return null;
        }

        private int findConfigAttrib(EGL10 egl, EGLDisplay display,
                                     EGLConfig config, int attribute, int defaultValue) {

            if (egl.eglGetConfigAttrib(display, config, attribute, mValue)) {
                return mValue[0];
            }
            return defaultValue;
        }

        private int[] mValue;
        // Subclasses can adjust these values:
        protected int mRedSize;
        protected int mGreenSize;
        protected int mBlueSize;
        protected int mAlphaSize;
        protected int mDepthSize;
        protected int mStencilSize;
    }

    /**
     * This class will choose a RGB_888 surface with
     * or without a depth buffer.
     */
    public static class SimpleEGLConfigChooser extends ComponentSizeChooser {
        public SimpleEGLConfigChooser(boolean withDepthBuffer, int contextClientVersion) {
            super(8, 8, 8, 0, withDepthBuffer ? 16 : 0, 0, contextClientVersion);
        }
    }


    public interface EGLContextFactory {
        EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig eglConfig, EGLContext eglContext);

        void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context);


        android.opengl.EGLContext createContextAPI17(android.opengl.EGLDisplay display, android.opengl.EGLConfig eglConfig, android.opengl.EGLContext eglContext);


        void destroyContext(android.opengl.EGLDisplay display, android.opengl.EGLContext context);
    }

    public static class DefaultContextFactory implements EGLContextFactory {
        private int EGL_CONTEXT_CLIENT_VERSION = 0x3098;

        private int contextClientVersion;

        public DefaultContextFactory(int contextClientVersion) {
            this.contextClientVersion = contextClientVersion;
        }

        @Override
        public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config, EGLContext eglContext) {
            int[] attrib_list = {
                    EGL_CONTEXT_CLIENT_VERSION, contextClientVersion,
                    EGL10.EGL_NONE};

            return egl.eglCreateContext(display, config, eglContext,
                    contextClientVersion != 0 ? attrib_list : null);
        }

        @Override
        public void destroyContext(EGL10 egl, EGLDisplay display,
                                   EGLContext context) {
            if (!egl.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                if (LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId());
                }
                EglHelper.throwEglException("eglDestroyContext", egl.eglGetError());
            }
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public android.opengl.EGLContext createContextAPI17(android.opengl.EGLDisplay display, android.opengl.EGLConfig eglConfig, android.opengl.EGLContext sharedContext) {
            int[] attrib_list = {
                    EGL14.EGL_CONTEXT_CLIENT_VERSION, contextClientVersion,
                    EGL14.EGL_NONE};
            return EGL14.eglCreateContext(display, eglConfig, sharedContext, attrib_list, 0);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void destroyContext(android.opengl.EGLDisplay display, android.opengl.EGLContext context) {
            if (!EGL14.eglDestroyContext(display, context)) {
                Log.e("DefaultContextFactory", "display:" + display + " context: " + context);
                if (LOG_THREADS) {
                    Log.i("DefaultContextFactory", "tid=" + Thread.currentThread().getId());
                }
                EglHelper.throwEglException("eglDestroyContext", EGL14.eglGetError());
            }
        }
    }


    public interface EGLWindowSurfaceFactory {
        /**
         * @return null if the surface cannot be constructed.
         */
        EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display, EGLConfig config,
                                       Object nativeWindow);

        void destroySurface(EGL10 egl, EGLDisplay display, EGLSurface surface);

        android.opengl.EGLSurface createWindowSurface(android.opengl.EGLDisplay display, android.opengl.EGLConfig config,
                                                      Object nativeWindow);

        void destroySurface(android.opengl.EGLDisplay display, android.opengl.EGLSurface surface);
    }

    public static class DefaultWindowSurfaceFactory implements EGLWindowSurfaceFactory {

        @Override
        public EGLSurface createWindowSurface(EGL10 egl, EGLDisplay display,
                                              EGLConfig config, Object nativeWindow) {

            int[] surfaceAttribs = {
                    EGL10.EGL_NONE
            };
            EGLSurface result = null;
            try {
                result = egl.eglCreateWindowSurface(display, config, nativeWindow, surfaceAttribs);
            } catch (IllegalArgumentException e) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e("DefaultWindow", "eglCreateWindowSurface", e);
            }
            return result;
        }

        @Override
        public void destroySurface(EGL10 egl, EGLDisplay display,
                                   EGLSurface surface) {
            egl.eglDestroySurface(display, surface);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public android.opengl.EGLSurface createWindowSurface(android.opengl.EGLDisplay display, android.opengl.EGLConfig config, Object nativeWindow) {
            int[] surfaceAttribs = {
                    EGL14.EGL_NONE
            };
            android.opengl.EGLSurface result = null;
            try {
                result = EGL14.eglCreateWindowSurface(display, config, nativeWindow, surfaceAttribs, 0);
            } catch (IllegalArgumentException e) {
                // This exception indicates that the surface flinger surface
                // is not valid. This can happen if the surface flinger surface has
                // been torn down, but the application has not yet been
                // notified via SurfaceHolder.Callback.surfaceDestroyed.
                // In theory the application should be notified first,
                // but in practice sometimes it is not. See b/4588890
                Log.e("DefaultWindow", "eglCreateWindowSurface", e);
            }
            return result;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void destroySurface(android.opengl.EGLDisplay display, android.opengl.EGLSurface surface) {
            EGL14.eglDestroySurface(display, surface);
        }
    }

    public static class Builder {
        private EGLConfigChooser configChooser;
        private EGLContextFactory eglContextFactory;
        private EGLWindowSurfaceFactory eglWindowSurfaceFactory;
        private GLViewRenderer renderer;
        private int eglContextClientVersion = 2;
        private int renderMode = RENDERMODE_WHEN_DIRTY;
        private Object surface;
        private EglContextWrapper eglContext = EglContextWrapper.EGL_NO_CONTEXT_WRAPPER;

        public Builder setSurface(Object surface) {
            this.surface = surface;
            return this;
        }


        public Builder setEGLConfigChooser(boolean needDepth) {
            setEGLConfigChooser(new SimpleEGLConfigChooser(needDepth, eglContextClientVersion));
            return this;
        }


        public Builder setEGLConfigChooser(EGLConfigChooser configChooser) {
            this.configChooser = configChooser;
            return this;
        }

        public Builder setEGLConfigChooser(int redSize, int greenSize, int blueSize,
                                           int alphaSize, int depthSize, int stencilSize) {
            setEGLConfigChooser(new ComponentSizeChooser(redSize, greenSize,
                    blueSize, alphaSize, depthSize, stencilSize, eglContextClientVersion));
            return this;
        }

        public Builder setEglContextFactory(EGLContextFactory eglContextFactory) {
            this.eglContextFactory = eglContextFactory;
            return this;
        }

        public Builder setEglWindowSurfaceFactory(EGLWindowSurfaceFactory eglWindowSurfaceFactory) {
            this.eglWindowSurfaceFactory = eglWindowSurfaceFactory;
            return this;
        }

        public Builder setRenderer(GLViewRenderer renderer) {
            this.renderer = renderer;
            return this;
        }

        public Builder setGLWrapper(GLWrapper mGLWrapper) {
            return this;
        }

        public Builder setEglContextClientVersion(int eglContextClientVersion) {
            this.eglContextClientVersion = eglContextClientVersion;
            return this;
        }

        public Builder setRenderMode(int renderMode) {
            this.renderMode = renderMode;
            return this;
        }

        public Builder setSharedEglContext(@NonNull EglContextWrapper sharedEglContext) {
            this.eglContext = sharedEglContext;
            return this;
        }

        public GLThread createGLThread() {
            if (renderer == null) {
                throw new NullPointerException("renderer has not been set");
            }
            if (surface == null && eglWindowSurfaceFactory == null) {
                throw new NullPointerException("surface has not been set");
            }
            if (configChooser == null) {
                configChooser = new SimpleEGLConfigChooser(true, eglContextClientVersion);
            }
            if (eglContextFactory == null) {
                eglContextFactory = new DefaultContextFactory(eglContextClientVersion);
            }
            if (eglWindowSurfaceFactory == null) {
                eglWindowSurfaceFactory = new DefaultWindowSurfaceFactory();
            }
            return new GLThread(configChooser, eglContextFactory, eglWindowSurfaceFactory, renderer, renderMode, surface, eglContext);
        }
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static class ChoreographerRender implements Choreographer.FrameCallback {

        private GLThread glThread;
        private boolean canSwap = true;

        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
        public ChoreographerRender(GLThread glThread) {
            this.glThread = glThread;
        }

        @Override
        public void doFrame(long frameTimeNanos) {
            if (glThread.getRenderMode() == RENDERMODE_CONTINUOUSLY) {
                canSwap = true;
                glThread.requestRender(frameTimeNanos);
                Choreographer.getInstance().postFrameCallback(this);
            }
        }

        public void start() {
            Choreographer.getInstance().postFrameCallback(this);
        }

        public void stop() {
            Choreographer.getInstance().removeFrameCallback(this);
        }

        public void setCanSwap(boolean canSwap) {
            this.canSwap = canSwap;
        }

        public boolean isCanSwap() {
            return canSwap || glThread.getRenderMode() == RENDERMODE_WHEN_DIRTY;
        }
    }

    public static class ChoreographerRenderWrapper {

        private ChoreographerRender choreographerRender = null;

        public ChoreographerRenderWrapper(GLThread glThread) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                choreographerRender = new ChoreographerRender(glThread);
            }
        }

        public void start() {
            if (choreographerRender != null) {
                choreographerRender.start();
            }
        }

        public void stop() {
            if (choreographerRender != null) {
                choreographerRender.stop();
            }
        }

        public boolean canSwap() {
            if (choreographerRender != null) {
                return choreographerRender.isCanSwap();
            }
            return true;
        }

        public void disableSwap() {
            if (choreographerRender != null) {
                choreographerRender.setCanSwap(false);
            }
        }
    }
}
