package com.xyoye.dandanplay.bean;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

/**
 * Created by xyoye on 2019/7/11.
 */

public class RemoteScanBean implements Parcelable {

    /**
     * ip : ["192.168.1.240","192.168.32.1","192.168.160.1"]
     * port : 80
     * machineName :
     * tokenRequired :
     */

    private int port;
    private String machineName;
    private boolean tokenRequired;
    private String authorization;
    private List<String> ip;

    public RemoteScanBean() {

    }

    protected RemoteScanBean(Parcel in) {
        port = in.readInt();
        machineName = in.readString();
        tokenRequired = in.readByte() != 0;
        authorization = in.readString();
        ip = in.createStringArrayList();
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getMachineName() {
        return machineName;
    }

    public void setMachineName(String machineName) {
        this.machineName = machineName;
    }

    public boolean isTokenRequired() {
        return tokenRequired;
    }

    public void setTokenRequired(boolean tokenRequired) {
        this.tokenRequired = tokenRequired;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public List<String> getIp() {
        return ip;
    }

    public void setIp(List<String> ip) {
        this.ip = ip;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(port);
        dest.writeString(machineName);
        dest.writeByte((byte) (tokenRequired ? 1 : 0));
        dest.writeString(authorization);
        dest.writeStringList(ip);
    }

    public static final Creator<RemoteScanBean> CREATOR = new Creator<RemoteScanBean>() {
        @Override
        public RemoteScanBean createFromParcel(Parcel in) {
            return new RemoteScanBean(in);
        }

        @Override
        public RemoteScanBean[] newArray(int size) {
            return new RemoteScanBean[size];
        }
    };
}
