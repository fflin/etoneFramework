package com.etone.framework.annotation;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.etone.framework.annotation.event.EventBase;
import com.etone.framework.cache.LruMemoryCache;
import com.etone.framework.event.EventBus;
import com.etone.framework.event.EventBus.MethodInfo;
import com.etone.framework.event.SubscriberListener;
import com.etone.framework.base.BaseArrayListAdapter.BaseHolder;
import com.etone.framework.utils.LogUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

public class InjectUtils
{
	private static final int MEMORY_CACHE_SIZE = 1024 * 1024;
	private static final LruMemoryCache<String, Object> cache = new LruMemoryCache<>(MEMORY_CACHE_SIZE);

	/*
	* 这个方法只用在Adapter上面
	* 如果有注解，那么返回layout的值，否则返回0
	* */
	public static int getAdapterInjectLayout(Object handler)
	{
		int layout = 0;
		Class<?> handlerType = handler.getClass();

		AdapterView adapterView = handlerType.getAnnotation(AdapterView.class);
		if (adapterView != null)
			layout = adapterView.value();

		return layout;
	}

	public static BaseHolder injectAdapterGetView(Class<?> handlerType, View parent)
	{
		Object holder = null;

		Class<?>[] classes = handlerType.getDeclaredClasses();
		for (int i=0; i<classes.length; i++)
		{
			Class<?> clz = classes[i];
			if (BaseHolder.class.isAssignableFrom(clz))
			{
				try
				{
					holder = clz.newInstance();
					injectFields(holder, new ViewFinder(parent), parent);
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}

				break;
			}
		}

		return (BaseHolder) holder;
	}

	/*该方法只注册事件，不涉及任何UI的内容,例如contraller则可以直接在这里使用*/
	public static void injectOnlyEvent(SubscriberListener listener)
	{
		injectEvent(listener.getClass(), listener);
	}

	/*
	* 注解一个页面容器，默认handler就是SubscriberListener
	* 若handler不是activity且viw为空，则抛出空指针异常
	* 若view不为空，则优先在view上面findView,否则才去看activity
	* 并且该方法暂不支持注解contentView
	* 一般情况下默认handler作为事件继承者，不继承SubscriberListener的
	* 无法接收事件
	* */
	public static void injectContainer(Object handler, View view)
	{
		if (view == null && !(handler instanceof Activity))
			throw new NullPointerException();

		Class<?> handlerType = handler.getClass();

		ViewFinder viewFinder = null;
		if (view == null)
			viewFinder = new ViewFinder((Activity) handler);
		else
			viewFinder = new ViewFinder(view);

		injectFields(handler, viewFinder, view);

		if (handler instanceof SubscriberListener)
			injectEvent(handlerType, (SubscriberListener) handler);
	}

	/*
	* 在这个方法里面，默认认为这个Activity就是事件接收者，一般情况下也是这样写
	* 并且，在注解事件的时候，也必须要继承SubscriberListener，否则无法接收事件
	* 所以，只需要传一个参数即可
	* */
	public static void injectActivity(Activity activity)
	{
		if (activity == null)
			throw new NullPointerException();

		Class<?> handlerType = activity.getClass();

		injectContentView(activity);
		injectFields(activity, new ViewFinder(activity), null);

		if (activity instanceof SubscriberListener)
			injectEvent(handlerType, (SubscriberListener) activity);
		else
			LogUtils.e("this is not subscriberListener!!!");
	}

	/*
	* 在这个方法里面，默认认为这个Fragment就是事件接收者，一般情况下也是这样写
	* 并且，在注解事件的时候，也必须要继承SubscriberListener，否则无法接收事件
	* 所以，只需要传一个参数即可
	* */
	public static View injectFragment(Fragment fragment, LayoutInflater inflater, ViewGroup container)
	{
		if (fragment == null || inflater == null || container == null)
			throw new NullPointerException();

		Class<?> handlerType = fragment.getClass();

		View view = injectFragmentView(fragment, inflater, container);
		injectFields(fragment, new ViewFinder(view), view);

		return view;
	}

	private static View injectFragmentView(Fragment fragment, LayoutInflater inflater, ViewGroup container)
	{
		Class<?> handlerType = fragment.getClass();
		FragmentView fragmentView = handlerType.getAnnotation(FragmentView.class);
		if (fragmentView != null)
		{
			View v = inflater.inflate(fragmentView.value(), container, false);
			return v;
		}

		return null;
	}

	private static void injectContentView(Object handler)
	{
		Class<?> handlerType = handler.getClass();
		LogUtils.e(handlerType.getName());
		LayoutInject contentView = handlerType.getAnnotation(LayoutInject.class);
		if (contentView != null)
		{
			LogUtils.e("contentView.id:" + contentView.value());
			try
			{
				Method setContentViewMethod = handlerType.getMethod ("setContentView", int.class);
				setContentViewMethod.invoke(handler, contentView.value());
			}
			catch (Throwable e)
			{
				e.printStackTrace();
			}
		}
	}


