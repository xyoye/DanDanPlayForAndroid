package com.xyoye.dandanplay.bean;

/**
 * Created by xyoye on 2020/2/23.
 */

public class ShooterErrotResult {


    /**
     * status : 30900
     * errmsg : you are exceeding request limits
     */

    private int status;
    private String errmsg;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getErrmsg() {
        return errmsg;
    }

    public void setErrmsg(String errmsg) {
        this.errmsg = errmsg;
    }
}
