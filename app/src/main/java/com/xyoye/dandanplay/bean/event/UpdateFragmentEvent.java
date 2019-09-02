package com.xyoye.dandanplay.bean.event;

import com.xyoye.dandanplay.ui.fragment.PersonalFragment;
import com.xyoye.dandanplay.ui.fragment.PlayFragment;

/**
 * Created by xyoye on 2019/9/2.
 */

public class UpdateFragmentEvent {
    private Class clazz;
    private int updateType;

    private UpdateFragmentEvent(Class clazz, int updateType){
        this.clazz = clazz;
        this.updateType = updateType;
    }

    public Class getClazz() {
        return clazz;
    }

    public int getUpdateType() {
        return updateType;
    }

    public static UpdateFragmentEvent updatePersonal() {
        return new UpdateFragmentEvent(PersonalFragment.class, -1);
    }

    public static UpdateFragmentEvent updatePlay(int updateType) {
        return new UpdateFragmentEvent(PlayFragment.class, updateType);
    }
}
