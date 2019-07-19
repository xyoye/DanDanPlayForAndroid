package com.xyoye.dandanplay.utils.smbv2.http;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by xyoye on 2019/7/15.
 */
public class HttpSocket {
    private Socket mSocket;
    private InputStream socketInputStream = null;
    private OutputStream socketOutputStream = null;

    public HttpSocket(Socket socket) {
        this.mSocket = socket;
        open();
    }

    public boolean open() {
        try {
            socketInputStream = mSocket.getInputStream();
            socketOutputStream = mSocket.getOutputStream();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void close() {
        try {
            if (socketInputStream != null) {
                socketInputStream.close();
            }
            if (socketOutputStream != null) {
                socketOutputStream.close();
            }
            mSocket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void finalize() {
        close();
    }

    public boolean isClosed(){
        return mSocket.isClosed();
    }

    public InputStream getInputStream() {
        return socketInputStream;
    }

    public OutputStream getOutputStream() {
        return socketOutputStream;
    }
}
