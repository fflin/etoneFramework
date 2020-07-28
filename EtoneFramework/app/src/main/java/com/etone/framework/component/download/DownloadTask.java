package com.etone.framework.component.download;

import com.etone.framework.component.download.Downloader.Callback;
import com.etone.framework.component.download.Downloader.StopState;
import com.etone.framework.task.PriorityAsyncTask;
import com.etone.framework.utils.IOUtils;
import com.etone.framework.utils.LogUtils;


import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;

class DownloadTask extends PriorityAsyncTask
{
    private static final int PROGRESS_UPDATE_CYCLE = 16;
    private static final int BUFFER_SIZE = 8096;

    private String url;
    private String path;
    private Callback callback;
    private int timeout;

    protected boolean isPaused;
    protected File file;

    protected AtomicInteger mRunCounter = new AtomicInteger(0);

    protected DownloadTask(String url, String path, Callback cb, int timeout)
    {
        this.url = url;
        this.path = path;
        this.callback = cb;
        this.timeout = timeout;

        this.isPaused = false;

        file = new File(path);
    }

    @Override
    protected Object doInBackground(Object[] params)
    {
        mRunCounter.incrementAndGet();
        byte[] buffer = new byte[BUFFER_SIZE];
        long targetLength, fileLength, completeSize;
        int length;

        RandomAccessFile randomAccessFile = null;

        HttpURLConnection connection = null;
        InputStream is = null;

        StopState state = StopState.SUCCESS;
        boolean needToClose = true;
        try
        {
            do {
                URL url = new URL(this.url);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(timeout);
                targetLength = connection.getContentLength();
                connection.disconnect();
                LogUtils.e("targetLength:" + targetLength);
                if (!file.exists())
                    if (!file.createNewFile())
                        throw new IOException("can't create new file on the path:" + path);
                completeSize = fileLength = file.length();
                if (targetLength == fileLength)
                {
                    needToClose = false;
                    break;
                }

                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestProperty("Range", "bytes=" + fileLength + "-" + targetLength);
                connection.setConnectTimeout(timeout);
                connection.setReadTimeout(timeout);
                randomAccessFile = new RandomAccessFile(file, "rwd");
                randomAccessFile.seek(fileLength);
                is = connection.getInputStream();
                int c = 0;
                while ((length = is.read(buffer)) != -1) {
                    if (isPaused) {
                        state = StopState.PAUSE;
                        break;
                    }

                    randomAccessFile.write(buffer, 0, length);
                    completeSize += length;

                    c++;
                    if (c == PROGRESS_UPDATE_CYCLE) {
                        if (callback != null)
                            callback.onProgress(targetLength, completeSize);
                        c = 0;
                    }
                }
            } while(false);
        }
        catch (Exception e)
        {
            state = StopState.EXCEPTION;
            if (callback != null)
                callback.onException(e);
        }
        finally
        {
            mRunCounter.decrementAndGet();
            try
            {
                if (needToClose)
                {
                    IOUtils.closeLoudly(is);
                    IOUtils.closeLoudly(randomAccessFile);

                    if (connection != null)
                        connection.disconnect();
                }
            }
            catch (Exception e)
            {
                state = Downloader.StopState.EXCEPTION;
                if (callback != null)
                    callback.onException(e);
            }
            if (callback != null)
                callback.onStop(state);
        }

        return null;
    }
}