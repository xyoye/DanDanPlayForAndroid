package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemoteVideoBean {

    /**
     * AnimeId : 0
     * EpisodeId : 0
     * AnimeTitle : null
     * EpisodeTitle : null
     * Id : 40b2ad9a-8faf-46db-a491-ff1c69e2ef76
     * Hash : 30F85BB0C0B5A389A2F728FAD7145B2F
     * Name : 10.mp4
     * Path : C:\Users\admin\Desktop\temp\10.mp4
     * Size : 173268208
     * Rate : 0
     * IsStandalone : false
     * Created : 2019-06-27T16:17:45.3430415+08:00
     * LastMatch : 2019-07-11T15:37:39.7588851+08:00
     * LastPlay : null
     * LastThumbnail : 2019-07-11T14:23:04.3438951+08:00
     * Duration : 649
     */

    private int AnimeId;
    private int EpisodeId;
    private String AnimeTitle;
    private String EpisodeTitle;
    private String Id;
    private String Hash;
    private String Name;
    private String Path;
    private int Size;
    private int Rate;
    private boolean IsStandalone;
    private String Created;
    private String LastMatch;
    private Object LastPlay;
    private String LastThumbnail;
    private int Duration;

    private String originUrl;
    private String danmuPath;

    public int getAnimeId() {
        return AnimeId;
    }

    public void setAnimeId(int AnimeId) {
        this.AnimeId = AnimeId;
    }

    public int getEpisodeId() {
        return EpisodeId;
    }

    public void setEpisodeId(int EpisodeId) {
        this.EpisodeId = EpisodeId;
    }

    public String getAnimeTitle() {
        return AnimeTitle;
    }

    public void setAnimeTitle(String AnimeTitle) {
        this.AnimeTitle = AnimeTitle;
    }

    public String getEpisodeTitle() {
        return EpisodeTitle;
    }

    public void setEpisodeTitle(String EpisodeTitle) {
        this.EpisodeTitle = EpisodeTitle;
    }

    public String getId() {
        return Id;
    }

    public void setId(String Id) {
        this.Id = Id;
    }

    public String getHash() {
        return Hash;
    }

    public void setHash(String Hash) {
        this.Hash = Hash;
    }

    public String getName() {
        return Name;
    }

    public void setName(String Name) {
        this.Name = Name;
    }

    public String getPath() {
        return Path;
    }

    public void setPath(String Path) {
        this.Path = Path;
    }

    public int getSize() {
        return Size;
    }

    public void setSize(int Size) {
        this.Size = Size;
    }

    public int getRate() {
        return Rate;
    }

    public void setRate(int Rate) {
        this.Rate = Rate;
    }

    public boolean isIsStandalone() {
        return IsStandalone;
    }

    public void setIsStandalone(boolean IsStandalone) {
        this.IsStandalone = IsStandalone;
    }

    public String getCreated() {
        return Created;
    }

    public void setCreated(String Created) {
        this.Created = Created;
    }

    public String getLastMatch() {
        return LastMatch;
    }

    public void setLastMatch(String LastMatch) {
        this.LastMatch = LastMatch;
    }

    public Object getLastPlay() {
        return LastPlay;
    }

    public void setLastPlay(Object LastPlay) {
        this.LastPlay = LastPlay;
    }

    public String getLastThumbnail() {
        return LastThumbnail;
    }

    public void setLastThumbnail(String LastThumbnail) {
        this.LastThumbnail = LastThumbnail;
    }

    public int getDuration() {
        return Duration;
    }

    public void setDuration(int Duration) {
        this.Duration = Duration;
    }

    public String getOriginUrl() {
        return originUrl;
    }

    public void setOriginUrl(String originUrl) {
        this.originUrl = originUrl;
    }

    public String getDanmuPath() {
        return danmuPath;
    }

    public void setDanmuPath(String danmuPath) {
        this.danmuPath = danmuPath;
    }
}
