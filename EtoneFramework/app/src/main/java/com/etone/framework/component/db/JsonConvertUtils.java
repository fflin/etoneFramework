package com.etone.framework.component.db;

import com.etone.framework.utils.JSONUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public final class JsonConvertUtils
{
    /*如果一个数据结构在缓存中已存在，那么就不再去解析这个类*/
    private static final LinkedHashMap<Class<?>, Field[]> cache = new LinkedHashMap<>();

    private JsonConvertUtils()
    {

    }

    private static Field[] getModelFields(Class<?> clz)
    {
        /*有就直接拿来用*/
        if (cache.containsKey(clz))
            return cache.get(clz);

        /*没有的话，要把fields里面的数据进行一次过滤*/
        Field[] fields = clz.getDeclaredFields();
        ArrayList<Field> list = new ArrayList<>();
        Field field;
        for (int i = 0; i < fields.length; i++)
        {
            field = fields[i];
            if (field.getName().contains("$"))
                continue;
            field.setAccessible(true);
            list.add(field);
        }

        int length = list.size();
        fields = new Field[length];
        for (int i = 0; i < length; i++)
        {
            fields[i] = list.get(i);
        }

        cache.put(clz, fields);

        return fields;
    }

    public static ArrayList<?> fromJsonArray(String json, String key, Class<?> modelType)
    {
        ArrayList<Object> list = new ArrayList<>();
        String[] res = JSONUtils.getStringArray(json, key, new String[]{});
        for (int i = 0; i < res.length; i++)
        {
            Object obj = fromJson(res[i], modelType);
            list.add(obj);
        }

        return list;
    }

    public static JSONArray toJsonArray(ArrayList<?> obj)
    {
        JSONArray array = new JSONArray();
        for (int i = 0; i < obj.size(); i++)
        {
            array.put(toJson(obj.get(i)));
        }

        return array;
    }

    /*针对model，或者已经被new出来的对象，json里面的值会覆盖掉之前的值*/
    public static void fromJson(String json, Object obj)
    {
        try
        {
            setJsonValues(json, obj, obj.getClass());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /*任意类都可以，这里会反射出来一个对象并返回*/
    public static Object fromJson(String json, Class<?> modelType)
    {
        Object obj = null;
        try
        {
            Constructor constructor = modelType.getDeclaredConstructor();
            obj = constructor.newInstance();
            setJsonValues(json, obj, modelType);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return obj;
    }

    private static void setJsonValues(String json, Object obj, Class<?> modelType) throws Exception
    {
        Field[] fields = getModelFields(modelType);
        for (int i = 0; i < fields.length; i++)
        {
            setObjectValue(json, obj, fields[i]);
        }
    }

    public static JSONObject toJson(Object obj)
    {
        JSONObject jsonObject = null;
        Class<?> modelType = obj.getClass();
        try
        {
            jsonObject = new JSONObject();
            Field[] fields = getModelFields(modelType);

            for (int i = 0; i < fields.length; i++)
            {
                setJsonObjectValue(jsonObject, obj, fields[i]);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return jsonObject;
    }

    private static void setJsonObjectValue(JSONObject jsonObject, Object object, Field field) throws Exception
    {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        Object value = field.get(object);

        if (fieldType.equals(Byte.class) || fieldType.equals(byte.class) || fieldType.equals(Integer.class) || fieldType.equals(int.class) || fieldType.equals(Short.class) || fieldType.equals(short.class))
        {
            jsonObject.put(fieldName, (int)value);
        }
        else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
        {
            jsonObject.put(fieldName, (boolean)value);
        }
        else if (fieldType.equals(Long.class) || fieldType.equals(long.class))
        {
            jsonObject.put(fieldName, (long)value);
        }
        else if (fieldType.equals(Float.class) || fieldType.equals(float.class) || fieldType.equals(Double.class) || fieldType.equals(double.class))
        {
            jsonObject.put(fieldName, (double)value);
        }
        else if (fieldType.equals(Character.class) || fieldType.equals(char.class) || fieldType.equals(String.class))
        {
            jsonObject.put(fieldName, value);
        }
        //Json暂时不支持byte[]类型的数据传输
        /*else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
            byte[] b;
            value = cursor.getBlob(index);
        }*/
        else
        {
            jsonObject.put(fieldName, value);
        }
    }

    private static void setObjectValue(String json, Object object, Field field) throws Exception
    {
        Class<?> fieldType = field.getType();
        String fieldName = field.getName();
        Object value;

        if (fieldType.equals(Byte.class) || fieldType.equals(byte.class) || fieldType.equals(Integer.class) || fieldType.equals(int.class) || fieldType.equals(Short.class) || fieldType.equals(short.class))
        {
            value = JSONUtils.getInt(json, fieldName, 0);
        }
        else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
        {
            value = JSONUtils.getBoolean(json, fieldName, false);
        }
        else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
            value = JSONUtils.getLong(json, fieldName, 0L);
        }
        else if (fieldType.equals(Float.class) || fieldType.equals(float.class) || fieldType.equals(Double.class) || fieldType.equals(double.class)) {
            value = JSONUtils.getDouble(json, fieldName, 0.0D);
        }
        else if (fieldType.equals(Character.class) || fieldType.equals(char.class) || fieldType.equals(String.class))
        {
            value = JSONUtils.getString(json, fieldName, "");
        }
        //Json暂时不支持byte[]类型的数据传输
        /*else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
            byte[] b;
            value = cursor.getBlob(index);
        }*/
        else
        {
            value = null;
        }

        field.set(object, value);
    }
}
