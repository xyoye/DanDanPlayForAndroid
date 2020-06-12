package com.xyoye.dandanplay.utils.smb;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.LinkAddress;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;

import com.xyoye.dandanplay.app.IApplication;

import java.math.BigInteger;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Enumeration;
import java.util.List;

/**
 * Created by xyoye on 2018/11/19.
 */

public class LocalIPUtil {
    public LocalIPUtil(){
    }

    public String getLocalIp(){
        String ip = findLocalIp1();
        if (ip != null)
            return ip;
        ip = findLocalIp2();
        if (ip != null)
            return ip;
        ip = findLocalIp3();
        if (ip != null)
            return ip;
        return null;
    }

    private String findLocalIp1(){
        ConnectivityManager connMgr = (ConnectivityManager) IApplication.get_context().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && connMgr != null) {
            LinkProperties linkProperties;
            String ipAddress = null;
            linkProperties = getLinkProperties(connMgr, NetworkCapabilities.TRANSPORT_VPN);
            if (linkProperties == null)
                linkProperties = getLinkProperties(connMgr, NetworkCapabilities.TRANSPORT_ETHERNET);
            if (linkProperties == null)
                linkProperties = getLinkProperties(connMgr, NetworkCapabilities.TRANSPORT_WIFI);
            if (linkProperties != null)
                ipAddress =  getIp(linkProperties);
            if (ipAddress != null)
                return ipAddress;
        }
        return null;
    }

    private String findLocalIp2(){
        ConnectivityManager connMgr = (ConnectivityManager) IApplication.get_context().getSystemService(Context.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifiInfo;
        if (connMgr != null) {
            wifiInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (wifiInfo.isConnected()) {
                WifiManager myWifiManager = (WifiManager) IApplication.get_context().getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo myWifiInfo;
                if (myWifiManager != null) {
                    myWifiInfo = myWifiManager.getConnectionInfo();
                    byte[] bytes = BigInteger.valueOf(myWifiInfo.getIpAddress()).toByteArray();
                    InetAddress address;
                    try {
                        address = InetAddress.getByAddress(bytes);
                        if (address != null && address.isSiteLocalAddress())
                            return address.getHostAddress();
                    } catch (UnknownHostException ignored) {
                        ignored.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

    private String findLocalIp3(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();)
                {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && (inetAddress instanceof Inet4Address))
                    {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        }
        catch (SocketException ex){
            ex.printStackTrace();
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private LinkProperties getLinkProperties(ConnectivityManager connectivityManager, int cap) {
        Network nets[] = connectivityManager.getAllNetworks();
        for (Network n: nets) {
            LinkProperties linkProperties = connectivityManager.getLinkProperties(n);
            NetworkCapabilities networkCapabilities = connectivityManager.getNetworkCapabilities(n);
            String interfaceName =  linkProperties.getInterfaceName();
            if (interfaceName != null && networkCapabilities != null) {
                if (networkCapabilities.hasTransport(cap))
                    return linkProperties;
            }
        }
        return null;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private String getIp(LinkProperties lp) {
        List<LinkAddress> linkAddresses = lp.getLinkAddresses();
        for(LinkAddress linkAddress: linkAddresses) {
            InetAddress inetAddress = linkAddress.getAddress();
            if (inetAddress instanceof Inet4Address) {
                return inetAddress.getHostAddress();
            }
        }
        return null;
    }
}
