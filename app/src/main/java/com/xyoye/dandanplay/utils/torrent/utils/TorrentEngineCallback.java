package com.xyoye.dandanplay.utils.torrent.utils;

public interface TorrentEngineCallback
{
    void onTorrentAdded(String id);

    void onTorrentStateChanged(String id);

    void onTorrentFinished(String id);

    void onTorrentRemoved(String id);

    void onTorrentPaused(String id);

    void onTorrentResumed(String id);

    void onEngineStarted();

    void onTorrentMoved(String id, boolean success);

    void onIpFilterParsed(boolean success);

    void onRestoreSessionError(String id);

    void onTorrentError(String id, String errorMsg);

    void onSessionError(String errorMsg);

    void onNatError(String errorMsg);
}