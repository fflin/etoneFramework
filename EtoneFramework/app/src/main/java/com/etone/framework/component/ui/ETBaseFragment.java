package com.etone.framework.component.ui;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etone.framework.annotation.InjectUtils;
import com.etone.framework.base.InstanceHolder;
import com.etone.framework.component.plugin.load.DLBasePluginFragmentActivity;
import com.etone.framework.component.plugin.load.DLProxyFragmentActivity;
import com.etone.framework.event.EventBus;
import com.etone.framework.event.EventData;
import com.etone.framework.event.SubscriberListener;

public class ETBaseFragment extends Fragment implements SubscriberListener
{
    public View v;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstance)
    {
        if (this.v == null)
        {
            this.v = InjectUtils.injectFragment(this, inflater, container);
        }

        return v;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        InstanceHolder.putInstance(this);
        InjectUtils.injectEvent (this.getClass(), this);
    }

    public FragmentActivity getThatActivity()
    {
        FragmentActivity a = this.getActivity();
        if (a instanceof DLBasePluginFragmentActivity)
            return ((DLBasePluginFragmentActivity) a).getThatFragmentActivity();

        return a;
    }

    @Override
    public void onDestroy()
    {
        EventBus.unregisterListenerAll(this);
        InstanceHolder.deleteInstance(this.getClass());

        super.onDestroy();
    }

    @Override
    public void onEventException(String eventType, EventData data, Throwable e)
    {

    }
}
