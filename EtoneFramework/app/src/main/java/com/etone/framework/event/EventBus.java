package com.etone.framework.event;

import android.os.Looper;

import com.etone.framework.annotation.DynamicHandler;
import com.etone.framework.cache.LruMemoryCache;
import com.etone.framework.task.PriorityExecutor;
import com.etone.framework.utils.LogUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Created by Administrator on 2016/7/13.
 */
final public class EventBus
{
    /*最大缓存元素的个数，并非实际内存占用数量*/
    private static final int SUBSCRIBER_CACHE_SIZE = 256;
    private static final int METHOD_CACHE_SIZE = 256;

    /*每一个事件具体对应到的类*/
    private static final LruMemoryCache<String, CopyOnWriteArraySet<SubscriberListener>> subscriberList = new LruMemoryCache<>(SUBSCRIBER_CACHE_SIZE);

    /*每一个事件具体对应到的方法*/
    private static final LruMemoryCache<SubscriberListener, ConcurrentHashMap<String, MethodInfo>> subscriberMethod = new LruMemoryCache<>(METHOD_CACHE_SIZE);

    /*开辟一个线程池，最大数量为5*/
    private static final PriorityExecutor executorService = new PriorityExecutor(5);

    private EventBus(){}

    public static void showSubscriberListInfo()
    {
        LruMemoryCache<String, CopyOnWriteArraySet<SubscriberListener>> subscribers = subscriberList;
        LruMemoryCache<SubscriberListener, ConcurrentHashMap<String, MethodInfo>> methods = subscriberMethod;

        Iterator<SubscriberListener> iterator = methods.iteratorSnapshot();
        SubscriberListener listener = null;
        ConcurrentHashMap<String, MethodInfo> map = null;
        LogUtils.e("EventBus Memory Info:");
        while(iterator.hasNext())
        {
            listener = iterator.next();
            LogUtils.e("listener:" + listener.getClass().getName());
            map = methods.get(listener);
            Iterator<String> iterator1 = map.keySet().iterator();
            while(iterator1.hasNext())
            {
                String key = iterator1.next();
                MethodInfo mi = map.get(key);
                LogUtils.e("    key:" + key + ",    method:" + mi.methodName + ",    thread:" + mi.threadType.toString());
            }
        }

        Iterator<String> keyIterator = subscribers.iteratorSnapshot();
        while(keyIterator.hasNext())
        {
            String key = keyIterator.next();
            LogUtils.e("eventType:" + key);
            CopyOnWriteArraySet<SubscriberListener> subscriberLists = subscribers.get(key);
            Iterator<SubscriberListener> subscriberListenerIterator = subscriberLists.iterator();
            while(subscriberListenerIterator.hasNext())
            {
                SubscriberListener subscriberListener = subscriberListenerIterator.next();
                LogUtils.e("    subscriberListener:" + subscriberListener.getClass().getName());
            }
        }

        LogUtils.e("subscribersList.size:" + subscribers.size() + " / " + subscribers.maxSize());
        LogUtils.e("subscriberMethod.size:" + methods.size() + " / " + methods.maxSize());
    }

    /*
    * 注册一个订阅者的监听器
    * 在写注解的时候，要直接完全扫描一个类，然后把所有的method全部一次性加载进来
    * 如果没有方法需要被加载，那么直接返回即可
    * */
    public static void registerSubscriber(final SubscriberListener listener, ConcurrentHashMap<String, MethodInfo> methodMap)
    {
        if (methodMap == null)
            return;

        /*先注册订阅者事件列表*/
        Set<String> eventTypes = methodMap.keySet();
        Iterator<String> iterator = eventTypes.iterator();
        String type = null;
        while(iterator.hasNext())
        {
            type = iterator.next();
            registerSubscriberListener(type, listener);
        }

        /*再注册事件方法列表*/
        registerSubscriberMethods(listener, methodMap);
    }

    private static void registerSubscriberListener(String eventType, SubscriberListener listener)
    {
        LruMemoryCache<String, CopyOnWriteArraySet<SubscriberListener>> list = subscriberList;

        CopyOnWriteArraySet<SubscriberListener> set = getListenerSetByKey(eventType, list);
        set.add(listener);
    }

