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

public class AnimeTypeBean {

    private List<TypesBean> Types;

    public List<TypesBean> getTypes() {
        return Types;
    }

    public void setTypes(List<TypesBean> Types) {
        this.Types = Types;
    }

    public static class TypesBean {

        public TypesBean() {
        }

        public TypesBean(int id, String name) {
            Id = id;
            Name = name;
        }

        /**
         * Id : 0
         * Name : 未知分类
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

    public static void getAnimeType(CommOtherDataObserver<AnimeTypeBean> observer, NetworkConsumer consumer){
        RetroFactory.getResInstance().getAnimeType()
                .doOnSubscribe(consumer)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer);
    }
}
