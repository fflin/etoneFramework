package com.etone.framework.component.http;


import com.etone.framework.event.EventBus;
import com.etone.framework.task.PriorityAsyncTask;
import com.etone.framework.utils.LogUtils;
import com.etone.framework.utils.StringUtils;
import com.etone.framework.component.http.HttpParams.FileUploader;
import com.etone.framework.component.http.HttpParams.UploadListener;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

final class HttpTask extends PriorityAsyncTask
{
    private static final int FILE_TIME_OUT = 10 * 1000000;
    private static final String PERFIX = "--";
    private static final String LINE_END = "\r\n";
    private static String ANDROID_VERSION = "";
    private static String ANDROID_MODEL = "";

    private final HttpParams hp;

    public HttpTask(HttpParams hp)
    {
        this.hp = hp;
    }

    public static void setAndroidProperty(String version, String model)
    {
        ANDROID_VERSION = version;
        ANDROID_MODEL = model;
    }

    @Override
    protected final HttpParams doInBackground(Object... params)
    {
        HttpParams hp = this.hp;
        String eventType = hp.getHttpEvent();
        try
        {
            hp.content = getContent(hp);
            UploadListener listener = hp.getUploadListener();
            if (listener != null)
                listener.onAllFinish();
            EventBus.onPostReceived (eventType, hp);
        }
        catch (Throwable e)
        {
            EventBus.onExceptionPostReceived(eventType, hp, e);
            e.printStackTrace();
        }

        return hp;
    }

    private String getContent(HttpParams hp) throws Exception
    {
        if (HttpUtils.isDebug)
            LogUtils.e("url:" + hp.url);
        URL url = new URL (hp.url);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        boolean hasFileToUpload = (hp.getFiles() != null && hp.getFiles().size() != 0);
        if (hasFileToUpload)
        {
            conn.setConnectTimeout(10 * 1000000);
            conn.setReadTimeout(10 * 10000000);
        }
        else
        {
            conn.setConnectTimeout(hp.timeOut);
            conn.setReadTimeout(hp.timeOut);
        }
        conn.setRequestMethod(hp.method.getValue());

        setHeader(conn, hp);
        setCookie(conn, hp.cookieToSend);

        //这里已经把参数传过去了，目前只支持json
        //TODO 明天把upload封装成一个类，丢到httpParams里面去，其中包括了服务器接收的参数，一般的参数先不考虑了
        if (hasFileToUpload)
        {
            uploadFile(conn, hp);
        }
        else
        {
            sendParams(conn, hp.alwaysSend);
            sendParams(conn, hp.getParams());
            sendJsonParams(conn, hp.getJsonParams());
        }

        hp.responseCode = conn.getResponseCode();
        if (HttpUtils.isDebug)
            LogUtils.e("responseCode:" + hp.responseCode);
        if (hp.responseCode == 401)
            throw new AuthException(hp.url);
        InputStream is = conn.getInputStream();
        Map<String, List<String>> headers = conn.getHeaderFields();
        boolean isGZip = headerCheck(headers);

        String res = getConnectionResult(is, hp, isGZip);
        LogUtils.e("res=" + res);
        getCookie(conn, hp);

        is.close();
        conn.disconnect();

        return res;
    }

