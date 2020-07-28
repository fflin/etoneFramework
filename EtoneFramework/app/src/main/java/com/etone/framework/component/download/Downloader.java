package com.etone.framework.component.download;

import com.etone.framework.task.PriorityExecutor;
import com.etone.framework.utils.LogUtils;

import java.io.File;

public class Downloader
{
    private static final int DEFAULT_TIMEOUT = 10000;
    private static final int THREAD_POOL_SIZE = 16;

    protected static final PriorityExecutor executorService = new PriorityExecutor(THREAD_POOL_SIZE);

    public enum StopState
    {
        SUCCESS, PAUSE, EXCEPTION
    }

    public Downloader(String url, String path)
    {
        init(url, path, null, DEFAULT_TIMEOUT);
    }

    public Downloader(String url, String path, Callback callback)
    {
        init(url, path, callback, DEFAULT_TIMEOUT);
    }

    public Downloader(String url, String path, Callback callback, int timeout)
    {
        init(url, path, callback, timeout);
    }

    private void init(String url, String path, Callback callback, int timeout)
    {
        this.url = url;
        this.path = path;
        this.callback = callback;
        this.timeout = timeout;
    }

    private String url;
    private String path;
    private Callback callback;
    private int timeout;

    protected DownloadTask task;

    public void setListener(Callback callback)
    {
        this.callback = callback;
    }

    public void doStart()
    {
        task = new DownloadTask(url, path, callback, timeout);

        task.executeOnExecutor(executorService);
    }

    public void doPause()
    {
        task.isPaused = true;
    }

    public void doCancel()
    {
        if (task != null && !task.isPaused)
        {
            task.isPaused = true;
            while (task.mRunCounter.get() > 0)
            {
                LogUtils.e("mRunCounter.wait ..........");
            }
        }

        File f = new File(path);
        if (f.exists())
        {
            try
            {
                if (!f.delete())
                    f.deleteOnExit();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public void doRestart()
    {
        doCancel();
        doStart();
    }

    public boolean isRunning()
    {
        return task.mRunCounter.get() > 0;
    }

    public interface Callback
    {
        //更新进度
        void onProgress(long totalSize, long completeSize);

        //下载停止，其中包括（0:成功，1:暂停，2:异常终止）
        void onStop(StopState state);

        //当下载出现异常时回调此函数
        void onException(Exception e);
    }
}