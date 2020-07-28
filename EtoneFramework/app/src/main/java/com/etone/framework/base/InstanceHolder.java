package com.etone.framework.base;

import android.app.Activity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/7/28.
 */

/*句柄存储类，方便Activity等之间的调用，但是会有生命周期的风险，因此需要仔细确认生命周期才能使用
* 如果一个key put了两次，那么句柄以第二次的为准*/
public final class InstanceHolder
{
    /*所有需要存储的instance都可以放在这里面*/
    private static final HashMap<Class, Object> cache = new HashMap<>();

    private static final ArrayList<Activity> activityList = new ArrayList<>();

    /*禁止new它*/
    private InstanceHolder(){}

    public static void addActivity(Activity activity)
    {
        synchronized (activityList)
        {
            activityList.add(activity);
        }
    }

    public static void onTerminate()
    {
        synchronized (activityList)
        {
            for (Activity activity : activityList)
            {
                if (activity != null)
                    activity.finish();
            }
        }
    }

    public static void putInstance(Object value)
    {
        synchronized (cache)
        {
            cache.put(value.getClass(), value);
        }
    }

    /*如果当前缓存里面有这个句柄，那么就可以返回它*/
    public static Object getInstance(Class key)
    {
        synchronized (cache)
        {
            if (cache.containsKey(key))
                return cache.get(key);
        }

        return null;
    }

    public static void deleteInstance(Class key)
    {
        synchronized (cache)
        {
            if (cache.containsKey(key))
                cache.remove(key);
        }
    }
}
