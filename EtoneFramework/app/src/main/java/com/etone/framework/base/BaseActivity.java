package com.etone.framework.base;

import android.app.Activity;
import android.os.Bundle;

import com.etone.framework.annotation.InjectUtils;
import com.etone.framework.event.EventBus;
import com.etone.framework.event.EventData;
import com.etone.framework.event.SubscriberListener;
import com.etone.framework.utils.LogUtils;

/**
 * Created by Administrator on 2016/7/14.
 */
public class BaseActivity extends Activity implements SubscriberListener
{
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        InstanceHolder.putInstance(this);
        InjectUtils.injectActivity(this);
        InstanceHolder.addActivity(this);
    }

    @Override
    public void onEventException(String eventType, EventData data, Throwable e)
    {
        e.printStackTrace();
    }

    @Override
    public void onDestroy()
    {
        EventBus.unregisterListenerAll(this);
        InstanceHolder.deleteInstance(this.getClass());
        super.onDestroy();
    }
}
