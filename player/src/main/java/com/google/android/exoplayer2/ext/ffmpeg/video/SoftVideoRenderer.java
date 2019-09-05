/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.exoplayer2.ext.ffmpeg.video;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.support.annotation.IntDef;
import android.util.Log;
import android.view.Surface;

import com.google.android.exoplayer2.BaseRenderer;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.FormatHolder;
import com.google.android.exoplayer2.decoder.DecoderCounters;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSession.DrmSessionException;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.ext.ffmpeg.DecoderSoLibrary;
import com.google.android.exoplayer2.ext.ffmpeg.exception.VideoSoftDecoderException;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.TraceUtil;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoRendererEventListener;
import com.google.android.exoplayer2.video.VideoRendererEventListener.EventDispatcher;
import com.xyoye.player.exoplayer.meida.egl.GLThread;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Decodes and renders video using the ffmpeg videoDecoder.
 */
public final class SoftVideoRenderer extends BaseRenderer {
  @SuppressWarnings("unused")
  private static final String TAG = "SoftVideoRenderer";

  @Retention(RetentionPolicy.SOURCE)
  @IntDef({REINITIALIZATION_STATE_NONE, REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM,
      REINITIALIZATION_STATE_WAIT_END_OF_STREAM})
  private @interface ReinitializationState {}
  /**
   * The videoDecoder does not need to be re-initialized.
   */
  private static final int REINITIALIZATION_STATE_NONE = 0;
  /**
   * The input format has changed in a way that requires the videoDecoder to be re-initialized, but we
   * haven't yet signaled an end of stream to the existing videoDecoder. We need to do so in order to
   * ensure that it outputs any remaining buffers before we release it.
   */
  private static final int REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM = 1;
  /**
   * The input format has changed in a way that requires the videoDecoder to be re-initialized, and we've
   * signaled an end of stream to the existing videoDecoder. We're waiting for the videoDecoder to output an
   * end of stream signal to indicate that it has output any remaining buffers before we release it.
   */
  private static final int REINITIALIZATION_STATE_WAIT_END_OF_STREAM = 2;

  /**
   * The number of input buffers.
   */
  private static final int NUM_INPUT_BUFFERS = 8;
  /**
   * The number of output buffers. The renderer may limit the minimum possible value due to
   * requiring multiple output buffers to be dequeued at a time for it to make progress.
   */
  private static final int NUM_OUTPUT_BUFFERS = 16;
  /**
   * The initial input buffer size. Input buffers are reallocated dynamically if this value is
   * insufficient.
   */
  private static final int INITIAL_INPUT_BUFFER_SIZE = 768 * 1024; // Value based on cs/SoftFFmpeg.cpp.

  private final boolean scaleToFit;
  private final long allowedJoiningTimeMs;
  private final int maxDroppedFramesToNotify;
  private final boolean playClearSamplesWithoutKeys;
  private final EventDispatcher eventDispatcher;
  private final FormatHolder formatHolder;
  private final DecoderInputBuffer flagsOnlyBuffer;
  private final DrmSessionManager<FrameworkMediaCrypto> drmSessionManager;

  private DecoderCounters decoderCounters;
  private Format format;
  private VideoDecoder videoDecoder;
  private PacketBuffer inputBuffer;
  private FrameBuffer outputBuffer;
  private FrameBuffer nextOutputBuffer;
  private DrmSession<FrameworkMediaCrypto> drmSession;
  private DrmSession<FrameworkMediaCrypto> pendingDrmSession;

  private @ReinitializationState int decoderReinitializationState;
  private boolean decoderReceivedBuffers;

  private Bitmap bitmap;
  private boolean renderedFirstFrame;
  private boolean forceRenderFrame;
  private long joiningDeadlineMs;
  private Surface surface;
  private int surfaceWidth = -1;
  private int surfaceHeight = -1;
  private FrameRenderer outputBufferRenderer;
  private GLThread glThread;
  private boolean waitingForKeys;

  private boolean inputStreamEnded;
  private boolean outputStreamEnded;
  private int reportedWidth;
  private int reportedHeight;

  private long droppedFrameAccumulationStartTimeMs;
  private int droppedFrames;
  private int consecutiveDroppedFrameCount;
  private int buffersInCodecCount;

