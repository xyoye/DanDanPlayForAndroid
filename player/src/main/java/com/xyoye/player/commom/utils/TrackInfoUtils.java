package com.xyoye.player.commom.utils;

import com.xyoye.player.ijkplayer.media.VideoInfoTrack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import tv.danmaku.ijk.media.player.misc.ITrackInfo;
import tv.danmaku.ijk.media.player.misc.IjkTrackInfo;

/**
 * Created by XYJ on 2020/2/10.
 */

public class TrackInfoUtils {
    private List<VideoInfoTrack> audioTrackList;
    private List<VideoInfoTrack> subTrackList;

    public TrackInfoUtils(){
        audioTrackList = new ArrayList<>();
        subTrackList = new ArrayList<>();
    }

    public void initTrackInfo(ITrackInfo[] trackInfo, int selectedAudioTrack, int selectedSubTrack){
        int audioCount = 1;
        int subCount = 1;
        for (int i = 0; i < trackInfo.length; i++) {
            if (trackInfo[i].getTrackType() == IjkTrackInfo.MEDIA_TRACK_TYPE_AUDIO) {
                VideoInfoTrack videoInfoTrack = new VideoInfoTrack();
                String title = trackInfo[i].getTitle();
                String language = trackInfo[i].getLanguage();
                String codecName = trackInfo[i].getCodecName();
                String audioTrackText = String.format(Locale.CHINESE,"#%d：%s[%s, %s]", audioCount, title, language, codecName);

                videoInfoTrack.setStream(i);
                videoInfoTrack.setName(audioTrackText);
                videoInfoTrack.setLanguage(language);
                if (i == selectedAudioTrack)
                    videoInfoTrack.setSelect(true);
                audioCount++;
                audioTrackList.add(videoInfoTrack);
            } else if (trackInfo[i].getTrackType() == IjkTrackInfo.MEDIA_TRACK_TYPE_TIMEDTEXT){
                VideoInfoTrack videoInfoTrack = new VideoInfoTrack();
                String title = trackInfo[i].getTitle();
                String codecName = trackInfo[i].getCodecName();
                String subTrackText = String.format(Locale.CHINESE,"#%d：%s[%s]", subCount, title, codecName);

                videoInfoTrack.setStream(i);
                videoInfoTrack.setName(subTrackText);
                if (i == selectedSubTrack)
                    videoInfoTrack.setSelect(true);
                subCount++;
                subTrackList.add(videoInfoTrack);
            }
        }
    }

    public List<VideoInfoTrack> getAudioTrackList() {
        return audioTrackList;
    }

    public List<VideoInfoTrack> getSubTrackList() {
        return subTrackList;
    }
}
