package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2018/10/15.
 */

public class SelectInfoEvent {
    public static int TYPE = 1;
    public static int SUBGROUP = 2;

    private int selectId;
    private String selectName;
    private int type;

    public SelectInfoEvent(int type, int selectId, String selectName) {
        this.selectId = selectId;
        this.type = type;
        this.selectName = selectName;
    }

    public int getSelectId() {
        return selectId;
    }

    public void setSelectId(int selectId) {
        this.selectId = selectId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getSelectName() {
        return selectName;
    }

    public void setSelectName(String selectName) {
        this.selectName = selectName;
    }
}
