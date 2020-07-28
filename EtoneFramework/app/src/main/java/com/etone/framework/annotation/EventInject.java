package com.etone.framework.annotation;

import com.etone.framework.event.TaskType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventInject
{
	String eventType();
	TaskType runThread() default TaskType.UI;
}