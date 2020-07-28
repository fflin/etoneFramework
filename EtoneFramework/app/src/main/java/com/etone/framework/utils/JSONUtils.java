package com.etone.framework.utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class JSONUtils 
{
	public static boolean isPrintException = false;
	
	/*get Long from jsonObject*/
	public static Long getLong(JSONObject jsonObject, String key, Long defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
		{
			return defaultValue;
		}
		
		try
		{
			return jsonObject.getLong(key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			return defaultValue;
		}
	}
	
	/*get Long from jsonData*/
	public static Long getLong(String jsonData, String key, Long defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getLong (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			return defaultValue;
		}
	}
	
	/*get long from jsonObject*/
	public static long getLong(JSONObject jsonObject, String key, long defaultValue)
	{
		return getLong(jsonObject, key, (Long)defaultValue);
	}
	
	/*getlong from jsonData*/
	public static long getLong(String jsonData, String key, long defaultValue)
	{
		return getLong (jsonData, key, (Long)defaultValue);
	}
	
	/*get Int from jsonObject*/
	public static Integer getInt(JSONObject jsonObject, String key, Integer defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
		{
			return defaultValue;
		}
		
		try
		{
			return jsonObject.getInt(key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			return defaultValue;
		}
	}
	
	/*get Int from jsonData*/
	public static Integer getInt(String jsonData, String key, Integer defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getInt (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			return defaultValue;
		}
	}
	
	/*get int from jsonObject*/
	public static int getInt(JSONObject jsonObject, String key, int defaultValue)
	{
		return getInt(jsonObject, key, (Integer)defaultValue);
	}
	
	/*get int from jsonData*/
	public static int getInt(String jsonData, String key, int defaultValue)
	{
		return getInt (jsonData, key, (Integer)defaultValue);
	}
	
	/*get Double from jsonObject*/
	public static Double getDouble(JSONObject jsonObject, String key, Double defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
		{
			return defaultValue;
		}
		
		try
		{
			return jsonObject.getDouble(key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			return defaultValue;
		}
	}
	
	/*get Double from jsonData*/
	public static Double getDouble(String jsonData, String key, Double defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getDouble (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			return defaultValue;
		}
	}
	
	/*get double from jsonObject*/
	public static double getDouble(JSONObject jsonObject, String key, double defaultValue)
	{
		return getDouble(jsonObject, key, (Double)defaultValue);
	}
	
	/*get double from jsonData*/
	public static double getDouble(String jsonData, String key, double defaultValue)
	{
		return getDouble (jsonData, key, (Double)defaultValue);
	}
	
	/*get String from jsonObject*/
	public static String getString (JSONObject jsonObject, String key, String defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
			return defaultValue;
		
		try
		{
			return jsonObject.getString(key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get String from jsonData*/
	public static String getString (String jsonData, String key, String defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getString (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get String array from jsonObject*/
	public static String[] getStringArray (JSONObject jsonObject, String key, String[] defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
			return defaultValue;
		
		try
		{
			JSONArray statusArray = jsonObject.getJSONArray(key);
			if (statusArray != null)
			{
				String[] value = new String[statusArray.length()];
				for (int i=0; i<statusArray.length(); i++)
				{
					value[i] = statusArray.getString(i);
				}
				return value;
			}
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
		
		return defaultValue;
	}
	
	/*get String array from jsonData*/
	public static String[] getStringArray (String jsonData, String key, String[] defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getStringArray(jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get JSONObject from jsonObject*/
	public static JSONObject getJSONObject (JSONObject jsonObject, String key, JSONObject defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
			return defaultValue;
		
		try
		{
			return jsonObject.getJSONObject(key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get JSONObject from jsonData*/
	public static JSONObject getJSONObject (String jsonData, String key, JSONObject defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getJSONObject (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
		}
		
		return defaultValue;
	}
	
	/*get JSONArray from jsonObject*/
	public static JSONArray getJSONArray (JSONObject jsonObject, String key, JSONArray defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
			return defaultValue;
		
		try
		{
			return jsonObject.getJSONArray (key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get JSONArray from jsonData*/
	public static JSONArray getJSONArray (String jsonData, String key, JSONArray defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getJSONArray (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get Boolean from jsonObject*/
	public static Boolean getBoolean (JSONObject jsonObject, String key, Boolean defaultValue)
	{
		if (jsonObject == null || StringUtils.isEmpty(key))
		{
			return defaultValue;
		}
		
		try
		{
			return jsonObject.getBoolean(key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get Boolean from jsonData*/
	public static boolean getBoolean(String jsonData, String key, boolean defaultValue)
	{
		if (StringUtils.isEmpty(jsonData))
			return defaultValue;
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getBoolean (jsonObject, key, defaultValue);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return defaultValue;
		}
	}
	
	/*get map from jsonObject*/
	public static Map<String, String> getMap(JSONObject jsonObject, String key)
	{
		return JSONUtils.parseKeyAndValueToMap(JSONUtils.getString(jsonObject, key, null));
	}
	
	/*get map from jsonData*/
	public static Map<String, String> getMap(String jsonData, String key)
	{
		if (jsonData == null)
			return null;
		if (jsonData.length() == 0)
			return new HashMap<String, String>();
		
		try
		{
			JSONObject jsonObject = new JSONObject(jsonData);
			return getMap (jsonObject, key);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return null;
		}
	}
	
	/*parse key-value pairs to map. ignore empty key, if getValue exception, pub empty value*/
	@SuppressWarnings("rawtypes")
	public static Map<String, String> parseKeyAndValueToMap(JSONObject sourceObj)
	{
		if (sourceObj == null)
			return null;
		
		Map<String, String> keyAndValueMap = new HashMap<String, String>();
		for (Iterator iter = sourceObj.keys(); iter.hasNext();)
		{
			String key = (String)iter.next();
			MapUtils.putMapNotNullKeyAndValue(keyAndValueMap, key, getString(sourceObj, key, ""));
		}
		
		return keyAndValueMap;
	}
	
	/*parse key-value pairs to map. ignore empty key, if getValue exception , put empty value*/
	public static Map<String, String> parseKeyAndValueToMap(String source)
	{
		if (StringUtils.isEmpty(source))
			return null;
		
		try
		{
			JSONObject jsonObject = new JSONObject(source);
			return parseKeyAndValueToMap(jsonObject);
		}
		catch (JSONException e)
		{
			if (isPrintException)
				e.printStackTrace();
			
			return null;
		}
	}
}
