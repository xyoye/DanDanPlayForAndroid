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

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.decoder.CryptoInfo;
import com.google.android.exoplayer2.drm.DecryptionException;
import com.google.android.exoplayer2.drm.ExoMediaCrypto;
import com.google.android.exoplayer2.ext.ffmpeg.DecoderSoLibrary;
import com.google.android.exoplayer2.ext.ffmpeg.exception.VideoSoftDecoderException;
import com.google.android.exoplayer2.util.MimeTypes;

import java.io.File;
import java.io.FileFilter;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.regex.Pattern;

/**
 * ffmpeg decoder.
 */
@SuppressLint("WrongConstant")
final class VideoDecoder extends BaseDecoder {
    private static final int NO_ERROR = 0;
    private static final int DECODE_ERROR = 1;
    private static final int DRM_ERROR = 2;
    private static final int DECODE_AGAIN = 3;
    private static final int DECODE_EOF = 4;
    private static final int OUTPUT_BUFFER_ALLOCATE_FAILED = 5;

    private final ExoMediaCrypto exoMediaCrypto;
    private final long ffmpegDecContext;

    /**
     * Creates a ffmpeg decoder.
     *
     * @param numInputBuffers        The number of input buffers.
     * @param numOutputBuffers       The number of output buffers.
     * @param initialInputBufferSize The initial size of each input buffer.
     * @param exoMediaCrypto         The {@link ExoMediaCrypto} object required for decoding encrypted
     *                               content. Maybe null and can be ignored if decoder does not handle encrypted content.
     * @throws VideoSoftDecoderException Thrown if an exception occurs when initializing the decoder.
     */
    public VideoDecoder(Format format, int numInputBuffers, int numOutputBuffers, int initialInputBufferSize,
                        ExoMediaCrypto exoMediaCrypto) throws VideoSoftDecoderException {
        super(new PacketBuffer[numInputBuffers], new FrameBuffer[numOutputBuffers]);
        if (!DecoderSoLibrary.isAvailable()) {
            throw new VideoSoftDecoderException("Failed to load decoder native libraries.");
        }
        this.exoMediaCrypto = exoMediaCrypto;
        if (exoMediaCrypto != null && !DecoderSoLibrary.ffmpegIsSecureDecodeSupported()) {
            throw new VideoSoftDecoderException("FFmpeg decoder does not support secure decode.");
        }
        String mimeType = format.sampleMimeType;
        String codecName;
        if (mimeType.equals(MimeTypes.VIDEO_H264)) {
            codecName = "h264";
        } else if (mimeType.equals(MimeTypes.VIDEO_H265)) {
            codecName = "hevc";
        } else {
            throw new VideoSoftDecoderException("Unsupported mimetype:" + mimeType);
        }

        int width = format.width;
        int height = format.height;

        ffmpegDecContext = ffmpegInit(codecName,
                width,
                height,
                getExtraData(mimeType, format.initializationData),
                getCpuNumCores() + 1);
        if (ffmpegDecContext == 0) {
            throw new VideoSoftDecoderException("Failed to initialize decoder");
        }
        setInitialInputBufferSize(initialInputBufferSize);
    }

    @Override
    public String getName() {
        return "libffmpeg" + DecoderSoLibrary.getVersion();
    }

    @Override
    protected PacketBuffer createInputBuffer() {
        return new PacketBuffer();
    }

    @Override
    protected FrameBuffer createOutputBuffer() {
        return new FrameBuffer(this);
    }

    @Override
    protected void resetDecoder() {
        ffmpegFlushBuffers(ffmpegDecContext);
    }

