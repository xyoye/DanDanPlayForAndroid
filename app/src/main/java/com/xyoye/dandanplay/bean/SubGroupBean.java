package com.xyoye.dandanplay.bean;

import com.xyoye.dandanplay.utils.net.CommOtherDataObserver;
import com.xyoye.dandanplay.utils.net.NetworkConsumer;
import com.xyoye.dandanplay.utils.net.RetroFactory;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by xyoye on 2018/10/15.
 */

public class SubGroupBean {

    private List<SubgroupsBean> Subgroups;

    public List<SubgroupsBean> getSubgroups() {
        return Subgroups;
    }

    public void setSubgroups(List<SubgroupsBean> Subgroups) {
        this.Subgroups = Subgroups;
    }

    public static class SubgroupsBean {

        public SubgroupsBean() {
        }

        public SubgroupsBean(int id, String name) {
            Id = id;
            Name = name;
        }

        /**
         * Id : 0
         * Name : 未知字幕组
         */

        private int Id;
        private String Name;

        public int getId() {
            return Id;
        }

        public void setId(int Id) {
            this.Id = Id;
        }

        public String getName() {
            return Name;
        }

        public void setName(String Name) {
            this.Name = Name;
        }
    }

    public static void getSubGroup(CommOtherDataObserver<SubGroupBean> observer, NetworkConsumer consumer){
        RetroFactory.getResInstance().getSubGroup()
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