    private void uploadFile(HttpURLConnection conn, HttpParams hp) throws Exception
    {
        UploadListener listener = hp.getUploadListener();
        String BOUNDARY = UUID.randomUUID().toString();
        String CONTENT_TYPE = "multipart/form-data";
        ArrayList<FileUploader> files = hp.getFiles();
        HashMap<String, String> params = hp.getParams();
        //第一步，设置参数
        conn.setRequestProperty("connection", "keep-alive");
        conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary="+BOUNDARY);
        conn.setDoInput(true);
        conn.setDoOutput(true);
        conn.setUseCaches(false);
        //第二步，发送json数据
        StringBuilder sbParams = new StringBuilder();
        sbParams.append(LINE_END);
        for (Map.Entry<String, String> entry : params.entrySet())
        {
            sbParams.append(PERFIX).append(BOUNDARY).append(LINE_END);
            sbParams.append("Content-Disposition:form-data;name=\"" + entry.getKey() + "\"").append(LINE_END);
            sbParams.append("Content-Type:text/plain;charset=\"utf-8\"").append(LINE_END);
            sbParams.append("Content-Transfer-Encoding:8bit").append(LINE_END);
            sbParams.append(LINE_END);
            sbParams.append(entry.getValue());
            sbParams.append(LINE_END);
        }
        DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
        dos.write(sbParams.toString().getBytes());
        //第三步，开始发送文件
        byte[] bytes = new byte[1024];
        long totalBytes;
        long curBytes;
        int len;
        for (int i=0; i<files.size(); i++)
        {
            FileUploader file = files.get(i);
            sbParams = new StringBuilder();
            sbParams.append(PERFIX).append(BOUNDARY).append(LINE_END);
            sbParams.append("Content-Disposition:form-data;name=\""+file.key+"\";filename=\"" + file.fileName + "\"").append(LINE_END);
            sbParams.append("Content-Type:application/octet-stream;charset=\"utf-8\"").append(LINE_END);
            sbParams.append(LINE_END);
            dos.write(sbParams.toString().getBytes());
            InputStream is = new FileInputStream(file.file);
            totalBytes = file.file.length();
            curBytes = 0;
            len = 0;
            while ((len = is.read(bytes)) != -1)
            {
                curBytes += len;
                dos.write(bytes, 0, len);
                if (listener != null)
                    listener.onProgress(file, curBytes, 1.0d * curBytes / totalBytes);
            }
            is.close();
            dos.write(LINE_END.getBytes());
            if (listener != null)
                listener.onFinish(file);
        }
        byte[] end_data = (PERFIX + BOUNDARY + PERFIX + LINE_END).getBytes();
        dos.write(end_data);
        dos.flush();
        dos.close();
    }

    private String getConnectionResult(InputStream is, HttpParams hp, boolean isGZip) throws IOException
    {
        byte[] buf = new byte[hp.bufferSize];
        InputStream iis;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        if (isGZip)
            iis = new GZIPInputStream(is);
        else
            iis = is;

        int count;
        while ((count = iis.read(buf)) >= 0)
            out.write(buf, 0, count);

        byte[] resp = out.toByteArray();

        String res = new String(resp, hp.charset);
        out.close();

        return res;
    }

    private boolean headerCheck(Map<String, List<String>> header) throws AuthException
    {
        boolean isGZip = false;
        Set<Map.Entry<String, List<String>>> set = header.entrySet();
        Iterator<Map.Entry<String, List<String>>> iterator = set.iterator();

        Map.Entry<String, List<String>> entry;
        List<String> res;
        Iterator<String> iteratorRes;
        String tmp;
        while (iterator.hasNext())
        {
            entry = iterator.next();
            res = entry.getValue();
            iteratorRes = res.iterator();
            while(iteratorRes.hasNext())
            {
                tmp = iteratorRes.next();
                if (tmp.contains("gzip"))
                {
                    isGZip = true;
                    break;
                }
                else if (tmp.contains("Unauthorized"))
                {
                    throw new AuthException(hp.url);
                }
            }
        }

        return isGZip;
    }

    private void setHeader(HttpURLConnection conn, HttpParams hp)
    {
        HashMap<String, String> header = hp.getHeader();
        conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Linux; U; Android " + ANDROID_VERSION + "; en-us; " + ANDROID_MODEL + ") AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1");
        switch (hp.accept)
        {
            case JSON:
                conn.setRequestProperty("Accept", "application/json; charset=utf-8");
                conn.setRequestProperty("accept-charset", "utf-8");
                conn.setRequestProperty("Content-Type", "application/json;");
                break;
            case XML:
                conn.setRequestProperty("Accept", "application/xml");
                conn.setRequestProperty("Content-Type", "application/xml");
                break;
            case URLENCODED:
                conn.setRequestProperty("Accept", "application/x-www-form-urlencoded");
                conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                break;
        }

