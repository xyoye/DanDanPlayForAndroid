package com.xyoye.dandanplay.utils.net;

import java.io.Serializable;

/**
 * Created by yzd on 2017/5/15 0015.
 */

public class CommJsonEntity implements Serializable {

    private int errorCode;
    private boolean success;
    private String errorMessage;

    public int getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean isSuccess() {
        return this.success;
    }
}
