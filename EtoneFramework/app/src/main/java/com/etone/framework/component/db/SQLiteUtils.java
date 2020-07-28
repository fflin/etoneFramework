package com.etone.framework.component.db;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.etone.framework.annotation.Database;
import com.etone.framework.utils.IOUtils;
import com.etone.framework.utils.LogUtils;
import com.etone.framework.utils.StringUtils;

public final class SQLiteUtils
{
    public static int DATABASE_VERSION = 1;
    /*线程安全的原子*/
    private AtomicInteger mOpenCounter = new AtomicInteger();

    private SQLiteOpenHelper mDatabaseHelper;
    private SQLiteDatabase mDatabase;
    private String packageName;

    /*这里存储所有需要加载的类，程序只加载一次即可*/
    private HashMap<String, Table> cache;

    /*这个表里存储每一个model对应的数据库名称,这个只有一个实例，因为它要查所有项目里面的类*/
    private static final HashMap<Class<?>, String> dbNameCache = new HashMap<>();

    protected enum SQLiteType {
        INTEGER, REAL, TEXT, BLOB
    }

    protected static final HashMap<Class<?>, SQLiteType> TYPE_MAP = new HashMap<Class<?>, SQLiteType>() {
        {
            put(byte.class, SQLiteType.INTEGER);
            put(short.class, SQLiteType.INTEGER);
            put(int.class, SQLiteType.INTEGER);
            put(long.class, SQLiteType.INTEGER);
            put(float.class, SQLiteType.REAL);
            put(double.class, SQLiteType.REAL);
            put(boolean.class, SQLiteType.INTEGER);
            put(char.class, SQLiteType.TEXT);
            put(byte[].class, SQLiteType.BLOB);
            put(Byte.class, SQLiteType.INTEGER);
            put(Short.class, SQLiteType.INTEGER);
            put(Integer.class, SQLiteType.INTEGER);
            put(Long.class, SQLiteType.INTEGER);
            put(Float.class, SQLiteType.REAL);
            put(Double.class, SQLiteType.REAL);
            put(Boolean.class, SQLiteType.INTEGER);
            put(Character.class, SQLiteType.TEXT);
            put(String.class, SQLiteType.TEXT);
            put(String[].class, SQLiteType.TEXT);
            put(Byte[].class, SQLiteType.BLOB);
        }
    };

    private static final LinkedHashMap<String, SQLiteUtils> instanceCache = new LinkedHashMap<>();

    /*初始化数据库，并返回该数据库的操作句柄*/
    public static synchronized SQLiteUtils initDatabase(Context context, String dbName, Class<? extends Model>[] clazz) {
        if (instanceCache.containsKey(dbName))
            return instanceCache.get(dbName);

        SQLiteUtils instance = new SQLiteUtils();
        instance.init(context, dbName, clazz);
        instanceCache.put(dbName, instance);

        return instance;
    }

    public static final synchronized void terminateDatabase()
    {
        if (instanceCache == null || instanceCache.size() == 0)
            return;
        Set set = instanceCache.keySet();
        Iterator<String> iterator = set.iterator();
        SQLiteUtils instance;
        String key;
        while(iterator.hasNext())
        {
            key = iterator.next();
            instance = instanceCache.get(key);
            instance.mDatabase.close();
        }
    }

    /*直接返回数据库的操作句柄，如果没有这个数据库，则返回null*/
    public static synchronized SQLiteUtils getDatabase(String dbName) {
        if (instanceCache.containsKey(dbName))
            return instanceCache.get(dbName);

        return null;
    }

    private SQLiteUtils() {
    }

