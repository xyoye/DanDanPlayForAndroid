package com.xyoye.dandanplay.utils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by xyy on 2018/11/23.
 */

public class HashBufferedInputStream extends BufferedInputStream {
    private final MessageDigest messageDigest;

    private static final Set<String> algorithms = new HashSet<>();

    static {
        algorithms.add("MD5");
        algorithms.add("SHA-1");
        algorithms.add("SHA-256");
        algorithms.add("SHA-512");
    }

    /**
     * @param anIn
     * @throws NoSuchAlgorithmException
     */
    public HashBufferedInputStream(final InputStream anIn, final String algorithm) throws NoSuchAlgorithmException {
        super(anIn);
        if (!algorithms.contains(algorithm)) {
            throw new RuntimeException("algorithm not support");
        }
        messageDigest = MessageDigest.getInstance(algorithm);
    }

    /**
     * @param anIn
     * @param anSize
     * @throws NoSuchAlgorithmException
     */
    public HashBufferedInputStream(final InputStream anIn, final int anSize, final String algorithm) throws NoSuchAlgorithmException {
        super(anIn, anSize);
        if (!algorithms.contains(algorithm)) {
            throw new RuntimeException("algorithm not support");
        }
        messageDigest = MessageDigest.getInstance(algorithm);
    }

    @Override
    public int read(final byte[] anB) throws IOException {
        final int result = super.read(anB);
        if (result != -1) {
            messageDigest.update(anB, 0, result);
        }
        return result;
    }

    public String getHash() {
        final byte byteBuffer[] = messageDigest.digest();
        final StringBuffer hexStr = new StringBuffer();
        for (final byte element : byteBuffer) {
            final String hex = Integer.toHexString(0xff & element);
            if (hex.length() == 1) {
                hexStr.append('0');
            }
            hexStr.append(hex);
        }
        return hexStr.toString();
    }
}
