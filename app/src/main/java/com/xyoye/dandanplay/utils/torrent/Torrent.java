package com.xyoye.dandanplay.utils.torrent;

import com.github.axet.wget.SpeedInfo;

import java.io.Serializable;
import java.util.List;

import libtorrent.Libtorrent;
import libtorrent.StatsTorrent;

/**
 * Created by xyy on 2018/10/23.
 */
public class Torrent implements Serializable{

    private long id;
    private String title;
    private String path;
    private String danmuPath;
    private int episodeId;
    private String animeTitle;
    private String hash;
    private long size;
    private boolean done;
    private boolean error;
    private int status = -1;
    private boolean isLongCheck;
    private boolean update;
    private String magnet;
    private List<TorrentFile> torrentFileList;
    public SpeedInfo downloaded = new SpeedInfo();
    public SpeedInfo uploaded = new SpeedInfo();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }

    public String getAnimeTitle() {
        return animeTitle;
    }

    public void setAnimeTitle(String animeTitle) {
        this.animeTitle = animeTitle;
    }

    public int getEpisodeId() {
        return episodeId;
    }

    public void setEpisodeId(int episodeId) {
        this.episodeId = episodeId;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isDone() {
        return done;
    }

    public void setDone(boolean done) {
        this.done = done;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public boolean isLongCheck() {
        return isLongCheck;
    }

    public void setLongCheck(boolean longCheck) {
        isLongCheck = longCheck;
    }

    public boolean isUpdate() {
        return update;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }

    public String getMagnet() {
        return magnet;
    }

    public void setMagnet(String magnet) {
        this.magnet = magnet;
    }

    public List<TorrentFile> getTorrentFileList() {
        return torrentFileList;
    }

    public void setTorrentFileList(List<TorrentFile> torrentFileList) {
        this.torrentFileList = torrentFileList;
    }

    public static class TorrentFile implements Serializable{
        private long id;
        private long torrentId;
        private boolean isCheck;
        private long length;
        private long completed;
        private String originPath;
        private String path;
        private String name;
        private String danmuPath;
        private int episodeId;

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }

        public long getTorrentId() {
            return torrentId;
        }

        public void setTorrentId(long torrentId) {
            this.torrentId = torrentId;
        }

        public boolean isCheck() {
            return isCheck;
        }

        public void setCheck(boolean check) {
            isCheck = check;
        }

        public String getOriginPath() {
            return originPath;
        }

        public void setOriginPath(String originPath) {
            this.originPath = originPath;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }

        public long getCompleted() {
            return completed;
        }

        public void setCompleted(long completed) {
            this.completed = completed;
        }

        public String getDanmuPath() {
            return danmuPath;
        }

        public void setDanmuPath(String danmuPath) {
            this.danmuPath = danmuPath;
        }

        public int getEpisodeId() {
            return episodeId;
        }

        public void setEpisodeId(int episodeId) {
            this.episodeId = episodeId;
        }

        public boolean isDone() {
            return completed == length;
        }
    }

    public void update(){
        StatsTorrent stats = Libtorrent.torrentStats(id);
        downloaded.step(stats.getDownloaded());
        uploaded.step(stats.getUploaded());
        if (Libtorrent.metaTorrent(id)) {
            long l = Libtorrent.torrentPendingBytesLength(id);
            long c = Libtorrent.torrentPendingBytesCompleted(id);
            if (l > 0 && l == c && !isDone()){
                setDone(true);
                TorrentUtil.updateTorrent(this);
            }
        } else {
            done = false;
        }
    }

    public boolean completed() {
        if (Libtorrent.metaTorrent(id)) {
            long l = Libtorrent.torrentPendingBytesLength(id);
            long c = Libtorrent.torrentPendingBytesCompleted(id);
            return l > 0 && l == c;
        } else {
            return false;
        }
    }

}
