package com.etone.framework.base;

import com.etone.framework.annotation.InjectUtils;
import com.etone.framework.component.plugin.load.internal.DLIntent;
import com.etone.framework.component.plugin.load.internal.DLPluginManager;
import com.etone.framework.event.EventData;
import com.etone.framework.event.SubscriberListener;
import com.etone.framework.utils.LogUtils;

/**
 * Created by Administrator on 2016/7/28.
 */
public class BaseContraller implements SubscriberListener
{
    public BaseContraller()
    {
        InjectUtils.injectOnlyEvent(this);
    }

    @Override
    public void onEventException(String eventType, EventData data, Throwable e)
    {
        e.printStackTrace();
    }
}
