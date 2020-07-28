package com.etone.framework.utils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class StringUtils 
{
	/*is null or its length is 0 or it is made by space*/
	public static boolean isBlank(String str)
	{
		return (str == null || str.trim().length()==0);
	}
	
	/*is null or its length is 0*/
	public static boolean isEmpty(String str)
	{
		return (str == null || str.length()==0);
	}
	
	/*null string to empty string*/
	public static String nullStrToEmpty(String str)
	{
		return (str == null ? "" : str);
	}
	
	/*encoded in utf-8*/
	public static String utf8Encode(String str)
	{
		if (!isEmpty(str) && str.getBytes().length != str.length())
		{
			try
			{
				return URLEncoder.encode(str, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				throw new RuntimeException("UnsupportedEncodingException occurred. ", e);
			}
		}
		
		return str;
	}
	
	/*encoded in utf-8, if exception, return defalutReturn */
	public static String utf8Encode (String str, String defaultReturn)
	{
		if (!isEmpty(str) && str.getBytes().length != str.length())
		{
			try
			{
				return URLEncoder.encode(str, "UTF-8");
			}
			catch (UnsupportedEncodingException e)
			{
				return defaultReturn;
			}
		}
		
		return str;
	}
	
	public static String getContent(String content, String start, String end, int index)
	{
		if (content == null || start == null || end == null)
			return null;
		
		int begin = content.indexOf(start);
		int done  = content.indexOf(end, begin+1);

		for (int i=0; i<index-1; i++)
			done = content.indexOf(end, done+1);
		
		if (begin == -1 || done == -1)
			return null;
		
		content = content.substring(begin, done+1);
		return content;
	}
}
