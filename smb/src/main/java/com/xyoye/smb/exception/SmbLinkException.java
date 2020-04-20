package com.xyoye.smb.exception;

import com.xyoye.smb.info.SmbType;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xyoye on 2019/12/20.
 */

public class SmbLinkException extends Exception {
    private List<DetailException> detailExceptions;

    public SmbLinkException() {
        detailExceptions = new ArrayList<>();
    }

    public void addException(SmbType smbType, String msg) {
        detailExceptions.add(new DetailException(smbType, msg));
    }

    public void clearException() {
        detailExceptions.clear();
    }

    public String getExceptionString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (DetailException exception : detailExceptions) {
            String typeName = exception.getSmbType().toString();
            stringBuilder.append("\n")
                    .append("Type: ")
                    .append(typeName)
                    .append("\n")
                    .append("Error: ")
                    .append(exception.getErrorMsg())
                    .append("\n");
        }
        return stringBuilder.toString();
    }

    public List<DetailException> getDetailExceptions() {
        return detailExceptions;
    }

    public static class DetailException {
        private SmbType smbType;
        private String errorMsg;

        DetailException(SmbType smbType, String errorMsg) {
            this.smbType = smbType;
            this.errorMsg = errorMsg;
        }

        public String getErrorMsg() {
            return errorMsg;
        }

        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        public SmbType getSmbType() {
            return smbType;
        }

        public void setSmbType(SmbType smbType) {
            this.smbType = smbType;
        }
    }
}