    @Override
    protected VideoSoftDecoderException sendPacket(PacketBuffer inputBuffer) {

        boolean isEndOfStream = inputBuffer.isEndOfStream();
        boolean isDecodeOnly = inputBuffer.isDecodeOnly();

        ByteBuffer inputData = inputBuffer.data;
        int inputSize = 0;
        if (!inputBuffer.isEndOfStream()) {
            inputSize = inputData.limit();
        }
        CryptoInfo cryptoInfo = inputBuffer.cryptoInfo;
        final long result = inputBuffer.isEncrypted()
                ? ffmpegSecureDecode(ffmpegDecContext,
                inputData,
                inputSize,
                exoMediaCrypto,
                cryptoInfo.mode,
                cryptoInfo.key,
                cryptoInfo.iv,
                cryptoInfo.numSubSamples,
                cryptoInfo.numBytesOfClearData,
                cryptoInfo.numBytesOfEncryptedData,
                inputBuffer.timeUs,
                isDecodeOnly,
                isEndOfStream)
                : ffmpegDecode(ffmpegDecContext,
                inputData,
                inputSize,
                inputBuffer.timeUs,
                isDecodeOnly,
                isEndOfStream);
        if (result != NO_ERROR) {
            if (result == DRM_ERROR) {
                String message = "Drm error!!";
                DecryptionException cause = new DecryptionException(
                        ffmpegGetErrorCode(ffmpegDecContext), message);
                return new VideoSoftDecoderException(message, cause);
            } else if (result == DECODE_AGAIN) {
                inputBuffer.addFlag(Constant.BUFFER_FLAG_DECODE_AGAIN);
            } else {
                return new VideoSoftDecoderException("failed to decode, error code: " + ffmpegGetErrorCode(ffmpegDecContext));
            }
        }
        return null;
    }

    @Override
    protected VideoSoftDecoderException getFrame(FrameBuffer outputBuffer) {
        int getFrameResult = ffmpegGetFrame(ffmpegDecContext, outputBuffer);
        if (getFrameResult == DECODE_AGAIN) {
            outputBuffer.addFlag(Constant.BUFFER_FLAG_DECODE_AGAIN);
        } else if (getFrameResult == OUTPUT_BUFFER_ALLOCATE_FAILED) {
            return new VideoSoftDecoderException("failed to initialize buffer");
        } else if (getFrameResult == DECODE_EOF) {
            outputBuffer.addFlag(C.BUFFER_FLAG_END_OF_STREAM);
        } else if (getFrameResult != NO_ERROR) {
            return new VideoSoftDecoderException("failed to get next frame, error code:" + ffmpegGetErrorCode(ffmpegDecContext));
        }
        return null;
    }

    @Override
    protected void releaseOutputBuffer(FrameBuffer buffer) {
        super.releaseOutputBuffer(buffer);
    }

    @Override
    public void release() {
        super.release();
        ffmpegClose(ffmpegDecContext);
    }

    private static int getCpuNumCores() {
        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    return Pattern.matches("cpu[0-9]+", pathname.getName());
                }
            });
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch(Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    private static byte[] getExtraData(String mimeType, List<byte[]> initializationData) {
        byte[] extraData = null;
        if (initializationData.size() > 1) {
            int extraDataLength = 0;
            for (byte[] data : initializationData) {
                // 加2个分割
                if (extraDataLength != 0) {
                    extraDataLength += 2;
                }
                extraDataLength += data.length + 2;
            }

            if (extraDataLength > 0) {
                extraData = new byte[extraDataLength];
                int currentPos = 0;
                for (byte[] data : initializationData) {
                    if (currentPos != 0) {
                        extraData[currentPos] = 0;
                        extraData[currentPos + 1] = 0;
                        currentPos += 2;
                    }
                    extraData[currentPos] = (byte) (data.length >> 8);
                    extraData[currentPos + 1] = (byte) (data.length & 0xFF);
                    System.arraycopy(data, 0, extraData, currentPos + 2, data.length);
                    currentPos += data.length + 2;
                }
            }
        } else if (initializationData.size() == 1){
            byte[] data = initializationData.get(0);
            if (data != null && data.length > 0) {
                extraData = new byte[data.length];
                System.arraycopy(data, 0, extraData, 0, data.length);
            }
        }

        return extraData;
    }

    private native long ffmpegInit(String codecName, int width, int height,
                                   byte[] extraData, int threadCount);

    private native int ffmpegClose(long context);

    private native void ffmpegFlushBuffers(long context);

    private native int ffmpegDecode(long context,
                                     ByteBuffer encoded,
                                     int length,
                                     long timeUs,
                                     boolean isDecodeOnly,
                                     boolean isEndOfStream);

    private native int ffmpegSecureDecode(long context,
                                           ByteBuffer encoded,
                                           int length,
                                           ExoMediaCrypto mediaCrypto,
                                           int inputMode,
                                           byte[] key,
                                           byte[] iv,
                                           int numSubSamples,
                                           int[] numBytesOfClearData,
                                           int[] numBytesOfEncryptedData,
                                           long timeUs,
                                           boolean isDecodeOnly,
                                           boolean isEndOfStream);

    private native int ffmpegGetFrame(long context, FrameBuffer outputBuffer);

    private native int ffmpegGetErrorCode(long context);
}
