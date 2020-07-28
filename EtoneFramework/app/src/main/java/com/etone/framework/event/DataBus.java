package com.etone.framework.event;

import com.etone.framework.cache.LruMemoryCache;
import com.etone.framework.task.PriorityExecutor;

import java.util.LinkedHashSet;

/**
 * Created by Administrator on 2016/7/13.
 */
public class DataBus
{
    private static final LruMemoryCache<String, LinkedHashSet<String>> e = new LruMemoryCache<>(0);

    public void main()
    {
        PriorityExecutor pe = new PriorityExecutor(5);
        EventTask et = new EventTask("", null, null, TaskType.Async);
        et.executeOnExecutor(pe);
    }
}
