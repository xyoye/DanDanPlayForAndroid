package com.xyoye.dandanplay.utils;

import android.support.annotation.NonNull;

import com.blankj.utilcode.util.FileUtils;

import net.sf.sevenzipjbinding.ArchiveFormat;
import net.sf.sevenzipjbinding.ExtractAskMode;
import net.sf.sevenzipjbinding.ExtractOperationResult;
import net.sf.sevenzipjbinding.IArchiveExtractCallback;
import net.sf.sevenzipjbinding.IInArchive;
import net.sf.sevenzipjbinding.ISequentialOutStream;
import net.sf.sevenzipjbinding.PropID;
import net.sf.sevenzipjbinding.SevenZip;
import net.sf.sevenzipjbinding.SevenZipException;
import net.sf.sevenzipjbinding.impl.RandomAccessFileInStream;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by xyoye on 2020/6/10.
 */

public class SevenZipUtils {

    public static void extractFile(@NonNull File rarFile, ExtractCallback callback) throws IOException {
        if (!rarFile.exists() || !rarFile.isFile())
            throw new IOException("compress file not found");
        String destDirName = getFileNameNoExtension(rarFile.getAbsolutePath());
        File destDir = new File(rarFile.getParent(), destDirName);
        if (!destDir.exists()){
            if (destDir.mkdir()){
                extractFile(rarFile, destDir, callback);
            } else {
                throw new IOException("mkdir output directory failed");
            }
        } else {
            extractFile(rarFile, destDir, callback);
        }

    }

    public static void extractFile(@NonNull File compressFile, @NonNull File destDir, ExtractCallback callback) throws IOException {
        if (!compressFile.exists() || !compressFile.isFile())
            throw new IOException("compress file not found");
        if (!destDir.exists() || !destDir.isDirectory())
            throw new IOException("Dest directory not found");


        RandomAccessFile randomAccessFile = new RandomAccessFile(compressFile, "r");
        RandomAccessFileInStream accessFileInStream = new RandomAccessFileInStream(randomAccessFile);

        IInArchive inArchive = SevenZip.openInArchive(null, accessFileInStream);
        inArchive.extract(null, false, new ArchiveExtractCallback(inArchive, destDir, callback));
    }

    private static class ArchiveExtractCallback implements IArchiveExtractCallback {

        private IInArchive inArchive;
        private File destDir;
        private ExtractCallback callback;
        private long totalProgress;
        private boolean isCompleted;

        private ArchiveExtractCallback(IInArchive iInArchive, File destDir, ExtractCallback callback) {
            this.inArchive = iInArchive;
            this.destDir = destDir;
            this.callback = callback;
        }

        @Override
        public ISequentialOutStream getStream(int index, ExtractAskMode extractAskMode) throws SevenZipException {
            final String fileName = getFileName((String) inArchive.getProperty(index, PropID.PATH));
            return new SequentialOutStream(destDir, fileName);
        }

        @Override
        public void prepareOperation(ExtractAskMode extractAskMode) {

        }

        @Override
        public void setOperationResult(ExtractOperationResult extractOperationResult) throws SevenZipException {
            if (extractOperationResult != ExtractOperationResult.OK) {
                throw new SevenZipException(extractOperationResult.toString());
            }
        }

        @Override
        public void setTotal(long total) {
            totalProgress = total;
            if (callback != null) {
                callback.onStart();
            }
        }

        @Override
        public void setCompleted(long complete) {
            float progress = (float) complete / (float) totalProgress * 100;
            if (callback != null && !isCompleted) {
                callback.onProgress((int) progress);
            }
            if (complete == totalProgress) {
                if (callback != null && !isCompleted) {
                    isCompleted = true;
                    callback.onCompleted(destDir.getAbsolutePath());
                }
            }
        }
    }

    private static class SequentialOutStream implements ISequentialOutStream {
        private File destDir;
        private String fileName;

        private SequentialOutStream(File destDir, String fileName) {
            this.destDir = destDir;
            this.fileName = fileName;
        }

        @Override
        public int write(byte[] data) throws SevenZipException {
            if (data == null || data.length == 0) {
                throw new SevenZipException("null data");
            }

            if (!destDir.exists() || !destDir.isDirectory()) {
                throw new SevenZipException("out put directory error");
            }

            if (fileName == null || fileName.length() == 0) {
                fileName = destDir.getName() + "_" + System.currentTimeMillis();
            }

            File outFile = new File(destDir, fileName);

            try (FileOutputStream fileOutputStream = new FileOutputStream(outFile)) {
                fileOutputStream.write(data);
                fileOutputStream.flush();
            } catch (IOException e) {
                throw new SevenZipException("failed to write file: " + fileName);
            }
            return data.length;
        }
    }

    private static String getFileName(String filePath) {
        if (isSpace(filePath)) return "";
        int lastSep = filePath.lastIndexOf(File.separator);
        return lastSep == -1 ? filePath : filePath.substring(lastSep + 1);
    }

    public static String getFileNameNoExtension(final String filePath) {
        if (isSpace(filePath)) return "";
        int lastPoi = filePath.lastIndexOf('.');
        int lastSep = filePath.lastIndexOf(File.separator);
        if (lastSep == -1) {
            return (lastPoi == -1 ? filePath : filePath.substring(0, lastPoi));
        }
        if (lastPoi == -1 || lastSep > lastPoi) {
            return filePath.substring(lastSep + 1);
        }
        return filePath.substring(lastSep + 1, lastPoi);
    }

    private static boolean isSpace(final String s) {
        if (s == null) return true;
        for (int i = 0, len = s.length(); i < len; ++i) {
            if (!Character.isWhitespace(s.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static ArchiveFormat getArchiveFormat(String fileExtension) {
        if (fileExtension == null || fileExtension.length() == 0)
            return null;
        for (ArchiveFormat format : ArchiveFormat.values()) {
            String upperMethodName = format.getMethodName().toUpperCase();
            String upperExtension = fileExtension.toUpperCase();
            if (upperMethodName.equals(upperExtension)) {
                return format;
            }
        }
        return null;
    }

    public interface ExtractCallback {
        void onStart();

        void onProgress(int progress);

        void onCompleted(String destDirectoryPath);
    }
}
