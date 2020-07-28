package com.etone.framework.annotation.event;

import android.view.View;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@EventBase(listenerType = View.OnTouchListener.class,
			listenerSetter = "setOnTouchListener", 
			methodName = "onTouch")
public @interface OnTouch 
{
	int value();
	Class<?> clz();
	String method();
}