  /**
   * @param scaleToFit Whether video frames should be scaled to fit when rendering.
   * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
   *     can attempt to seamlessly join an ongoing playback.
   */
  public SoftVideoRenderer(boolean scaleToFit, long allowedJoiningTimeMs) {
    this(scaleToFit, allowedJoiningTimeMs, null, null, 0);
  }

  /**
   * @param scaleToFit Whether video frames should be scaled to fit when rendering.
   * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
   *     can attempt to seamlessly join an ongoing playback.
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
   *     invocations of {@link VideoRendererEventListener#onDroppedFrames(int, long)}.
   */
  public SoftVideoRenderer(boolean scaleToFit, long allowedJoiningTimeMs,
                           Handler eventHandler, VideoRendererEventListener eventListener,
                           int maxDroppedFramesToNotify) {
    this(scaleToFit, allowedJoiningTimeMs, eventHandler, eventListener, maxDroppedFramesToNotify,
        null, false);
  }

  /**
   * @param scaleToFit Whether video frames should be scaled to fit when rendering.
   * @param allowedJoiningTimeMs The maximum duration in milliseconds for which this video renderer
   *     can attempt to seamlessly join an ongoing playback.
   * @param eventHandler A handler to use when delivering events to {@code eventListener}. May be
   *     null if delivery of events is not required.
   * @param eventListener A listener of events. May be null if delivery of events is not required.
   * @param maxDroppedFramesToNotify The maximum number of frames that can be dropped between
   *     invocations of {@link VideoRendererEventListener#onDroppedFrames(int, long)}.
   * @param drmSessionManager For use with encrypted media. May be null if support for encrypted
   *     media is not required.
   * @param playClearSamplesWithoutKeys Encrypted media may contain clear (un-encrypted) regions.
   *     For example a media file may start with a short clear region so as to allow playback to
   *     begin in parallel with key acquisition. This parameter specifies whether the renderer is
   *     permitted to play clear regions of encrypted media files before {@code drmSessionManager}
   *     has obtained the keys necessary to decrypt encrypted regions of the media.
   */
  public SoftVideoRenderer(boolean scaleToFit, long allowedJoiningTimeMs,
                           Handler eventHandler, VideoRendererEventListener eventListener,
                           int maxDroppedFramesToNotify, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager,
                           boolean playClearSamplesWithoutKeys) {
    super(C.TRACK_TYPE_VIDEO);
    this.scaleToFit = scaleToFit;
    this.allowedJoiningTimeMs = allowedJoiningTimeMs;
    this.maxDroppedFramesToNotify = maxDroppedFramesToNotify;
    this.drmSessionManager = drmSessionManager;
    this.playClearSamplesWithoutKeys = playClearSamplesWithoutKeys;
    this.outputBufferRenderer = new FrameRenderer();
    joiningDeadlineMs = C.TIME_UNSET;
    clearReportedVideoSize();
    formatHolder = new FormatHolder();
    flagsOnlyBuffer = DecoderInputBuffer.newFlagsOnlyInstance();
    eventDispatcher = new EventDispatcher(eventHandler, eventListener);
    decoderReinitializationState = REINITIALIZATION_STATE_NONE;
  }

  @Override
  public int supportsFormat(Format format) {
    if (!DecoderSoLibrary.isAvailable() ||
            !(MimeTypes.VIDEO_MP4.equalsIgnoreCase(format.sampleMimeType)
            || MimeTypes.VIDEO_H264.equalsIgnoreCase(format.sampleMimeType)
            || MimeTypes.VIDEO_H265.equalsIgnoreCase(format.sampleMimeType)
            || MimeTypes.VIDEO_MPEG.equalsIgnoreCase(format.sampleMimeType)
            || MimeTypes.VIDEO_MPEG2.equalsIgnoreCase(format.sampleMimeType))
            ) {
      return FORMAT_UNSUPPORTED_TYPE;
    } else if (!supportsFormatDrm(drmSessionManager, format.drmInitData)) {
      return FORMAT_UNSUPPORTED_DRM;
    }

    // ffmpeg解码需要
    if (format.initializationData == null || format.initializationData.size() <= 0) {
      return FORMAT_EXCEEDS_CAPABILITIES;
    }

    return FORMAT_HANDLED | ADAPTIVE_SEAMLESS;
  }

