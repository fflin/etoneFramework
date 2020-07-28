package com.etone.framework.utils;

public class ObjectUtils 
{
	/*compare two object*/
	public static boolean isEquals(Object actual, Object expected)
	{
		return actual == expected || (actual == null ? expected == null: actual.equals(expected));
	}
	
	/*convert long array to Long array*/
	public static Long[] transformLongArray(long[] source)
	{
		Long[] destin = new Long[source.length];
		for (int i=0; i<source.length; i++)
			destin[i] = source[i];
		
		return destin;
	}
	
	/*convert Long array to long array*/
	public static long[] transformLongArray(Long[] source)
	{
		long[] destin = new long[source.length];
		for (int i=0; i<source.length; i++)
			destin[i] = source[i];
		
		return destin;
	}
	
	/*convert int array to Integer array*/
	public static Integer[] transformIntArray(int[] source)
	{
		Integer[] destin = new Integer[source.length];
		for (int i=0; i<source.length; i++)
			destin[i] = source[i];
		
		return destin;
	}
	
	/*convert Integer array to int array*/
	public static int[] transformLongArray(Integer[] source)
	{
		int[] destin = new int[source.length];
		for (int i=0; i<source.length; i++)
			destin[i] = source[i];
		
		return destin;
	}
	
	/*compare two object*/
	@SuppressWarnings({ "unchecked" })
	public static <V> int compare (V v1, V v2)
	{
		return v1 == null ? (v2 == null ? 0 : -1) : (v2 == null ? 1 : ((Comparable<V>)v1).compareTo(v2));
	}
}
