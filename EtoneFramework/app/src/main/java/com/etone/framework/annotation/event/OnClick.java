package com.etone.framework.annotation.event;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@EventBase(listenerType = View.OnClickListener.class,
			listenerSetter = "setOnClickListener", 
			methodName = "onClick")
public @interface OnClick 
{
	int value();
	Class<?> clz();
	String method();
}