	private static void injectFields(Object handler, ViewFinder viewFinder, View parentView)
	{
		Class<?> handlerType = handler.getClass();

		Field[] fields = handlerType.getDeclaredFields();

		if (fields != null)
		{
			try
			{
				Field field = null;
				Annotation annotation = null;
				Annotation annotation1 = null;

				for (int i = 0; i < fields.length; i++)
				{
					field = fields[i];
					LogUtils.e(field.getName());
					Annotation[] annotations = field.getAnnotations();

					for (int j = 0; j < annotations.length; j++)
					{
						annotation = annotations[j];
						annotation1 = annotation.annotationType().getAnnotation(EventBase.class);
						if (annotation instanceof ViewInject)
						{
							injectViews(handler, viewFinder, parentView, (ViewInject)annotation, field);
						}
						else if (annotation instanceof ResInject)
						{
							injectRes(handler, viewFinder, (ResInject)annotation, field);
						}
						else if (annotation1 != null)
						{
							injectListener(handler, viewFinder, parentView, annotation, (EventBase) annotation1);
						}
					}
				}
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		else
		{
			LogUtils.e("fields is null!!!");
		}
	}

	public static void injectEvent(Class<?> handlerType, SubscriberListener listener)
	{
		if (listener == null)
			return;

		LogUtils.e("inject event:" + listener.getClass().getName());
		ConcurrentHashMap<String, MethodInfo> methodMap = null;
		Method method = null;
		Annotation annotation = null;
		EventInject event = null;
		MethodInfo methodInfo = null;
		String eventType = null;

		Method[] methods = handlerType.getMethods();

		for (int i=0; i<methods.length; i++)
		{
			method = methods[i];

			Annotation[] annotations = method.getAnnotations();
			for (int j=0; j<annotations.length; j++)
			{
				annotation = annotations[j];
				LogUtils.e(annotation.toString());
				if (annotation instanceof  EventInject)
				{
					event = (EventInject) annotation;
					eventType = event.eventType();
					methodInfo = new MethodInfo(method.getName(), event.runThread());

					if (methodMap == null)
						methodMap = new ConcurrentHashMap<String, MethodInfo>();
					methodMap.put(eventType, methodInfo);
					LogUtils.e("eventType:" + eventType + ", methodName:" + method.getName());
				}
			}
		}

		/*最后统一注册*/
		EventBus.registerSubscriber(listener, methodMap);
	}

	private static void injectViews(Object handler, ViewFinder viewFinder, View parentView, ViewInject viewInject, Field field) throws IllegalAccessException
	{
		int viewId = viewInject.value();
		View view = viewFinder.findViewById (viewId, parentView);
		if (view == null)
			throw new NullPointerException();
		field.setAccessible(true);
		field.set(handler, view);
	}

	private static void injectRes(Object handler, ViewFinder viewFinder, ResInject resInject, Field field)  throws IllegalAccessException
	{
		Object res = ResLoader.loadRes(resInject.type(), viewFinder.getContext(), resInject.id());
		if (res == null)
			throw new NullPointerException();
		field.setAccessible(true);
		field.set(handler, res);
	}

	/*
	* 注意，Activity、Fragment 不能作为Listener的实现类，
	* 因为无法实例化他们，并且也不希望Event和UI改变的内容在代码上放在一起
	* */
	private static void injectListener(Object handler, ViewFinder viewFinder, View parentView, Annotation annotation, EventBase eventBase) throws Exception
	{
		/*监听器设置部分所需参数*/
		Class<? extends Annotation> annotationType = annotation.annotationType();
		String listenerSetter = eventBase.listenerSetter();
		Class<?> listenerType = eventBase.listenerType();
		String methodName = eventBase.methodName();

		/*具体某一个监听器所需要的参数*/
		Method aMethod = annotationType.getDeclaredMethod ("value");
		Object[] objArray = new Object[]{};
		int viewId = (Integer) aMethod.invoke(annotation, objArray);
		aMethod = annotationType.getDeclaredMethod ("clz");
		Class<?> aClz = (Class<?>) aMethod.invoke(annotation, objArray);
		aMethod = annotationType.getDeclaredMethod ("method");
		String mName = (String) aMethod.invoke (annotation, objArray);

		/*
		* 执行事件的类如果没有实例化过，那么将它插入到缓存中, 否则说明可以直接用
		* 由于缓存始终持有这些实例，因此它们的生命周期与APP相同，不会销毁掉，
		* 并且不会重复
		* */
		String aClzName = aClz.getName();
		if (!cache.containsKey(aClzName))
		{
			cache.put(aClzName, aClz.newInstance());
		}
		/*获取这个示例，在下面代理它*/
		Object obj = cache.get(aClzName);

		/*
		* 这里由于不知道每个事件的具体参数，所以不能直接用方法名称
		* 来反射具体方法，只能循环去找，已经试验过了getDeclaredMethod()
		* 这个方法必须要接方法名，所以只能循环
		* */
		Method ms[] = aClz.getDeclaredMethods();
		Method m = null;
		for (int i=0; i<ms.length; i++)
		{
			m = ms[i];
			if (m.getName().equals(mName))
			{
				//通过代理的方式，将该方法和listener接口对应的方法进行定向
				DynamicHandler dynamicHandler = new DynamicHandler(obj);
				dynamicHandler.addMethod(methodName, m);
				//实例化一个listener
				Object listener = Proxy.newProxyInstance(listenerType.getClassLoader(), new Class<?>[]{listenerType}, dynamicHandler);

				//通过反射的方式，给该空间添加上所需要的监听事件
				View view = viewFinder.findViewById(viewId, parentView);
				Method setEventListenerMethod = view.getClass().getMethod(listenerSetter, listenerType);
				setEventListenerMethod.invoke(view, listener);

				break;
			}
		}
	}
}