        if (hp.contentType != null)
        {
            switch (hp.contentType)
            {
                case HTML:
                    conn.setRequestProperty("Content-Type", "text/html;");
                    break;
                case JSON:
                    conn.setRequestProperty("Content-Type", "application/json;");
                    break;
                case URLENCODED:
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;");
                    break;
            }
        }
        conn.setRequestProperty("Accept-Encoding", "gzip, deflate");

        if (header != null)
        {
            Set<String> set = header.keySet();

            String value;
            for (String key : set)
            {
                value = header.get(key);
                conn.setRequestProperty(key, value);
            }
        }
    }

    private void setCookie(HttpURLConnection conn, HttpAuthCookie hac)
    {
        if (hac != null)
        {
            if (!StringUtils.isEmpty(hac.value) && !StringUtils.isEmpty(hac.value))
                conn.setRequestProperty("Cookie", hac.key + "=" + hac.value);

            /*设置Authorization的token*/
            if (!StringUtils.isEmpty(hac.token))
                conn.setRequestProperty("Authorization", hac.token);
        }
    }

    private void getCookie(HttpURLConnection conn, HttpParams hp)
    {
        String cookie = conn.getHeaderField("set-cookie");
        if (cookie != null)
        {
            LogUtils.e(cookie);
            hp.cookieToGet = new HttpAuthCookie();
            hp.cookieToGet.key = hp.cookieKey;
            Pattern p = Pattern.compile(hp.cookieToGet.key + "=[^;]*");
            Matcher m = p.matcher(cookie);

            String ss;
            if (m.find())
            {
                ss = m.group();
                LogUtils.e("cookie.value:" + ss);
                hp.cookieToGet.value = ss.replace(hp.cookieToGet.key + "=", "").replace(";", "");
            }
        }
    }

    private void sendParams(HttpURLConnection conn, HashMap<String, String> params) throws Exception
    {
        if (params != null)
        {
            StringBuilder p = new StringBuilder();
            Set<String> set = params.keySet();
            if (set.size() == 0)
                return;
            String value;
            for (String key : set)
            {
                value = params.get(key);
                p.append(key).append("=").append(value).append("&");
            }

            p.substring(0,p.length()-1);
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(p.toString().getBytes());
            outStream.flush();
            outStream.close();
        }
    }

    private void sendJsonParams(HttpURLConnection conn, HashMap<String, Object> params) throws Exception
    {
        String json = null;
        if (params != null)
        {
            Set<String> set = params.keySet();
            Iterator<String> it = set.iterator();
            JSONObject obj = new JSONObject();

            String key;
            Object value;
            JSONArray jsonList;
            String[] tmpStr;
            int[] tmpInt;
            while (it.hasNext())
            {
                key = it.next();
                value = params.get(key);
                if (StringUtils.isEmpty(key) && value != null)
                    continue;

                if (value instanceof String[])
                {
                    jsonList = new JSONArray();
                    tmpStr = (String[])  value;
                    for(int i=0; i<tmpStr.length; i++)
                        jsonList.put(tmpStr[i]);
                    obj.put(key, jsonList);
                }
                else if (value instanceof int[])
                {
                    jsonList = new JSONArray();
                    tmpInt = (int[]) value;
                    for (int i=0; i< tmpInt.length; i++)
                        jsonList.put(tmpInt[i]);
                    obj.put(key, jsonList);
                }
                else
                {
                    obj.put(key, value);
                }
            }

            json = obj.toString();
            LogUtils.e("sendJson:" + json);
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(json.getBytes());
            outStream.flush();
            outStream.close();
        }
    }
}