package com.etone.framework.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;

/**
 * Created by zhuo on 2016/1/19.
 * 网络工具类
 */
public class NetWorkUtils {

    /**
     * 没有网络
     */
    public static final String NETWORKTYPE_INVALID = "NO";

    /**
     * 2G网络
     */
    public static final String NETWORKTYPE_2G = "2G";
    /**
     * 3G
     */
    public static final String NETWORKTYPE_3G = "3G";
    /**
     * 4G
     */
    public static final String NETWORKTYPE_4G = "4G";
    /**
     * wifi网络
     */
    public static final String NETWORKTYPE_WIFI = "WIFI";

    /**
     * wifi网络
     */
    public static final String NETWORKTYPE_UNKNOWN = "UNKNOWN";


    /**
     * 获取当前网络类型
     *
     * @param context
     * @return 2G/3G/4G/WIFI/no/unknown
     */
    public static String getNetType(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isAvailable())
        {
            return NETWORKTYPE_INVALID;
        }

        NetworkInfo infos[] = cm.getAllNetworkInfo();
        boolean isNetworkOk = false;
        for (int i=0; i<infos.length; i++)
        {
            if (infos[i].getState() == NetworkInfo.State.CONNECTED)
            {
                isNetworkOk = true;
                break;
            }
        }

        if (isNetworkOk == false)
            return NETWORKTYPE_INVALID;

        if (info.getType() == ConnectivityManager.TYPE_WIFI)
        {
            return NETWORKTYPE_WIFI;
        }
        if (info.getType() == ConnectivityManager.TYPE_MOBILE)
        {
            int sub = info.getSubtype();

            LogUtils.e("network type:"+sub);
            switch (sub)
            {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA://电信的2G
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    //以上的都是2G网络
                    return NETWORKTYPE_2G;

                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                    //以上的都是3G网络
                    return NETWORKTYPE_3G;

                case TelephonyManager.NETWORK_TYPE_LTE:
                case 18:

                    return NETWORKTYPE_4G;

                case TelephonyManager.NETWORK_TYPE_UNKNOWN:

                    return NETWORKTYPE_INVALID;

                default:
                    return NETWORKTYPE_UNKNOWN;
            }
        }
        return NETWORKTYPE_UNKNOWN;

    }


    /*
    * 获取IP地址
    *
    * */
    public static String getIPAddress(Context context) {

        if (NETWORKTYPE_WIFI.equals(getNetType(context))) {
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            // 获取32位整型IP地址
            int ipAddress = wifiInfo.getIpAddress();

            //返回整型地址转换成“*.*.*.*”地址
            return String.format("%d.%d.%d.%d",
                    (ipAddress & 0xff), (ipAddress >> 8 & 0xff),
                    (ipAddress >> 16 & 0xff), (ipAddress >> 24 & 0xff));
        } else {
            try {
                for (Enumeration<NetworkInterface> en = NetworkInterface
                        .getNetworkInterfaces(); en.hasMoreElements(); ) {
                    NetworkInterface intf = en.nextElement();
                    for (Enumeration<InetAddress> enumIpAddr = intf
                            .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                        InetAddress inetAddress = enumIpAddr.nextElement();
                        if (!inetAddress.isLoopbackAddress()
                                && inetAddress instanceof Inet4Address) {
                            // if (!inetAddress.isLoopbackAddress() && inetAddress
                            // instanceof Inet6Address) {
                            return inetAddress.getHostAddress().toString();
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return "";

        }

    }


}