  @Override
  public void render(long positionUs, long elapsedRealtimeUs) throws ExoPlaybackException {
    if (outputStreamEnded) {
      return;
    }

    if (format == null) {
      // We don't have a format yet, so try and read one.
      flagsOnlyBuffer.clear();
      int result = readSource(formatHolder, flagsOnlyBuffer, true);
      if (result == C.RESULT_FORMAT_READ) {
        onInputFormatChanged(formatHolder.format);
      } else if (result == C.RESULT_BUFFER_READ) {
        // End of stream read having not read a format.
        Assertions.checkState(flagsOnlyBuffer.isEndOfStream());
        inputStreamEnded = true;
        outputStreamEnded = true;
        return;
      } else {
        // We still don't have a format and can't make progress without one.
        return;
      }
    }

    // If we don't have a videoDecoder yet, we need to instantiate one.
    maybeInitDecoder();

    if (videoDecoder != null) {
      try {
        // Rendering loop.
        TraceUtil.beginSection("drainAndFeed");
        while (drainOutputBuffer(positionUs)) {}
        while (feedInputBuffer()) {}
        TraceUtil.endSection();
      } catch (VideoSoftDecoderException e) {
        throw ExoPlaybackException.createForRenderer(e, getIndex());
      }
      decoderCounters.ensureUpdated();
    }
  }

  private boolean drainOutputBuffer(long positionUs) throws ExoPlaybackException,
          VideoSoftDecoderException {
    // Acquire outputBuffer either from nextOutputBuffer or from the videoDecoder.
    if (outputBuffer == null) {
      if (nextOutputBuffer != null) {
        outputBuffer = nextOutputBuffer;
        nextOutputBuffer = null;
      } else {
        outputBuffer = videoDecoder.dequeueOutputBuffer();
      }
      if (outputBuffer == null) {
        return false;
      }
      decoderCounters.skippedOutputBufferCount += outputBuffer.skippedOutputBufferCount;
      buffersInCodecCount -= outputBuffer.skippedOutputBufferCount;
    }

    if (nextOutputBuffer == null) {
      nextOutputBuffer = videoDecoder.dequeueOutputBuffer();
    }

    if (outputBuffer.isEndOfStream()) {
      if (decoderReinitializationState == REINITIALIZATION_STATE_WAIT_END_OF_STREAM) {
        // We're waiting to re-initialize the videoDecoder, and have now processed all final buffers.
        releaseDecoder();
        maybeInitDecoder();
      } else {
        outputBuffer.release();
        outputBuffer = null;
        outputStreamEnded = true;
      }
      return false;
    }

    if (surface == null) {
      // Skip frames in sync with playback, so we'll be at the right frame if the mode changes.
      if (isBufferLate(outputBuffer.timeUs - positionUs)) {
        forceRenderFrame = false;
        skipBuffer();
        buffersInCodecCount--;
        return true;
      }
      return false;
    }

    if (forceRenderFrame) {
      forceRenderFrame = false;
      renderBuffer();
      buffersInCodecCount--;
      return true;
    }

    final long nextOutputBufferTimeUs =
        nextOutputBuffer != null && !nextOutputBuffer.isEndOfStream()
            ? nextOutputBuffer.timeUs : C.TIME_UNSET;

    long earlyUs = outputBuffer.timeUs - positionUs;
    if (shouldDropBuffersToKeyframe(earlyUs) && maybeDropBuffersToKeyframe(positionUs)) {
      forceRenderFrame = true;
      return false;
    } else if (shouldDropOutputBuffer(
        outputBuffer.timeUs, nextOutputBufferTimeUs, positionUs, joiningDeadlineMs)) {
      dropBuffer();
      buffersInCodecCount--;
      return true;
    }

    // If we have yet to render a frame to the current output (either initially or immediately
    // following a seek), render one irrespective of the state or current position.
    if (!renderedFirstFrame
        || (getState() == STATE_STARTED && earlyUs <= 30000)) {
      renderBuffer();
      buffersInCodecCount--;
    }
    return false;
  }

  /**
   * Returns whether the current frame should be dropped.
   *
   * @param outputBufferTimeUs The timestamp of the current output buffer.
   * @param nextOutputBufferTimeUs The timestamp of the next output buffer or {@link C#TIME_UNSET}
   *     if the next output buffer is unavailable.
   * @param positionUs The current playback position.
   * @param joiningDeadlineMs The joining deadline.
   * @return Returns whether to drop the current output buffer.
   */
  private boolean shouldDropOutputBuffer(long outputBufferTimeUs, long nextOutputBufferTimeUs,
      long positionUs, long joiningDeadlineMs) {
    return isBufferLate(outputBufferTimeUs - positionUs)
        && (joiningDeadlineMs != C.TIME_UNSET || nextOutputBufferTimeUs != C.TIME_UNSET);
  }

