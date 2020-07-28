package com.etone.framework.component.http;

public class AuthException extends Exception
{
	private static final long serialVersionUID = 3422893167078966831L;
	private String url;
	public AuthException(String url)
	{
		this.url = url;
	}
	
	@Override
	public String getMessage()
	{
		return "Url=" + url + "\nAuth Error:\n" + super.getMessage();
	}
}
