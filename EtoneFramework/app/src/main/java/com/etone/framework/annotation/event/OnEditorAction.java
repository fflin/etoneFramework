package com.etone.framework.annotation.event;

import android.widget.TextView.OnEditorActionListener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@EventBase(listenerType = OnEditorActionListener.class,
			listenerSetter = "setOnEditorActionListener", 
			methodName = "onEditorAction")
public @interface OnEditorAction 
{
	int value();
	Class<?> clz();
	String method();
}