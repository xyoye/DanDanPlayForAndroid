package com.xyoye.dandanplay.utils.permissionchecker.util;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by joker on 2017/8/17.
 */

public class AudioRecordManager {
    public File file;
    private AudioRecord mRecorder;
    private DataOutputStream dos;
    private Thread recordThread;
    private boolean isStart = false;
    private int bufferSize;
    /**
     * record thread
     */
    Runnable recordRunnable = new Runnable() {
        @Override
        public void run() {
            try {
                android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO);
                int bytesRecord;
                byte[] tempBuffer = new byte[bufferSize];
                mRecorder.startRecording();
                while (isStart) {
                    if (mRecorder != null) {
                        bytesRecord = mRecorder.read(tempBuffer, 0, bufferSize);
                        if (bytesRecord == AudioRecord.ERROR_INVALID_OPERATION || bytesRecord ==
                                AudioRecord.ERROR_BAD_VALUE) {
                            continue;
                        }
                        if (bytesRecord != 0 && bytesRecord != -1) {
                            dos.write(tempBuffer, 0, bytesRecord);
                        } else {
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    };
    private long length;

    public AudioRecordManager() {
        bufferSize = AudioRecord.getMinBufferSize(8000, AudioFormat.CHANNEL_IN_MONO, AudioFormat
                .ENCODING_PCM_16BIT);
        mRecorder = new AudioRecord(MediaRecorder.AudioSource.MIC, 8000, AudioFormat.CHANNEL_IN_MONO,
                AudioFormat.ENCODING_PCM_16BIT, bufferSize * 2);
    }

    public boolean getSuccess() {
        return length > 0;
    }

    /**
     * destroy record thread
     */
    private void destroyThread() {
        try {
            isStart = false;
            if (recordThread != null && recordThread.getState() != Thread.State.TERMINATED) {
                try {
                    recordThread.interrupt();
                } catch (Exception e) {
                    e.printStackTrace();
                    recordThread = null;
                }
            }
            recordThread = null;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            recordThread = null;
        }
    }

    /**
     * start record thread
     */
    private void startThread() {
        isStart = true;
        if (recordThread == null) {
            recordThread = new Thread(recordRunnable);
            recordThread.start();
        }
    }

    /**
     * save file
     *
     * @throws IOException
     */
    private void setPath(String path) throws IOException {
        file = new File(path);
        deleteFile();
        file.createNewFile();
        dos = new DataOutputStream(new FileOutputStream(file, true));
    }

    /**
     * start record
     *
     * @param path
     * @throws IOException
     */
    public void startRecord(String path) throws IOException {
        setPath(path);
        startThread();
    }

    /**
     * stop record
     *
     * @throws IOException
     * @throws InterruptedException
     */
    public void stopRecord() throws IOException, InterruptedException {
        // specially for OPPO、XIAOMI、MEIZU、HUAWEI and so on
        Thread.sleep(250);
        destroyThread();
        if (mRecorder != null) {
            if (mRecorder.getState() == AudioRecord.STATE_INITIALIZED) {
                mRecorder.stop();
            }
            if (mRecorder != null) {
                mRecorder.release();
            }
        }
        if (dos != null) {
            dos.flush();
            dos.close();
        }
        length = file.length();
        deleteFile();
    }

    private void deleteFile() {
        if (file.exists()) {
            file.delete();
        }
    }
}
