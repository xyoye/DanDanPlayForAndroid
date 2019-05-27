package com.xyoye.dandanplay.utils.torrent;

import android.annotation.SuppressLint;

import com.github.axet.wget.SpeedInfo;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by xyoye on 2019/3/21.
 */

public class TorrentSpeed {
    private static Map<Long, SpeedInfo> downloadMap;
    private static Map<Long, SpeedInfo> uploadMap;

    private static TorrentSpeed instance;

    @SuppressLint("UseSparseArrays")
    private TorrentSpeed(){
        downloadMap = new HashMap<>();
        uploadMap = new HashMap<>();
    }

    public static TorrentSpeed getInstance() {
        if (instance == null){
            instance = new TorrentSpeed();
        }
        return instance;
    }

    public SpeedInfo getDownloadSpeed(long id){
        if (downloadMap.containsKey(id)){
            return downloadMap.get(id);
        }else{
            SpeedInfo speedInfo = new SpeedInfo();
            downloadMap.put(id, speedInfo);
            return speedInfo;
        }
    }

    public SpeedInfo getUploadSpeed(long id){
        if (uploadMap.containsKey(id)){
            return uploadMap.get(id);
        }else{
            SpeedInfo speedInfo = new SpeedInfo();
            uploadMap.put(id, speedInfo);
            return speedInfo;
        }
    }

    public void addDownloadSpeed(long id, SpeedInfo speedInfo){
        if (!downloadMap.containsKey(id))
            downloadMap.put(id, speedInfo);
    }

    public void addUploadSpeed(long id, SpeedInfo speedInfo){
        if (!uploadMap.containsKey(id))
            uploadMap.put(id, speedInfo);
    }
}
