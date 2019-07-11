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

import android.annotation.SuppressLint;

import com.google.android.exoplayer2.decoder.Decoder;
import com.google.android.exoplayer2.ext.ffmpeg.exception.VideoSoftDecoderException;
import com.google.android.exoplayer2.util.Assertions;

import java.util.LinkedList;

public abstract class BaseDecoder implements Decoder<PacketBuffer, FrameBuffer, VideoSoftDecoderException> {

  private final Thread decodeThread;

  private final Object lock;
  private final LinkedList<PacketBuffer> queuedInputBuffers;
  private final LinkedList<FrameBuffer> queuedOutputBuffers;
  private final PacketBuffer[] availableInputBuffers;
  private final FrameBuffer[] availableOutputBuffers;

  private int availableInputBufferCount;
  private int availableOutputBufferCount;
  private PacketBuffer dequeuedInputBuffer;

  private VideoSoftDecoderException exception;
  private boolean flushed;
  private boolean released;
  private int skippedOutputBufferCount;

  private boolean maybeHasFrame = false;

  /**
   * @param inputBuffers An array of nulls that will be used to store references to input buffers.
   * @param outputBuffers An array of nulls that will be used to store references to output buffers.
   */
  protected BaseDecoder(PacketBuffer[] inputBuffers, FrameBuffer[] outputBuffers) {
    lock = new Object();
    queuedInputBuffers = new LinkedList<>();
    queuedOutputBuffers = new LinkedList<>();
    availableInputBuffers = inputBuffers;
    availableInputBufferCount = inputBuffers.length;
    for (int i = 0; i < availableInputBufferCount; i++) {
      availableInputBuffers[i] = createInputBuffer();
    }
    availableOutputBuffers = outputBuffers;
    availableOutputBufferCount = outputBuffers.length;
    for (int i = 0; i < availableOutputBufferCount; i++) {
      availableOutputBuffers[i] = createOutputBuffer();
    }
    decodeThread = new Thread("ffmpeg-decoder") {
      @Override
      public void run() {
        BaseDecoder.this.run();
      }
    };
    decodeThread.start();
  }

  /**
   * Sets the initial size of each input buffer.
   * <p>
   * This method should only be called before the decoder is used (i.e. before the first call to
   * {@link #dequeueInputBuffer()}.
   *
   * @param size The required input buffer size.
   */
  protected final void setInitialInputBufferSize(int size) {
    Assertions.checkState(availableInputBufferCount == availableInputBuffers.length);
    for (PacketBuffer inputBuffer : availableInputBuffers) {
      inputBuffer.ensureSpaceForWrite(size);
    }
  }

  @Override
  public final PacketBuffer dequeueInputBuffer() throws VideoSoftDecoderException {
    synchronized (lock) {
      maybeThrowException();
      Assertions.checkState(dequeuedInputBuffer == null);
      dequeuedInputBuffer = availableInputBufferCount == 0 ? null
          : availableInputBuffers[--availableInputBufferCount];
      return dequeuedInputBuffer;
    }
  }

  @Override
  public final void queueInputBuffer(PacketBuffer inputBuffer) throws VideoSoftDecoderException {
    synchronized (lock) {
      maybeThrowException();
      Assertions.checkArgument(inputBuffer == dequeuedInputBuffer);
      queuedInputBuffers.addLast(inputBuffer);
      maybeNotifyDecodeLoop();
      dequeuedInputBuffer = null;
    }
  }

  @Override
  public final FrameBuffer dequeueOutputBuffer() throws VideoSoftDecoderException {
    synchronized (lock) {
      maybeThrowException();
      if (queuedOutputBuffers.isEmpty()) {
        return null;
      }
      return queuedOutputBuffers.removeFirst();
    }
  }

  /**
   * Releases an output buffer back to the decoder.
   *
   * @param outputBuffer The output buffer being released.
   */
  protected void releaseOutputBuffer(FrameBuffer outputBuffer) {
    synchronized (lock) {
      releaseOutputBufferInternal(outputBuffer);
      maybeNotifyDecodeLoop();
    }
  }

  @Override
  public final void flush() {
    synchronized (lock) {
      flushed = true;
      skippedOutputBufferCount = 0;
      if (dequeuedInputBuffer != null) {
        releaseInputBufferInternal(dequeuedInputBuffer);
        dequeuedInputBuffer = null;
      }
      while (!queuedInputBuffers.isEmpty()) {
        releaseInputBufferInternal(queuedInputBuffers.removeFirst());
      }
      while (!queuedOutputBuffers.isEmpty()) {
        releaseOutputBufferInternal(queuedOutputBuffers.removeFirst());
      }
    }
  }

