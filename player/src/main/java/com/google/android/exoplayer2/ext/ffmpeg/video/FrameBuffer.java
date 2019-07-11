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

import com.google.android.exoplayer2.decoder.OutputBuffer;

import java.nio.ByteBuffer;

/**
 * Output buffer containing video frame data, populated by {@link VideoDecoder}.
 */
/* package */
final class FrameBuffer extends OutputBuffer {
  private final VideoDecoder owner;

  public ByteBuffer data;
  public int width;
  public int height;

  /**
   * YUV planes for YUV mode.
   */
  public ByteBuffer[] yuvPlanes;
  public int[] yuvStrides;
  public int bitDepth;

  public FrameBuffer(VideoDecoder owner) {
    this.owner = owner;
  }

  @Override
  public void release() {
    owner.releaseOutputBuffer(this);
  }

  /**
   * Resizes the buffer based on the given stride. Called via JNI after decoding completes.
   * @return Whether the buffer was resized successfully.
   */
  public boolean initForYuvFrame(int width, int height, int yStride, int uvStride,
      int bitDepth) {
    this.width = width;
    this.height = height;
    this.bitDepth = bitDepth;

    int uvHeight = (int) (((long) height + 1) / 2);
    if (!isSafeToMultiply(yStride, height) || !isSafeToMultiply(uvStride, uvHeight)) {
      return false;
    }

    int yLength = yStride * height;
    int uvLength = uvStride * uvHeight;
    int minimumYuvSize = yLength + (uvLength * 2);
    if (!isSafeToMultiply(uvLength, 2) || minimumYuvSize < yLength) {
      return false;
    }
    initData(minimumYuvSize);

    if (yuvPlanes == null) {
      yuvPlanes = new ByteBuffer[3];
    }
    yuvPlanes[0] = data.slice();
    yuvPlanes[0].limit(yLength);
    data.position(yLength);
    yuvPlanes[1] = data.slice();
    yuvPlanes[1].limit(uvLength);
    data.position(yLength + uvLength);
    yuvPlanes[2] = data.slice();
    yuvPlanes[2].limit(uvLength);
    if (yuvStrides == null) {
      yuvStrides = new int[3];
    }
    yuvStrides[0] = yStride;
    yuvStrides[1] = uvStride;
    yuvStrides[2] = uvStride;
    return true;
  }

  private void initData(int size) {
    if (data == null || data.capacity() < size) {
      data = ByteBuffer.allocateDirect(size);
    } else {
      data.position(0);
      data.limit(size);
    }
  }

  /**
   * Ensures that the result of multiplying individual numbers can fit into the size limit of an
   * integer.
   */
  private boolean isSafeToMultiply(int a, int b) {
    return a >= 0 && b >= 0 && !(b > 0 && a >= Integer.MAX_VALUE / b);
  }

  public boolean hasFlag(int flag) {
    return getFlag(flag);
  }
}
