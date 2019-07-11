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
package com.google.android.exoplayer2.ext.ffmpeg.audio;


import android.support.annotation.Nullable;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.DecoderInputBuffer;
import com.google.android.exoplayer2.decoder.SimpleDecoder;
import com.google.android.exoplayer2.decoder.SimpleOutputBuffer;
import com.google.android.exoplayer2.ext.ffmpeg.exception.AudioSoftDecoderException;
import com.google.android.exoplayer2.ext.ffmpeg.DecoderSoLibrary;
import com.google.android.exoplayer2.util.Assertions;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.MimeTypes;
import com.google.android.exoplayer2.util.ParsableByteArray;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * FFmpeg audio decoder.
 */
/* package */
final class AudioDecoder extends
        SimpleDecoder<DecoderInputBuffer, SimpleOutputBuffer, AudioSoftDecoderException> {
  private static final String TAG = "audiodecoder";

  // Output buffer sizes when decoding PCM mu-law streams, which is the maximum FFmpeg outputs.
  private static final int OUTPUT_BUFFER_SIZE_16BIT = 65536;
  private static final int OUTPUT_BUFFER_SIZE_32BIT = OUTPUT_BUFFER_SIZE_16BIT * 2;

  // Error codes matching ffmpeg_jni.cc.
  private static final int DECODER_ERROR_INVALID_DATA = -1;
  private static final int DECODER_ERROR_OTHER = -2;

  private final String codecName;
  private final @Nullable
  byte[] extraData;
  private final @C.Encoding int encoding;
  private final int outputBufferSize;

  private long nativeContext; // May be reassigned on resetting the codec.
  private boolean hasOutputFormat;
  private volatile int channelCount;
  private volatile int sampleRate;

  public AudioDecoder(
      int numInputBuffers,
      int numOutputBuffers,
      int initialInputBufferSize,
      Format format,
      boolean outputFloat)
      throws AudioSoftDecoderException {
    super(new DecoderInputBuffer[numInputBuffers], new SimpleOutputBuffer[numOutputBuffers]);
    if (!DecoderSoLibrary.isAvailable()) {
      throw new AudioSoftDecoderException("Failed to load decoder native libraries.");
    }
    Assertions.checkNotNull(format.sampleMimeType);
    codecName = Assertions.checkNotNull(getCodecName(format.sampleMimeType, format.pcmEncoding));
    extraData = getExtraData(format.sampleMimeType, format.initializationData);
    encoding = outputFloat ? C.ENCODING_PCM_FLOAT : C.ENCODING_PCM_16BIT;
    outputBufferSize = outputFloat ? OUTPUT_BUFFER_SIZE_32BIT : OUTPUT_BUFFER_SIZE_16BIT;
    nativeContext =
        ffmpegInitialize(codecName, extraData, outputFloat, format.sampleRate, format.channelCount);
    if (nativeContext == 0) {
      throw new AudioSoftDecoderException("Initialization failed.");
    }
    setInitialInputBufferSize(initialInputBufferSize);
  }

  @Override
  public String getName() {
    return "ffmpeg" + DecoderSoLibrary.getVersion() + "-" + codecName;
  }

  @Override
  protected DecoderInputBuffer createInputBuffer() {
    return new DecoderInputBuffer(DecoderInputBuffer.BUFFER_REPLACEMENT_MODE_DIRECT);
  }

  @Override
  protected SimpleOutputBuffer createOutputBuffer() {
    return new SimpleOutputBuffer(this);
  }

  @Override
  protected AudioSoftDecoderException createUnexpectedDecodeException(Throwable error) {
    return new AudioSoftDecoderException("Unexpected decode error", error);
  }

  @Override
  protected @Nullable
  AudioSoftDecoderException decode(
          DecoderInputBuffer inputBuffer, SimpleOutputBuffer outputBuffer, boolean reset) {
    if (reset) {
      nativeContext = ffmpegReset(nativeContext, extraData);
      if (nativeContext == 0) {
        return new AudioSoftDecoderException("Error resetting (see logcat).");
      }
    }
    ByteBuffer inputData = inputBuffer.data;
    int inputSize = inputData.limit();
    ByteBuffer outputData = outputBuffer.init(inputBuffer.timeUs, outputBufferSize);
    int result = ffmpegDecode(nativeContext, inputData, inputSize, outputData, outputBufferSize);
    if (result == DECODER_ERROR_INVALID_DATA) {
      // Treat invalid data errors as non-fatal to match the behavior of MediaCodec. No output will
      // be produced for this buffer, so mark it as decode-only to ensure that the audio sink's
      // position is reset when more audio is produced.
      outputBuffer.setFlags(C.BUFFER_FLAG_DECODE_ONLY);
      return null;
    } else if (result == DECODER_ERROR_OTHER) {
      return new AudioSoftDecoderException("Error decoding (see logcat).");
    }
    if (!hasOutputFormat) {
      channelCount = ffmpegGetChannelCount(nativeContext);
      sampleRate = ffmpegGetSampleRate(nativeContext);
      if (sampleRate == 0 && "alac".equals(codecName)) {
        Assertions.checkNotNull(extraData);
        // ALAC decoder did not set the sample rate in earlier versions of FFMPEG.
        // See https://trac.ffmpeg.org/ticket/6096
        ParsableByteArray parsableExtraData = new ParsableByteArray(extraData);
        parsableExtraData.setPosition(extraData.length - 4);
        sampleRate = parsableExtraData.readUnsignedIntToInt();
      }
      hasOutputFormat = true;
    }
    outputBuffer.data.position(0);
    outputBuffer.data.limit(result);
    return null;
  }

  @Override
  public void release() {
    super.release();
    ffmpegRelease(nativeContext);
    nativeContext = 0;
  }

  /**
   * Returns the channel count of output audio. May only be called after {@link #decode}.
   */
  public int getChannelCount() {
    return channelCount;
  }

  /**
   * Returns the sample rate of output audio. May only be called after {@link #decode}.
   */
  public int getSampleRate() {
    return sampleRate;
  }

  /**
   * Returns the encoding of output audio.
   */
  public @C.Encoding int getEncoding() {
    return encoding;
  }

  /**
   * Returns FFmpeg-compatible codec-specific initialization data ("extra data"), or {@code null} if
   * not required.
   */
  private static @Nullable
  byte[] getExtraData(String mimeType, List<byte[]> initializationData) {
    switch (mimeType) {
      case MimeTypes.AUDIO_AAC:
      case MimeTypes.AUDIO_ALAC:
      case MimeTypes.AUDIO_OPUS:
        return initializationData.get(0);
      case MimeTypes.AUDIO_VORBIS:
        byte[] header0 = initializationData.get(0);
        byte[] header1 = initializationData.get(1);
        byte[] extraData = new byte[header0.length + header1.length + 6];
        extraData[0] = (byte) (header0.length >> 8);
        extraData[1] = (byte) (header0.length & 0xFF);
        System.arraycopy(header0, 0, extraData, 2, header0.length);
        extraData[header0.length + 2] = 0;
        extraData[header0.length + 3] = 0;
        extraData[header0.length + 4] =  (byte) (header1.length >> 8);
        extraData[header0.length + 5] = (byte) (header1.length & 0xFF);
        System.arraycopy(header1, 0, extraData, header0.length + 6, header1.length);
        return extraData;
      default:
        // Other codecs do not require extra data.
        return null;
    }
  }

  private native long ffmpegInitialize(
      String codecName,
      @Nullable byte[] extraData,
      boolean outputFloat,
      int rawSampleRate,
      int rawChannelCount);

  private native int ffmpegDecode(long context, ByteBuffer inputData, int inputSize,
                                  ByteBuffer outputData, int outputSize);
  private native int ffmpegGetChannelCount(long context);
  private native int ffmpegGetSampleRate(long context);

  private native long ffmpegReset(long context, @Nullable byte[] extraData);

  private native void ffmpegRelease(long context);

  public static boolean supportsFormat(String mimeType, @C.PcmEncoding int encoding) {
    String codecName = getCodecName(mimeType, encoding);
    if (codecName == null) {
      return false;
    }
    if (!ffmpegHasDecoder(codecName)) {
      Log.w(TAG, "No " + codecName + " decoder available. Check the FFmpeg build configuration.");
      return false;
    }
    return true;
  }

  /**
   * Returns the name of the FFmpeg decoder that could be used to decode the format, or {@code null}
   * if it's unsupported.
   */
 private static @Nullable
 String getCodecName(String mimeType, @C.PcmEncoding int encoding) {
    switch (mimeType) {
      case MimeTypes.AUDIO_AAC:
        return "aac";
      case MimeTypes.AUDIO_MPEG:
      case MimeTypes.AUDIO_MPEG_L1:
      case MimeTypes.AUDIO_MPEG_L2:
        return "mp3";
      case MimeTypes.AUDIO_AC3:
        return "ac3";
      case MimeTypes.AUDIO_E_AC3:
      case MimeTypes.AUDIO_E_AC3_JOC:
        return "eac3";
      case MimeTypes.AUDIO_TRUEHD:
        return "truehd";
      case MimeTypes.AUDIO_DTS:
      case MimeTypes.AUDIO_DTS_HD:
        return "dca";
      case MimeTypes.AUDIO_VORBIS:
        return "vorbis";
      case MimeTypes.AUDIO_OPUS:
        return "opus";
      case MimeTypes.AUDIO_AMR_NB:
        return "amrnb";
      case MimeTypes.AUDIO_AMR_WB:
        return "amrwb";
      case MimeTypes.AUDIO_FLAC:
        return "flac";
      case MimeTypes.AUDIO_ALAC:
        return "alac";
      case MimeTypes.AUDIO_RAW:
        if (encoding == C.ENCODING_PCM_MU_LAW) {
          return "pcm_mulaw";
        } else if (encoding == C.ENCODING_PCM_A_LAW) {
          return "pcm_alaw";
        } else {
          return null;
        }
      default:
        return null;
    }
  }

  private static native boolean ffmpegHasDecoder(String codecName);
}