  /**
   * Returns whether to drop all buffers from the buffer being processed to the keyframe at or after
   * the current playback position, if possible.
   *
   * @param earlyUs The time until the current buffer should be presented in microseconds. A
   *     negative value indicates that the buffer is late.
   */
  private boolean shouldDropBuffersToKeyframe(long earlyUs) {
    return isBufferVeryLate(earlyUs);
  }

  private void renderBuffer() {
    // 软解带endofstream标志的buffer是没有实际数据的
    if (outputBuffer.isEndOfStream()) {
      outputBuffer = null;
      return;
    }

    if (surface != null) {
      maybeNotifyVideoSizeChanged(outputBuffer.width, outputBuffer.height);
      // The renderer will release the buffer.
      outputBufferRenderer.setOutputBuffer(outputBuffer);
      if (glThread != null) {
        glThread.requestRender();
      }
      outputBuffer = null;
      consecutiveDroppedFrameCount = 0;
      decoderCounters.renderedOutputBufferCount++;
      maybeNotifyRenderedFirstFrame();
    } else {
      dropBuffer();
    }
  }

  private void dropBuffer() {
    updateDroppedBufferCounters(1);
    outputBuffer.release();
    outputBuffer = null;
  }

  private boolean maybeDropBuffersToKeyframe(long positionUs) throws ExoPlaybackException {
    int droppedSourceBufferCount = skipSource(positionUs);
    if (droppedSourceBufferCount == 0) {
      return false;
    }
    decoderCounters.droppedToKeyframeCount++;
    // We dropped some buffers to catch up, so update the videoDecoder counters and flush the codec,
    // which releases all pending buffers buffers including the current output buffer.
    updateDroppedBufferCounters(buffersInCodecCount + droppedSourceBufferCount);
    flushDecoder();
    return true;
  }

  private void updateDroppedBufferCounters(int droppedBufferCount) {
    decoderCounters.droppedBufferCount += droppedBufferCount;
    droppedFrames += droppedBufferCount;
    consecutiveDroppedFrameCount += droppedBufferCount;
    decoderCounters.maxConsecutiveDroppedBufferCount = Math.max(consecutiveDroppedFrameCount,
        decoderCounters.maxConsecutiveDroppedBufferCount);
    if (droppedFrames >= maxDroppedFramesToNotify) {
      maybeNotifyDroppedFrames();
    }
  }

  private void skipBuffer() {
    decoderCounters.skippedOutputBufferCount++;
    outputBuffer.release();
    outputBuffer = null;
  }

  private void renderRgbFrame(FrameBuffer outputBuffer, boolean scale) {
    if (bitmap == null || bitmap.getWidth() != outputBuffer.width
        || bitmap.getHeight() != outputBuffer.height) {
      bitmap = Bitmap.createBitmap(outputBuffer.width, outputBuffer.height, Bitmap.Config.RGB_565);
    }
    bitmap.copyPixelsFromBuffer(outputBuffer.data);
    Canvas canvas = surface.lockCanvas(null);
    if (scale) {
      canvas.scale(((float) canvas.getWidth()) / outputBuffer.width,
          ((float) canvas.getHeight()) / outputBuffer.height);
    }
    canvas.drawBitmap(bitmap, 0, 0, null);
    surface.unlockCanvasAndPost(canvas);
  }

