package com.etone.framework.event;

import com.etone.framework.task.PriorityAsyncTask;
import com.etone.framework.utils.LogUtils;

/**
 * Created by Administrator on 2016/7/13.
 */
public class EventTask extends PriorityAsyncTask<Object, Object, EventData>
{
    private EventListener listener;
    private EventData data;
    private TaskType taskType;
    private String eventType;

    public EventTask(String eventType, EventListener listener, EventData data, TaskType taskType)
    {
        super();
        this.listener = listener;
        this.data = data;
        this.taskType = taskType;
        this.eventType = eventType;
    }

    /*
    * 处理完任务以后，如果需要发送广播，就自行完成，这里不会以链表的形式去执行任务
    * */
    @Override
    protected EventData doInBackground(Object... params)
    {
        if (taskType == TaskType.Async)
        {
            try
            {
                listener.onEvent(data);
            }
            catch (Throwable e)
            {
                EventBus.onExceptionPostReceived(eventType, data, e);
                e.printStackTrace();
            }
        }

        return data;
    }

    /**
     * 处理完任务以后，如果需要发送广播，就自行完成，这里不会以链表的形式去执行任务
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p/>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param result The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @SuppressWarnings({})
    protected void onPostExecute(EventData result)
    {
        if (taskType == TaskType.UI)
        {
            try
            {
                listener.onEvent(data);
            }
            catch (Throwable e)
            {
                EventBus.onExceptionPostReceived(eventType, data, e);
                e.printStackTrace();
            }
        }
    }
}