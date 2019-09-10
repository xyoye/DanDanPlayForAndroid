package com.xyoye.dandanplay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;

/**
 * Created by xyoye on 2018/10/13.
 */

public class MagnetBean implements Parcelable{

    /**
     * HasMore : true
     * Resources : [{"Title":"【傲娇零&自由字幕组】[刀剑神域III UnderWorld/Sword Art Online - Alicization][01][HEVC-10Bit-1080P AAC][外挂GB/BIG5][WEB-Rip][MP4+ass]","TypeId":2,"TypeName":"动画/新番连载","SubgroupId":532,"SubgroupName":"傲娇零字幕组","Magnet":"magnet:?xt=urn:btih:WEORDPJIJANN54BH2GNNJ6CSN7KB7S34","PageUrl":"https://share.dmhy.org/topics/view/501340_III_UnderWorld_Sword_Art_Online_-_Alicization_01_HEVC-10Bit-2160P_AAC_GB_BIG5_WEB-Rip_MP4_ass.html","FileSize":"818.7MB","PublishDate":"2018-10-12 12:44:00"}]
     */

    private boolean HasMore;
    private List<ResourcesBean> Resources;

    protected MagnetBean(Parcel in) {
        HasMore = in.readByte() != 0;
        Resources = in.createTypedArrayList(ResourcesBean.CREATOR);
    }

    public boolean isHasMore() {
        return HasMore;
    }

    public void setHasMore(boolean HasMore) {
        this.HasMore = HasMore;
    }

    public List<ResourcesBean> getResources() {
        return Resources;
    }

    public void setResources(List<ResourcesBean> Resources) {
        this.Resources = Resources;
    }

    public static final Creator<MagnetBean> CREATOR = new Creator<MagnetBean>() {
        @Override
        public MagnetBean createFromParcel(Parcel in) {
            return new MagnetBean(in);
        }

        @Override
        public MagnetBean[] newArray(int size) {
            return new MagnetBean[size];
        }
    };
    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte((byte) (HasMore ? 1 : 0));
        dest.writeTypedList(Resources);
    }

    public static class ResourcesBean implements Parcelable {
        /**
         * Title : 【傲娇零&自由字幕组】[刀剑神域III UnderWorld/Sword Art Online - Alicization][01][HEVC-10Bit-1080P AAC][外挂GB/BIG5][WEB-Rip][MP4+ass]
         * TypeId : 2
         * TypeName : 动画/新番连载
         * SubgroupId : 532
         * SubgroupName : 傲娇零字幕组
         * Magnet : magnet:?xt=urn:btih:WEORDPJIJANN54BH2GNNJ6CSN7KB7S34
         * PageUrl : https://share.dmhy.org/topics/view/501340_III_UnderWorld_Sword_Art_Online_-_Alicization_01_HEVC-10Bit-2160P_AAC_GB_BIG5_WEB-Rip_MP4_ass.html
         * FileSize : 818.7MB
         * PublishDate : 2018-10-12 12:44:00
         */

        private String Title;
        private int TypeId;
        private String TypeName;
        private int SubgroupId;
        private String SubgroupName;
        private String Magnet;
        private String PageUrl;
        private String FileSize;
        private String PublishDate;
        private int episodeId;

        protected ResourcesBean(Parcel in) {
            Title = in.readString();
            TypeId = in.readInt();
            TypeName = in.readString();
            SubgroupId = in.readInt();
            SubgroupName = in.readString();
            Magnet = in.readString();
            PageUrl = in.readString();
            FileSize = in.readString();
            PublishDate = in.readString();
            episodeId = in.readInt();
        }

        public static final Creator<ResourcesBean> CREATOR = new Creator<ResourcesBean>() {
            @Override
            public ResourcesBean createFromParcel(Parcel in) {
                return new ResourcesBean(in);
            }

            @Override
            public ResourcesBean[] newArray(int size) {
                return new ResourcesBean[size];
            }
        };

        public String getTitle() {
            return Title;
        }

        public void setTitle(String Title) {
            this.Title = Title;
        }

        public int getTypeId() {
            return TypeId;
        }

        public void setTypeId(int TypeId) {
            this.TypeId = TypeId;
        }

        public String getTypeName() {
            return TypeName;
        }

        public void setTypeName(String TypeName) {
            this.TypeName = TypeName;
        }

        public int getSubgroupId() {
            return SubgroupId;
        }

        public void setSubgroupId(int SubgroupId) {
            this.SubgroupId = SubgroupId;
        }

        public String getSubgroupName() {
            return SubgroupName;
        }

        public void setSubgroupName(String SubgroupName) {
            this.SubgroupName = SubgroupName;
        }

        public String getMagnet() {
            return Magnet;
        }

        public void setMagnet(String Magnet) {
            this.Magnet = Magnet;
        }

        public String getPageUrl() {
            return PageUrl;
        }

        public void setPageUrl(String PageUrl) {
            this.PageUrl = PageUrl;
        }

        public String getFileSize() {
            return FileSize;
        }

        public void setFileSize(String FileSize) {
            this.FileSize = FileSize;
        }

        public String getPublishDate() {
            return PublishDate;
        }

        public void setPublishDate(String PublishDate) {
            this.PublishDate = PublishDate;
        }

        public int getEpisodeId() {
            return episodeId;
        }

        public void setEpisodeId(int episodeId) {
            this.episodeId = episodeId;
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(Title);
            dest.writeInt(TypeId);
            dest.writeString(TypeName);
            dest.writeInt(SubgroupId);
            dest.writeString(SubgroupName);
            dest.writeString(Magnet);
            dest.writeString(PageUrl);
            dest.writeString(FileSize);
            dest.writeString(PublishDate);
            dest.writeInt(episodeId);
        }
    }

    public static void searchMagnet(String anime, int typeId, int subGroupId, CommOtherDataObserver<MagnetBean> observer, NetworkConsumer consumer){
        String type = typeId == -1 ? "" : typeId+"";
        String subGroup = subGroupId == -1 ? "" : subGroupId+"";
        RetroFactory.getResInstance().searchMagnet(anime, type, subGroup)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }

    public static void downloadTorrent(String magnet, CommOtherDataObserver<ResponseBody> observer, NetworkConsumer consumer){
        RequestBody requestBody = RequestBody.create(MediaType.parse("text/plain"), magnet);
        RetroFactory.getDTInstance().downloadTorrent(requestBody)
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
