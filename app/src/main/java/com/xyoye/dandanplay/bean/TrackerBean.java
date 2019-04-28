package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2019/4/28.
 */

public class TrackerBean {
    private String tracker;
    private boolean isSelected;
    private boolean isSelectType;

    public String getTracker() {
        return tracker;
    }

    public void setTracker(String tracker) {
        this.tracker = tracker;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isSelectType() {
        return isSelectType;
    }

    public void setSelectType(boolean selectType) {
        isSelectType = selectType;
    }
}