  @Override
  public void release() {
    synchronized (lock) {
      released = true;
      lock.notify();
    }
    try {
      decodeThread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  /**
   * Throws a decode exception, if there is one.
   *
   * @throws VideoSoftDecoderException The decode exception.
   */
  private void maybeThrowException() throws VideoSoftDecoderException {
    if (exception != null) {
      throw exception;
    }
  }

  /**
   * Notifies the decode loop if there exists a queued input buffer and an available output buffer
   * to decode into.
   * <p>
   * Should only be called whilst synchronized on the lock object.
   */
  private void maybeNotifyDecodeLoop() {
    if (canDecodeBuffer()) {
      lock.notify();
    }
  }

  private void run() {
    try {
      while (decode()) {
        // Do nothing.
      }
    } catch (InterruptedException e) {
      // Not expected.
      throw new IllegalStateException(e);
    }
  }

  @SuppressLint("WrongConstant")
  private boolean decode() throws InterruptedException {
    PacketBuffer inputBuffer = null;
    FrameBuffer outputBuffer = null;
    boolean resetDecoder;

    // Wait until we have an input buffer to decode, and an output buffer to decode into.
    synchronized (lock) {
      while (!released && !canDecodeBuffer()) {
        lock.wait();
      }
      if (released) {
        return false;
      }

      if (queuedInputBuffers.size() > 0) {
        inputBuffer = queuedInputBuffers.removeFirst();
      } else {
        inputBuffer = null;
      }

      if (maybeHasFrame && availableOutputBufferCount > 0) {
        outputBuffer = availableOutputBuffers[--availableOutputBufferCount];
      }

      resetDecoder = flushed;
      flushed = false;
    }

    if (resetDecoder) {
      resetDecoder();
    }

    if (inputBuffer != null) {
      // 发送packet
      exception = sendPacket(inputBuffer);
      boolean needSendAgain = inputBuffer.hasFlag(Constant.BUFFER_FLAG_DECODE_AGAIN);
      if (needSendAgain) {
        inputBuffer.clearFlag(Constant.BUFFER_FLAG_DECODE_AGAIN);
      }

      synchronized (lock) {
        if (needSendAgain) {
          queuedInputBuffers.addFirst(inputBuffer);
        } else {
          // Make the input buffer available again.
          releaseInputBufferInternal(inputBuffer);
          maybeHasFrame = true;
        }
      }

      if (exception != null) {
        return false;
      }
    }

    if (outputBuffer == null) {
      return true;
    }

    exception = getFrame(outputBuffer);
    if (exception != null) {
      synchronized (lock) {
        releaseOutputBufferInternal(outputBuffer);
      }
      return false;
    }

    boolean frameIsReady = !outputBuffer.hasFlag(Constant.BUFFER_FLAG_DECODE_AGAIN);
    boolean frameIsIgnored = outputBuffer.isDecodeOnly();

    synchronized (lock) {
      if (flushed || !frameIsReady) {
        releaseOutputBufferInternal(outputBuffer);
        maybeHasFrame = false;
      } else if (frameIsIgnored) {
        skippedOutputBufferCount++;
        releaseOutputBufferInternal(outputBuffer);
      } else {
        outputBuffer.skippedOutputBufferCount = skippedOutputBufferCount;
        skippedOutputBufferCount = 0;
        queuedOutputBuffers.addLast(outputBuffer);
        // 取出最后一帧后需要重置maybeHasFrame为false
        maybeHasFrame = !outputBuffer.isEndOfStream();
      }
    }

    return true;
  }

  private boolean canDecodeBuffer() {
    return (maybeHasFrame || !queuedInputBuffers.isEmpty()) && availableOutputBufferCount > 0;
  }

  private void releaseInputBufferInternal(PacketBuffer inputBuffer) {
    inputBuffer.clear();
    availableInputBuffers[availableInputBufferCount++] = inputBuffer;
  }

  private void releaseOutputBufferInternal(FrameBuffer outputBuffer) {
    outputBuffer.clear();
    availableOutputBuffers[availableOutputBufferCount++] = outputBuffer;
  }

  /**
   * Creates a new input buffer.
   */
  protected abstract PacketBuffer createInputBuffer();

  /**
   * Creates a new output buffer.
   */
  protected abstract FrameBuffer createOutputBuffer();

  protected abstract void resetDecoder();
  protected abstract VideoSoftDecoderException sendPacket(PacketBuffer inputBuffer);
  protected abstract VideoSoftDecoderException getFrame(FrameBuffer outputBuffer);
}
