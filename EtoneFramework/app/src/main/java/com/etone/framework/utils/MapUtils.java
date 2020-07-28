package com.etone.framework.utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class MapUtils 
{
	/*default separator between key and value*/
	public static final String DEFAULT_KEY_AND_VALUE_SEPARATOR      = ":";
	/*default separator between key-value pairs */
	public static final String DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR = ",";
	
	/*is null or its size is 0*/
	public static <K, V> boolean isEmpty(Map<K, V> sourceMap)
	{
		return (sourceMap == null || sourceMap.size() == 0);
	}
	
	/*add key-value pair to map, and key need not null or empty*/
	public static boolean putMapNotEmptyKey (Map<String, String> map, String key, String value)
	{
		if (map == null || StringUtils.isEmpty(key))
			return false;
		map.put(key, value);
		return true;
	}
	
	/*add key-value pair to map, both key and value need not null or empty*/
	public static boolean putMapNotEmptyKeyAndValue (Map<String, String> map, String key, String value)
	{
		if (map == null || StringUtils.isEmpty(key) || StringUtils.isEmpty(value))
			return false;
		map.put (key, value);
		return true;
	}
	
	/*add key-value pair to map, key need not null or empty*/
	public static boolean putMapNotEmptyKeyAndValue (Map<String, String> map, String key, String value, String defaultValue)
	{
		if (map == null || StringUtils.isEmpty(key))
			return false;
		
		map.put(key, StringUtils.isEmpty(value)?defaultValue:value);
		return true;
	}
	
	/*add key-value pair to map, key need not null*/
	public static <K, V> boolean putMapNotNullKey (Map<K, V> map, K key, V value)
	{
		if (map == null || key == null)
			return false;
		
		map.put(key, value);
		return true;
	}
	
	/*add key-value pair to map, both key and value need not null*/
	public static <K, V> boolean putMapNotNullKeyAndValue(Map<K, V> map, K key, V value)
	{
		if (map == null || key == null || value == null)
			return false;
		
		map.put(key, value);
		return true;
	}
	
	/*get key by value, match the first entry front to back*/
	public static <K, V> K getKeyByValue(Map<K, V> map, V value)
	{
		if (isEmpty(map))
			return null;
		
		for (Entry<K, V> entry : map.entrySet())
		{
			if (ObjectUtils.isEquals(entry.getValue(), value))
				return entry.getKey();
		}
		
		return null;
	}
	
	/*parse key-value pairs to map, ignore empty key*/
	public static Map<String, String> parseKeyAndValueToMap(String source, String keyAndValueSeparator, String keyAndValuePairSeparator, boolean ignoreSpace)
	{
		if (StringUtils.isEmpty(source))
			return null;
		if (StringUtils.isEmpty(keyAndValueSeparator))
			keyAndValueSeparator = DEFAULT_KEY_AND_VALUE_SEPARATOR;
		if (StringUtils.isEmpty(keyAndValuePairSeparator))
			keyAndValuePairSeparator = DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR;
		
		Map<String, String> keyAndValueMap = new HashMap<String, String>();
		String[] keyAndValueArray = source.split(keyAndValuePairSeparator);
		if (keyAndValueArray == null)
			return null;
		
		int seperator;
		for (String valueEntity : keyAndValueArray)
		{
			if (!StringUtils.isEmpty(valueEntity))
			{
				seperator = valueEntity.indexOf(keyAndValueSeparator);
				if (seperator != -1)
				{
					if (ignoreSpace)
						MapUtils.putMapNotEmptyKey(keyAndValueMap, valueEntity.substring(0, seperator).trim(), valueEntity.substring(seperator +1).trim());
					else
						MapUtils.putMapNotEmptyKey(keyAndValueMap, valueEntity.substring(0, seperator), valueEntity.substring(seperator +1));
				}
			}
		}
		return keyAndValueMap;
	}
	
	/*parse key-value pairs to map, ignore empty key*/
	public static Map<String, String> parseKeyAndValueToMap(String source, boolean ignoreSpace)
	{
		return parseKeyAndValueToMap(source, DEFAULT_KEY_AND_VALUE_SEPARATOR, DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR, ignoreSpace);
	}
	
	/*parse key-value pairs to map, ignore empty key, ignore space at the begging or end of key and value*/
	public static Map<String, String> parseKeyAndValueToMap(String source)
	{
		return parseKeyAndValueToMap(source, DEFAULT_KEY_AND_VALUE_SEPARATOR, DEFAULT_KEY_AND_VALUE_PAIR_SEPARATOR, true);
	}
	
	/*join map*/
	public static String toJson(Map<String, String> map)
	{
		if (map == null | map.size() == 0)
			return null;
		
		StringBuilder paras = new StringBuilder();
		paras.append("{");
		Iterator<Entry<String, String>> ite = map.entrySet().iterator();
		while (ite.hasNext())
		{
			Map.Entry<String, String> entry = (Map.Entry<String, String>)ite.next();
			String value = entry.getValue();
			paras.append("\"").append(entry.getKey()).append("\":");
			if (value != null)
			{
				if (value.startsWith("[") && value.lastIndexOf("]") == value.length() - 1)    //说明是一个json数组
					paras.append(value);
				else
					paras.append("\"").append(value).append("\"");
			}
			if (ite.hasNext())
			{
				paras.append(",");
			}
		}
		paras.append("}");
		return paras.toString();
	}
}
