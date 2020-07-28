package com.etone.framework.annotation.event;

import android.widget.AdapterView;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@EventBase(listenerType = AdapterView.OnItemClickListener.class,
			listenerSetter = "setOnItemClickListener",
			methodName = "onItemClick")
public @interface OnItemClick 
{
	int value();
	Class<?> clz();
	String method();
}
