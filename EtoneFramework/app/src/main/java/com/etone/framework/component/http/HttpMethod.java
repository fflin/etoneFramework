package com.etone.framework.component.http;

public enum HttpMethod 
{
	GET("GET"),
	POST("POST"),
	PUT("PUT"),
	DELETE("DELETE");
	
	private String value;
	
	HttpMethod(String value)
	{
		this.value = value;
	}
	
	public String getValue()
	{
		return value;
	}
}