  private boolean feedInputBuffer() throws VideoSoftDecoderException, ExoPlaybackException {
    if (videoDecoder == null || decoderReinitializationState == REINITIALIZATION_STATE_WAIT_END_OF_STREAM
        || inputStreamEnded) {
      // We need to reinitialize the videoDecoder or the input stream has ended.
      return false;
    }

    if (inputBuffer == null) {
      inputBuffer = videoDecoder.dequeueInputBuffer();
      if (inputBuffer == null) {
        return false;
      }
    }

    if (decoderReinitializationState == REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM) {
      inputBuffer.setFlags(C.BUFFER_FLAG_END_OF_STREAM);
      videoDecoder.queueInputBuffer(inputBuffer);
      inputBuffer = null;
      decoderReinitializationState = REINITIALIZATION_STATE_WAIT_END_OF_STREAM;
      return false;
    }

    int result;
    if (waitingForKeys) {
      // We've already read an encrypted sample into buffer, and are waiting for keys.
      result = C.RESULT_BUFFER_READ;
    } else {
      result = readSource(formatHolder, inputBuffer, false);
    }

    if (result == C.RESULT_NOTHING_READ) {
      return false;
    }
    if (result == C.RESULT_FORMAT_READ) {
      onInputFormatChanged(formatHolder.format);
      return true;
    }
    if (inputBuffer.isEndOfStream()) {
      inputStreamEnded = true;
      videoDecoder.queueInputBuffer(inputBuffer);
      inputBuffer = null;
      return false;
    }
    boolean bufferEncrypted = inputBuffer.isEncrypted();
    waitingForKeys = shouldWaitForKeys(bufferEncrypted);
    if (waitingForKeys) {
      return false;
    }
    inputBuffer.flip();
    inputBuffer.colorInfo = formatHolder.format.colorInfo;
    videoDecoder.queueInputBuffer(inputBuffer);
    buffersInCodecCount++;
    decoderReceivedBuffers = true;
    decoderCounters.inputBufferCount++;
    inputBuffer = null;
    return true;
  }

  private boolean shouldWaitForKeys(boolean bufferEncrypted) throws ExoPlaybackException {
    if (drmSession == null || (!bufferEncrypted && playClearSamplesWithoutKeys)) {
      return false;
    }
    @DrmSession.State int drmSessionState = drmSession.getState();
    if (drmSessionState == DrmSession.STATE_ERROR) {
      throw ExoPlaybackException.createForRenderer(drmSession.getError(), getIndex());
    }
    return drmSessionState != DrmSession.STATE_OPENED_WITH_KEYS;
  }

  private void flushDecoder() throws ExoPlaybackException {
    waitingForKeys = false;
    forceRenderFrame = false;
    buffersInCodecCount = 0;
    if (decoderReinitializationState != REINITIALIZATION_STATE_NONE) {
      releaseDecoder();
      maybeInitDecoder();
    } else {
      inputBuffer = null;
      if (outputBuffer != null) {
        outputBuffer.release();
        outputBuffer = null;
      }
      if (nextOutputBuffer != null) {
        nextOutputBuffer.release();
        nextOutputBuffer = null;
      }
      videoDecoder.flush();
      decoderReceivedBuffers = false;
    }
  }

  @Override
  public boolean isEnded() {
    return outputStreamEnded;
  }

  @Override
  public boolean isReady() {
    if (waitingForKeys) {
      return false;
    }
    if (format != null && (isSourceReady() || outputBuffer != null)
        && (renderedFirstFrame || surface == null)) {
      // Ready. If we were joining then we've now joined, so clear the joining deadline.
      joiningDeadlineMs = C.TIME_UNSET;
      return true;
    } else if (joiningDeadlineMs == C.TIME_UNSET) {
      // Not joining.
      return false;
    } else if (SystemClock.elapsedRealtime() < joiningDeadlineMs) {
      // Joining and still within the joining deadline.
      return true;
    } else {
      // The joining deadline has been exceeded. Give up and clear the deadline.
      joiningDeadlineMs = C.TIME_UNSET;
      return false;
    }
  }

  @Override
  protected void onEnabled(boolean joining) throws ExoPlaybackException {
    Log.d(TAG, "onEnabled");

    decoderCounters = new DecoderCounters();
    eventDispatcher.enabled(decoderCounters);
  }

  @Override
  protected void onPositionReset(long positionUs, boolean joining) throws ExoPlaybackException {
    Log.d(TAG, "onPositionReset");

    inputStreamEnded = false;
    outputStreamEnded = false;
    clearRenderedFirstFrame();
    consecutiveDroppedFrameCount = 0;
    if (videoDecoder != null) {
      flushDecoder();
    }
    if (joining) {
      setJoiningDeadlineMs();
    } else {
      joiningDeadlineMs = C.TIME_UNSET;
    }
  }

  @Override
  protected void onStarted() {
    Log.d(TAG, "onStarted");

    droppedFrames = 0;
    droppedFrameAccumulationStartTimeMs = SystemClock.elapsedRealtime();

    if (glThread != null) {
      glThread.onResume();
    }
  }

  @Override
  protected void onStopped() {
    Log.d(TAG, "onStopped");

    joiningDeadlineMs = C.TIME_UNSET;
    maybeNotifyDroppedFrames();

    if (glThread != null) {
      glThread.onPause();
    }
  }

