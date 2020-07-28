package com.etone.framework.event;

/**
 * Created by Administrator on 2016/7/13.
 */
public interface SubscriberListener
{
    /**
     * 当事件产生异常时
     * */
    public void onEventException(String eventType, EventData data, Throwable e);
}
