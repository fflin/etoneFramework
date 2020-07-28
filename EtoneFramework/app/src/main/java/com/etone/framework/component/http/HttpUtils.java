package com.etone.framework.component.http;

import com.etone.framework.task.PriorityExecutor;

import java.util.HashMap;

public final class HttpUtils
{
    private static  final int THREAD_POOL_SIZE = 16;
    public static boolean isDebug = false;

    /*Http模块的全局变量*/
    public static int HTTP_TIME_OUT = -1;
    public static int HTTP_BUFF_SIZE = -1;
    public static String HTTP_CHARSET = null;
    public static String HTTP_COOKIE_KEY = null;
    public static HttpParams.Accept HTTP_ACCEPT = null;
    public static HttpParams.Content HTTP_CONTENT = null;

    private static final PriorityExecutor executorService = new PriorityExecutor(THREAD_POOL_SIZE);
    public static final HashMap<String, String> alwaysSend = new HashMap<>();

    public static void setAndroidProperty(String version, String model)
    {
        HttpTask.setAndroidProperty(version, model);
    }

    public static void run (HttpParams hp)
    {
        if (hp == null)
            return;

        HttpTask httpTask = new HttpTask(hp);
        httpTask.executeOnExecutor(executorService);
    }
}