  @Override
  protected void finalize() throws Throwable {
    if (glThread != null) {
      glThread.surfaceDestroyed();
      glThread.requestExitAndWait();
      glThread = null;
    }
    super.finalize();
  }

  @Override
  protected void onDisabled() {
    Log.d(TAG, "onDisabled");

    format = null;
    waitingForKeys = false;
    clearReportedVideoSize();
    clearRenderedFirstFrame();
    try {
      releaseDecoder();
    } finally {
      try {
        if (drmSession != null) {
          drmSessionManager.releaseSession(drmSession);
        }
      } finally {
        try {
          if (pendingDrmSession != null && pendingDrmSession != drmSession) {
            drmSessionManager.releaseSession(pendingDrmSession);
          }
        } finally {
          drmSession = null;
          pendingDrmSession = null;
          decoderCounters.ensureUpdated();
          eventDispatcher.disabled(decoderCounters);
        }
      }
    }
  }

  private void maybeInitDecoder() throws ExoPlaybackException {
    if (videoDecoder != null) {
      return;
    }

    drmSession = pendingDrmSession;
    ExoMediaCrypto mediaCrypto = null;
    if (drmSession != null) {
      mediaCrypto = drmSession.getMediaCrypto();
      if (mediaCrypto == null) {
        DrmSessionException drmError = drmSession.getError();
        if (drmError != null) {
          throw ExoPlaybackException.createForRenderer(drmError, getIndex());
        }
        // The drm session isn't open yet.
        return;
      }
    }

    try {
      long codecInitializingTimestamp = SystemClock.elapsedRealtime();
      TraceUtil.beginSection("createFFmpegDecoder");
      videoDecoder = new VideoDecoder(format, NUM_INPUT_BUFFERS, NUM_OUTPUT_BUFFERS, INITIAL_INPUT_BUFFER_SIZE,
          mediaCrypto);
      TraceUtil.endSection();
      long codecInitializedTimestamp = SystemClock.elapsedRealtime();
      eventDispatcher.decoderInitialized(videoDecoder.getName(), codecInitializedTimestamp,
          codecInitializedTimestamp - codecInitializingTimestamp);
      decoderCounters.decoderInitCount++;
    } catch (Exception e) {
      throw ExoPlaybackException.createForRenderer(e, getIndex());
    }
  }

  private void releaseDecoder() {
    if (videoDecoder == null) {
      return;
    }

    inputBuffer = null;
    outputBuffer = null;
    nextOutputBuffer = null;
    videoDecoder.release();
    videoDecoder = null;
    decoderCounters.decoderReleaseCount++;
    decoderReinitializationState = REINITIALIZATION_STATE_NONE;
    decoderReceivedBuffers = false;
    forceRenderFrame = false;
    buffersInCodecCount = 0;
  }

  private void onInputFormatChanged(Format newFormat) throws ExoPlaybackException {
    Log.d(TAG, "onInputFormatChanged:" + newFormat.toString());
    Format oldFormat = format;
    format = newFormat;

    boolean drmInitDataChanged = !Util.areEqual(format.drmInitData, oldFormat == null ? null
        : oldFormat.drmInitData);
    if (drmInitDataChanged) {
      if (format.drmInitData != null) {
        if (drmSessionManager == null) {
          throw ExoPlaybackException.createForRenderer(
              new IllegalStateException("Media requires a DrmSessionManager"), getIndex());
        }
        pendingDrmSession = drmSessionManager.acquireSession(Looper.myLooper(), format.drmInitData);
        if (pendingDrmSession == drmSession) {
          drmSessionManager.releaseSession(pendingDrmSession);
        }
      } else {
        pendingDrmSession = null;
      }
    }

    boolean initializationDataChanged = !Util.areEqual(format.initializationData, oldFormat == null ? null
            : oldFormat.initializationData);

    if (initializationDataChanged || pendingDrmSession != drmSession) {
      if (decoderReceivedBuffers) {
        // Signal end of stream and wait for any final output buffers before re-initialization.
        decoderReinitializationState = REINITIALIZATION_STATE_SIGNAL_END_OF_STREAM;
      } else {
        // There aren't any final output buffers, so release the videoDecoder immediately.
        releaseDecoder();
        maybeInitDecoder();
      }
    }

    eventDispatcher.inputFormatChanged(format);
  }

