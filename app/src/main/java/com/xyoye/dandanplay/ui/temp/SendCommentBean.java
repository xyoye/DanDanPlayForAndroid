package com.xyoye.dandanplay.ui.temp;

/**
 * Title: SendCommentBean <br>
 * Description: 发送弹幕的bean<br>
 * Copyright (c) 传化物流版权所有 2017 <br>
 * Created DateTime: 2017-2-13 15:27
 * Created by Wentao.Shi.
 */
public class SendCommentBean {

    /**
     * Time : 1.23
     * Mode : 1
     * Color : 16777215
     * Timestamp : 0
     * Pool : 0
     * UId : 0
     * Cid : 0
     * Token : 0
     * Message : “弹幕测试”
     */

    private double Time;
    private int Mode;
    private int Color;
    private int Timestamp;
    private int Pool;
    private int UId;
    private int Cid;
    private int Token;
    private String Message;

    public double getTime() {
        return Time;
    }

    public void setTime(double Time) {
        this.Time = Time;
    }

    public int getMode() {
        return Mode;
    }

    public void setMode(int Mode) {
        this.Mode = Mode;
    }

    public int getColor() {
        return Color;
    }

    public void setColor(int Color) {
        this.Color = Color;
    }

    public int getTimestamp() {
        return Timestamp;
    }

    public void setTimestamp(int Timestamp) {
        this.Timestamp = Timestamp;
    }

    public int getPool() {
        return Pool;
    }

    public void setPool(int Pool) {
        this.Pool = Pool;
    }

    public int getUId() {
        return UId;
    }

    public void setUId(int UId) {
        this.UId = UId;
    }

    public int getCid() {
        return Cid;
    }

    public void setCid(int Cid) {
        this.Cid = Cid;
    }

    public int getToken() {
        return Token;
    }

    public void setToken(int Token) {
        this.Token = Token;
    }

    public String getMessage() {
        return Message;
    }

    public void setMessage(String Message) {
        this.Message = Message;
    }
}