    private static void registerSubscriberMethods(SubscriberListener listener, ConcurrentHashMap<String, MethodInfo> methodMap)
    {
        LruMemoryCache<SubscriberListener, ConcurrentHashMap<String, MethodInfo>> methodList = subscriberMethod;
        methodList.put(listener, methodMap);
    }

    /*
    * 在所有的事件中完全解除某一个订阅者的监听器
    * 用于当某一个类销毁以后，将不会再接收这些事件
    *
    * 当一个类加载时，中间不需要动态去销毁某一个事件，
    * 只有当类销毁的时候，才会去销毁，那么就只需要进行
    * 这个类里面所有的事件销毁即可，不需要单独销毁
    * */
    public static void unregisterListenerAll(SubscriberListener listener)
    {
        LruMemoryCache<String, CopyOnWriteArraySet<SubscriberListener>> list = subscriberList;
        Iterator<String> iterator = list.iteratorSnapshot();

        String key = null;
        CopyOnWriteArraySet<SubscriberListener> listenerList = null;
        while (iterator.hasNext())
        {
            key = iterator.next();
            listenerList = list.get(key);
            listenerList.remove(listener);
            if (listenerList.size() == 0)
                list.remove(key);
        }



        /*把这个类中的method注册全部删掉*/
        LruMemoryCache<SubscriberListener, ConcurrentHashMap<String, MethodInfo>> methods = subscriberMethod;
        methods.remove(listener);

        //showSubscriberListInfo();
    }

    /**
     * 事件触发时，将该事件广播给所有注册监听了该事件的类
     * */
    public static void onPostReceived(String eventType, EventData data)
    {
        postReceived(eventType, data, null);
    }

    private static void postReceived(String eventType, EventData data, Throwable e)
    {
        LruMemoryCache<String, CopyOnWriteArraySet<SubscriberListener>> list = subscriberList;
        CopyOnWriteArraySet<SubscriberListener> set = getListenerSetByKey(eventType, list);
        Iterator<SubscriberListener> iterator = set.iterator();
        SubscriberListener listener = null;
        while(iterator.hasNext())
        {
            listener = iterator.next();
            LogUtils.e(listener.getClass().getName());
            if (e == null)
                onPostReceived(listener, eventType, data);
            else
                listener.onEventException(eventType, data, e);
        }
    }

    public static void onExceptionPostReceived(String eventType, EventData data, Throwable e)
    {
        postReceived(eventType, data, e);
    }

    /*
    * 在当前缓存中查找，如果没有key的订阅者列表则创建
    * 最终把这个set返回
    * */
    private static CopyOnWriteArraySet<SubscriberListener> getListenerSetByKey(String key, LruMemoryCache<String, CopyOnWriteArraySet<SubscriberListener>> list)
    {
        CopyOnWriteArraySet<SubscriberListener> set = null;
        if (!list.containsKey(key))
        {
            set = new CopyOnWriteArraySet<SubscriberListener>();
            list.put(key, set);
        }
        else
        {
           set = list.get(key);
        }

        return set;
    }

    /*EventUtils里面代理生成监听器的方法，证明当前的数据结构可行*/
    private static void onPostReceived(SubscriberListener listener, String eventType, EventData data)
    {
        ConcurrentHashMap<String, MethodInfo> map = subscriberMethod.get(listener);
        MethodInfo mi = map.get(eventType);

        try
        {
            Class<?> clz = listener.getClass();
            Method method = clz.getMethod(mi.methodName, new Class<?>[]{EventData.class});
            LogUtils.e(method.getName());
            DynamicHandler handler = new DynamicHandler(listener);
            handler.addMethod("onEvent", method);
            EventListener l = (EventListener) Proxy.newProxyInstance(EventListener.class.getClassLoader(), new Class<?>[]{EventListener.class}, handler);
            EventTask et = new EventTask(eventType, l, data, mi.threadType);
            et.executeOnExecutor(executorService);
        }
        catch (Throwable e)
        {
            listener.onEventException(eventType, data, e);
        }
    }

    public static class MethodInfo
    {
        public MethodInfo(String methodName, TaskType threadType)
        {
            this.methodName = methodName;
            this.threadType = threadType;
        }

        String methodName;
        TaskType threadType;
    }
}