  @Override
  public void handleMessage(int messageType, Object message) throws ExoPlaybackException {
    if (messageType == C.MSG_SET_SURFACE) {
      setOutput((Surface) message);
    } else if (messageType == Constant.MSG_SURFACE_SIZE_CHANGED) {
      Point size = (Point) message;
      onSurfaceSizeChanged(size.x, size.y);
    } else if (messageType == Constant.MSG_PLAY_RELEASED) {
      onPlayReleased();
    } else {
      super.handleMessage(messageType, message);
    }
  }

  private void onPlayReleased() {
    if (glThread != null) {
      glThread.surfaceDestroyed();
      glThread.requestExitAndWait();
      glThread = null;
    }
  }

  private void setOutput(Surface surface) {
    if (this.surface != surface) {
      // The output has changed.
      Surface oldSurface = this.surface;
      this.surface = surface;
      onSurfaceChanged(surface, oldSurface);

      if (surface != null) {
        // If we know the video size, report it again immediately.
        maybeRenotifyVideoSizeChanged();
        // We haven't rendered to the new output yet.
        clearRenderedFirstFrame();
        if (getState() == STATE_STARTED) {
          setJoiningDeadlineMs();
        }
      } else {
        // The output has been removed. We leave the outputMode of the underlying videoDecoder unchanged
        // in anticipation that a subsequent output will likely be of the same type.
        clearReportedVideoSize();
        clearRenderedFirstFrame();
      }
    } else {
      // The output is unchanged and non-null. If we know the video size and/or have already
      // rendered to the output, report these again immediately.
      maybeRenotifyVideoSizeChanged();
      maybeRenotifyRenderedFirstFrame();
    }
  }

  private void onSurfaceChanged(Surface newSurface, Surface oldSurface) {
    if (glThread == null) {
      GLThread.Builder builder = new GLThread.Builder();
      builder.setSurface(newSurface).setRenderer(outputBufferRenderer);
      glThread = builder.createGLThread();
      glThread.start();
    } else {
      glThread.setSurface(newSurface);
    }

    if (newSurface != null) {
      glThread.surfaceCreated();
    } else {
      glThread.surfaceDestroyed();
    }
  }

  private void onSurfaceSizeChanged(int width, int height) {
    if (glThread != null) {
      glThread.onWindowResize(width, height);
    }
    surfaceWidth = width;
    surfaceHeight = height;
  }

  private void setJoiningDeadlineMs() {
    joiningDeadlineMs = allowedJoiningTimeMs > 0
        ? (SystemClock.elapsedRealtime() + allowedJoiningTimeMs) : C.TIME_UNSET;
  }

  private void clearRenderedFirstFrame() {
    renderedFirstFrame = false;
  }

  private void maybeNotifyRenderedFirstFrame() {
    if (!renderedFirstFrame) {
      renderedFirstFrame = true;
      eventDispatcher.renderedFirstFrame(surface);
    }
  }

  private void maybeRenotifyRenderedFirstFrame() {
    if (renderedFirstFrame) {
      eventDispatcher.renderedFirstFrame(surface);
    }
  }

  private void clearReportedVideoSize() {
    reportedWidth = Format.NO_VALUE;
    reportedHeight = Format.NO_VALUE;
  }

  private void maybeNotifyVideoSizeChanged(int width, int height) {
    if (reportedWidth != width || reportedHeight != height) {
      reportedWidth = width;
      reportedHeight = height;
      eventDispatcher.videoSizeChanged(width, height, 0, 1);
    }
  }

  private void maybeRenotifyVideoSizeChanged() {
    if (reportedWidth != Format.NO_VALUE || reportedHeight != Format.NO_VALUE) {
      eventDispatcher.videoSizeChanged(reportedWidth, reportedHeight, 0, 1);
    }
  }

  private void maybeNotifyDroppedFrames() {
    if (droppedFrames > 0) {
      long now = SystemClock.elapsedRealtime();
      long elapsedMs = now - droppedFrameAccumulationStartTimeMs;
      eventDispatcher.droppedFrames(droppedFrames, elapsedMs);
      droppedFrames = 0;
      droppedFrameAccumulationStartTimeMs = now;
    }
  }

  private static boolean isBufferLate(long earlyUs) {
    // Class a buffer as late if it should have been presented more than 30 ms ago.
    return earlyUs < -30000;
  }

  private static boolean isBufferVeryLate(long earlyUs) {
    // Class a buffer as very late if it should have been presented more than 500 ms ago.
    return earlyUs < -500000;
  }

}
