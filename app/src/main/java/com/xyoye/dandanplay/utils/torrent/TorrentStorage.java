package com.xyoye.dandanplay.utils.torrent;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;

import libtorrent.Buffer;
import libtorrent.FileStorageTorrent;

/**
 * Created by xyoye on 2018/10/23.
 */

public class TorrentStorage implements FileStorageTorrent {

    public final static HashMap<String, Torrent> hashs = new HashMap<>();

    public void addHash(String hash, Torrent torrent){
        hashs.put(hash, torrent);
    }

    public void removeHash(String hash){
        hashs.remove(hash);
    }

//    @Override
//    public void createZeroLengthFile(String hash, String path) throws Exception {
//        Torrent torrent = hashs.get(hash);
//        String folder = StringUtils.isEmpty(torrent.getAnimeTitle())
//                ? AppConfig.getInstance().getDownloadFolder()
//                : AppConfig.getInstance().getDownloadFolder() + "/" + torrent.getAnimeTitle();
//        File ff = new File(folder, path);
//        ff.createNewFile();
//    }

    @Override
    public long readFileAt(String hash, String path, Buffer buf, long off) throws Exception {
        Torrent torrent;
        synchronized (hashs) {
            torrent = hashs.get(hash);
        }
        File p = new File(torrent.getParentFolder()
        );
        try {
            File f = new File(p, path);
            RandomAccessFile r = new RandomAccessFile(f, "r");
            r.seek(off);
            int l = (int) buf.length();
            long rest = r.length() - off;
            if (rest < l)
                l = (int) rest;
            byte[] b = new byte[l];
            int a = r.read(b);
            if (a != l)
                throw new RuntimeException("unable to read a!=l " + a + "!=" + l);
            r.close();
            long k = buf.write(b, 0, l);
            if (l != k)
                throw new RuntimeException("unable to write l!=k " + l + "!=" + k);
            return l;
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public void remove(String hash, String path) {
        Torrent torrent;
        synchronized (hashs) {
            torrent = hashs.get(hash);
        }
        try {
            File f = new File(torrent.getParentFolder(), path);
            if (f.exists())
                f.delete();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void rename(String hash, String s1, String s2) {
        Torrent torrent;
        synchronized (hashs) {
            torrent = hashs.get(hash);
        }
        try {
            File f1 = new File(torrent.getParentFolder(), s1);
            File f2 = new File(torrent.getParentFolder(), s2);
            f1.renameTo(f2);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Override
    public long writeFileAt(String hash, String path, byte[] buf, long off) throws Exception {
        Torrent torrent;
        synchronized (hashs) {
            torrent = hashs.get(hash);
        }
        try {
            File f = new File(torrent.getParentFolder(), path);
            File p = f.getParentFile();
            if (!p.exists() && !p.mkdirs())
                throw new IOException("unable to create dir");
            RandomAccessFile r = new RandomAccessFile(f, "rw");
            r.seek(off);
            r.write(buf);
            r.close();
            for (int i = 0; i < buf.length; i++)
                buf[i] = 0;
            return buf.length;
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            throw e;
        }
    }
}
