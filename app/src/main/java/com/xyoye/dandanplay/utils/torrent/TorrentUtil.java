package com.xyoye.dandanplay.utils.torrent;

import android.content.Context;
import android.net.Uri;

import libtorrent.Libtorrent;

/**
 * Created by xyy on 2018/10/23.
 */

public class TorrentUtil {

    public static boolean prepareTorrentFromBytes(Torrent torrent, Uri parentFolder, byte[] buf) {
        long id = Libtorrent.addTorrentFromBytes(parentFolder.toString(), buf);
        if (id == -1) return false;
        torrent.setHash(Libtorrent.torrentHash(id));
        torrent.setId(id);
        torrent.setTitle(Libtorrent.torrentName(id));
        torrent.setStatus(Libtorrent.torrentStatus(id));
        return true;
    }

    public static String formatDuration(Context context, long diff) {
        int diffSeconds = (int) (diff / 1000 % 60);
        int diffMinutes = (int) (diff / (60 * 1000) % 60);
        int diffHours = (int) (diff / (60 * 60 * 1000) % 24);
        int diffDays = (int) (diff / (24 * 60 * 60 * 1000));

        String str;

        if (diffDays > 1)
            str = "48h+";
        else if (diffHours > 0)
            str = formatTime(diffHours) + ":" + formatTime(diffMinutes) + ":" + formatTime(diffSeconds);
        else
            str = formatTime(diffMinutes) + ":" + formatTime(diffSeconds);

        return str;
    }

    private static String formatTime(int tt) {
        return String.format("%02d", tt);
    }
}
