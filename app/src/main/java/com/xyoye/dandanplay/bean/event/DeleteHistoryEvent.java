package com.xyoye.dandanplay.bean.event;

/**
 * Created by xyoye on 2019/1/8.
 */

public class DeleteHistoryEvent {
    private int deletePosition;
    private boolean isDeleteAll;

    public DeleteHistoryEvent(int deletePosition, boolean isDeleteAll) {
        this.deletePosition = deletePosition;
        this.isDeleteAll = isDeleteAll;
    }

    public int getDeletePosition() {

        return deletePosition;
    }

    public void setDeletePosition(int deletePosition) {
        this.deletePosition = deletePosition;
    }

    public boolean isDeleteAll() {
        return isDeleteAll;
    }

    public void setDeleteAll(boolean deleteAll) {
        isDeleteAll = deleteAll;
    }
}