    /*程序启动的时候，需要先去建表建库等*/
    public synchronized void init(Context context, String dbName, Class<? extends Model>[] clazz)
    {
        packageName = context.getPackageName();
        mDatabaseHelper = new DBHelper(context, dbName);
        createVersionTable();
        int length = clazz.length;
        HashMap<String, Table> cache = this.cache = new HashMap<>(length);
        Table table;
        Class<? extends Model> clz;
        try {
            for (int i = 0; i < length; i++) {
                clz = clazz[i];
                if (!Model.class.isAssignableFrom(clz))
                    continue;
                table = new Table(this, clz);
                int v = clz.newInstance().getVersion();

                /*
                * 1.把表名和版本号插入到数据库中，如果有的话，则返回数据库的版本号，否则返回当前版本号
                * 2.如果数据库中的版本号比类的版本号小，则执行更新表结构的操作
                * */
                if (compareTableVersion(table.name, v))
                    updateTable(table);

                cache.put(table.name, table);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createVersionTable()
    {
        String sql = "CREATE TABLE IF NOT EXISTS t_version (id INTEGER PRIMARY KEY NOT NULL, name VARCHAR(256), version INTEGER)";
        LogUtils.e(sql);
        queryFactory(sql);
    }

    /*
    * 表的当前版本是否小于Model的版本, true:小于(需要更新)，false:大于等于(不需要更新)
    * 这个方法里面会把最新的版本号更新到数据库中
    * */
    private boolean compareTableVersion(String tableName, int version)
    {
        boolean res = false;

        String sql = "select * from t_version where name='" + tableName + "' limit 1";
        ArrayList<Version> versionList = (ArrayList<Version>) rawSelect(sql, Version.class);
        if (versionList.size() == 0)
        {
            sql = "insert into t_version (`name`, `version`) values ('" + tableName + "', '" + version + "')";
            rawQuery(sql);
        }
        else
        {
            Version v = versionList.get(0);
            res = v.version < version;
            if (res)
                rawQuery("update t_version set `version`='" + version + "' where `name`='" + tableName + "'");
        }

        return res;
    }

    private void updateTable(Table table)
    {
        String[] columnNames = getColumnNames(table.name);
        Field[] fields = table.fields;

        /*以最新的model为主体，less是库比model少的字段，more是库比model多的字段*/
        ArrayList<Field> lessColumns = new ArrayList<>();

        Field f;
        String field;
        String column;
        boolean has;

        for (int i=0; i<fields.length; i++)
        {
            field = fields[i].getName();
            has = false;
            for (int j=0; j<columnNames.length; j++)
            {
                column = columnNames[j];
                if (field.equals(column))
                {
                    has = true;
                    break;
                }
            }

            if (!has)
                lessColumns.add(fields[i]);
        }

        StringBuilder sb = new StringBuilder();
        String key;
        Class<?> type;
        if (lessColumns.size() > 0) {
            for (int i = 0; i < lessColumns.size(); i++) {
                f = lessColumns.get(i);
                key = f.getName();
                type = f.getType();
                if (key.equals("mId"))
                    continue;
                sb.setLength(0);
                sb.append("ALTER TABLE `").append(table.name).append("` ADD COLUMN `").append(key).append("` ").append(SQLiteUtils.TYPE_MAP.get(type).toString()).append(";");
                LogUtils.e(sb.toString());
                queryFactory(sb.toString());
            }
        } else {
            LogUtils.e("没有少掉的列");
        }
    }

    public static class Version extends Model
    {
        public int id;
        public String name;
        public int version;

        public Version()
        {
            super();
        }

        @Override
        public int getVersion()
        {
            return 0;
        }
    }

    public synchronized ArrayList<?> modelSelect(Class<? extends Model> clz, String where)
    {
        Table t = findTableByClass(clz);
        return t.select(where);
    }

    /*这个方法适用于无返回结果的自定义sql*/
    public synchronized void rawQuery(String sql)
    {
        queryFactory(sql);
    }

    protected synchronized static void putDbNameCache(Class<?> clz)
    {
        if (dbNameCache.containsKey(clz))
            return;
        Database database = clz.getAnnotation(Database.class);
        LogUtils.e("dbName:" + database);
        LogUtils.e("modelName:" + clz.getName());

        /*如果要是有的类不需要数据库，那么可以不用这样*/
        if (database == null)
            return;
        //throw new NullPointerException("no such database. have you declared the annotation on the model?");

        dbNameCache.put(clz, database.value());
    }

    protected synchronized static String getDbNameCache(Class<?> clz)
    {
        putDbNameCache(clz);
        if (dbNameCache.containsKey(clz))
            return dbNameCache.get(clz);

        return null;
    }

    protected synchronized void queryFactory(String sql)
    {
        try
        {
            SQLiteDatabase db = this.openDatabase();
            LogUtils.e(sql);
            db.execSQL(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            this.closeDatabase();
        }
    }

    /*这个方法适用于自定义sql, 但是前提是必须要有一个Model来支持,这个Model不一定需要对应一张表,因此速度比较慢*/
    public synchronized ArrayList<?> rawSelect(String sql, Class<? extends Model> model) {
        LinkedHashSet<Field> fieldMap = new LinkedHashSet<>();
        String fieldName;
        Field[] fieldsRes = null;
        try {
            Field[] fields = model.getFields();
            for (int i = 0; i < fields.length; i++) {
                fieldName = fields[i].getName();
                if (fieldName.contains("$"))
                    continue;

                fields[i].setAccessible(true);
                fieldMap.add(fields[i]);
            }
            fieldsRes = fieldMap.toArray(new Field[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return selectFactory(sql, fieldsRes, model);
    }

    private String[] getColumnNames(String tableName)
    {
        Cursor cursor = null;
        String[] res = null;
        SQLiteDatabase db = this.openDatabase();

        try
        {
            cursor = db.rawQuery("select * from " + tableName, null);
            res = cursor.getColumnNames();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            IOUtils.closeQuietly(cursor);
            this.closeDatabase();
        }

        return res;
    }

    public synchronized int countQuery(String sql)
    {
        Cursor cursor = null;
        int count = 0;
        try
        {
            SQLiteDatabase db = this.openDatabase();
            cursor = db.rawQuery(sql, null);
            cursor.moveToNext();
            int index = cursor.getColumnIndex("num");
            count = cursor.getInt(index);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
                cursor.close();

            this.closeDatabase();
        }

        return count;
    }

    protected synchronized ArrayList<?> selectFactory(String sql, Field[] fields, Class<? extends Model> model)
    {
        ArrayList<Object> list = new ArrayList<>();
        Cursor cursor = null;
        try
        {
            SQLiteDatabase db = this.openDatabase();
            cursor = db.rawQuery(sql, null);
            Constructor<? extends Model> constructor = model.getDeclaredConstructor();
            Object obj;
            Field field;
            String fieldName;
            while (cursor.moveToNext())
            {
                obj = constructor.newInstance();
                for (int i=0; i<fields.length; i++)
                {
                    field = fields[i];
                    fieldName = field.getName();
                    if (fieldName.contains("$"))
                        continue;

                    setObjectValue(cursor, obj, field, fieldName);
                }

                list.add(obj);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (cursor != null)
                cursor.close();

            this.closeDatabase();
        }

        return list;
    }

    private static synchronized void setObjectValue(Cursor cursor, Object object, Field field, String fieldName) throws Exception
    {
        Class<?> fieldType = field.getType();
        Object value;
        int index = cursor.getColumnIndex(fieldName);
        if (index == -1)
            return;

        if (fieldType.equals(Byte.class) || fieldType.equals(byte.class) || fieldType.equals(Integer.class) || fieldType.equals(int.class))
        {
            value = cursor.getInt(index);
        }
        else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class))
        {
            value = cursor.getInt(index) == 1 ? true : false;
        }
        else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
            value = cursor.getShort(index);
        }
        else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
            value = cursor.getLong(index);
        }
        else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
            value = cursor.getFloat(index);
        }
        else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
            value = cursor.getDouble(index);
        }
        else if (fieldType.equals(Character.class) || fieldType.equals(char.class) || fieldType.equals(String.class)) {
            value = cursor.getString(index);
        }
        else if (fieldType.equals(String[].class))
        {
            String res =cursor.getString(index).trim();
            if (res.contains("HXH"))
                value = res.split("HXH");
            else if (StringUtils.isEmpty(res))
                value = new String[]{};
            else
                value = new String[]{res};
        }
        else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
            value = cursor.getBlob(index);
        }
        else
        {
            value = null;
        }

        field.set(object, value);
    }

    public synchronized Table findTableByClass(Class<? extends Model> clz)
    {
        return findTableByName(clz.getSimpleName());
    }

    public synchronized Table findTableByName(String tableName)
    {
        return cache.get(Table.TABLE_DEFAULT_PERFIX + tableName);
    }

    private boolean mainTmpDirSet = false;
    protected synchronized SQLiteDatabase openDatabase()
    {
        /*if (mOpenCounter.incrementAndGet() == 1)
            mDatabase = mDatabaseHelper.getWritableDatabase();*/
        if (mDatabase == null)
        {
            if (mainTmpDirSet == false)
            {
                String path = "/data/data/" + packageName + "/databases/tmpDir";
                LogUtils.e("boolean res1=" + new File(path).mkdirs());
                mDatabaseHelper.getWritableDatabase().execSQL("PRAGMA temp_store_directory = '"+path+"'");
            }
            else
                mainTmpDirSet = true;

            mDatabase = mDatabaseHelper.getWritableDatabase();
        }

        return mDatabase;
    }

    protected synchronized void closeDatabase()
    {
        /*if (mOpenCounter.decrementAndGet() == 0)
            mDatabase.close();*/
    }

    private static final class DBHelper extends SQLiteOpenHelper
    {
        public DBHelper(Context context, String name) { super(context, name, null, DATABASE_VERSION); }

        @Override
        public void onCreate(SQLiteDatabase db) { }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { }
    }
}