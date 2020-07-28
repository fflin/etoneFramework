package com.etone.framework.component.db;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import com.etone.framework.annotation.PrimaryKey;
import com.etone.framework.utils.LogUtils;
import com.etone.framework.utils.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.ArrayList;

/*需要优化的地方是循环访问、把iterator换成for*/
public class Table
{
    /*表面前面加一个前缀，以防出现关键字*/
    public static final String TABLE_DEFAULT_PERFIX = "t_";

    private SQLiteUtils instance;
    private Class<? extends Model> model;

    public String name;

    public Field[] fields;

    //主键在new这个table的时候就已经被赋值了，如果为空，说明没有用户定义的主键
    private Field primaryKey;

    public Table(SQLiteUtils instance, Class<? extends Model> model)
    {
        this.primaryKey = null;
        this.instance = instance;
        this.model = model;
        this.name = TABLE_DEFAULT_PERFIX + model.getSimpleName();
        Field[] fieldMap = this.fields = new Field[]{};
        String fieldName;
        Annotation annotation;
        ArrayList<Field> list = new ArrayList<Field>();
        try
        {
            Field[] fields = model.getFields();
            for (int i = 0; i < fields.length; i++)
            {
                fieldName = fields[i].getName();
                if (fieldName.contains("$"))
                    continue;

                fields[i].setAccessible(true);
                list.add(fields[i]);

                if (this.primaryKey == null)
                {
                    annotation = fields[i].getAnnotation(PrimaryKey.class);
                    if (annotation != null)
                    {
                        this.primaryKey = fields[i];
                    }
                }
            }
            this.fields = list.toArray(fieldMap);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        createTableIfNotExists();
    }

    private void createTableIfNotExists()
    {
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE TABLE IF NOT EXISTS ").append(name).append(" (");
        Field[] fields = this.fields;
        Field field;

        String key;
        Class<?> type;
        boolean hasPrimaryKey = false;
        String tmpSql;
        for (int i=0; i<fields.length; i++)
        {
            field = fields[i];
            key = field.getName();
            type = field.getType();
            if (!hasPrimaryKey)
            {
                Annotation pk = field.getAnnotation(PrimaryKey.class);
                if (pk != null)
                {
                    tmpSql = "`" + key + "` " + SQLiteUtils.TYPE_MAP.get(type).toString() + " PRIMARY KEY NOT NULL, ";
                    hasPrimaryKey = true;
                }
                else
                    tmpSql = "`" + key + "` " + SQLiteUtils.TYPE_MAP.get(type).toString() + ", ";
            }
            else
                tmpSql = "`" + key + "` " + SQLiteUtils.TYPE_MAP.get(type).toString() + ", ";

            if (key.equals("mId"))
                continue;
            sb.append(tmpSql);
        }
        if (!hasPrimaryKey)
            sb.append("mId INTEGER PRIMARY KEY NOT NULL, ");
        sb.setLength(sb.length() - 2);
        sb.append(")");
        LogUtils.e(sb.toString());

        instance.queryFactory(sb.toString());
    }


    public Long saveModel(Model model)
    {
        final ContentValues values;
        Long id = model.mId;

        try
        {
            /*如果primaryKey不为空，说明用户有定义主键*/
            values = getContentValue(model, primaryKey != null);
            SQLiteDatabase db = instance.openDatabase();
            id = db.replace(this.name, null, values);

            if (primaryKey == null)
                model.mId = id;

            /*if (id == null)
            {
                id = db.insert(this.name, null, values);
                LogUtils.e("insert");
            }
            else
            {
                if (primaryKey != null)
                {
                    Object value = primaryKey.get(model);
                    db.replace()
                    db.update(this.name, values, primaryKey.getName() + "=" + value, null);
                }
                else
                {
                    db.update(this.name, values, "mId=" + model.mId, null);
                }

                LogUtils.e("update");
            }*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        finally
        {
            instance.closeDatabase();
        }

        return id;
    }

    public void deleteModel(Model model)
    {
        try
        {
            String sql = "delete from " + this.name + " where ";
            if (primaryKey != null)
            {
                Object value = primaryKey.get(model);
                sql = sql + primaryKey.getName() + "='" + value + "'";
            }
            else
            {
                sql = sql + "mId='" + model.mId + "'";
            }
            instance.queryFactory(sql);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    public void clear()
    {
        String sql = "delete from " + this.name;
        instance.queryFactory(sql);
    }

    /*在模型内的查询,需要表结构的支持*/
    public ArrayList<?> select(String where)
    {
        StringBuilder sql = new StringBuilder("select * from " + this.name + " where 1=1 ");
        if (!StringUtils.isEmpty(where))
            sql.append("and ").append(where);
        LogUtils.e(sql.toString());

        return instance.selectFactory(sql.toString(), this.fields, this.model);
    }

    public int count(String where)
    {
        StringBuilder sql = new StringBuilder("select count(*) as num from " + this.name + " where 1=1 ");
        if (!StringUtils.isEmpty(where))
            sql.append("and ").append(where);
        LogUtils.e(sql.toString());

        return instance.countQuery(sql.toString());
    }

    private ContentValues getContentValue(Object object, boolean hasPrimaryKey) throws Exception
    {
        Field[] fields = this.fields;
        final ContentValues values = new ContentValues();
        Field field;
        String fieldName;
        Class<?> fieldType;

        for (int i=0; i<fields.length; i++)
        {
            field = fields[i];
            fieldName = field.getName();
            Object value = field.get(object);
            if (fieldName.contains("$"))
                continue;

            /*如果一个表里面有自己的主键，那么mId的值要被忽略掉*/
            if (hasPrimaryKey && fieldName.equals("mId"))
                continue;

            fieldType = field.getType();
            if (value == null)
                values.putNull(fieldName);
            else if (fieldType.equals(Byte.class) || fieldType.equals(byte.class)) {
                values.put(fieldName, (Byte) value);
            }
            else if (fieldType.equals(Short.class) || fieldType.equals(short.class)) {
                values.put(fieldName, (Short) value);
            }
            else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
                values.put(fieldName, (Integer) value);
            }
            else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
                values.put(fieldName, (Long) value);
            }
            else if (fieldType.equals(Float.class) || fieldType.equals(float.class)) {
                values.put(fieldName, (Float) value);
            }
            else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
                values.put(fieldName, (Double) value);
            }
            else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
                values.put(fieldName, (Boolean) value);
            }
            else if (fieldType.equals(Character.class) || fieldType.equals(char.class)) {
                values.put(fieldName, value.toString());
            }
            else if (fieldType.equals(String.class)) {
                values.put(fieldName, value.toString());
            }
            else if (fieldType.equals(String[].class))
            {
                StringBuilder sb = new StringBuilder();
                String[] res = (String[]) value;
                for (int h=0; h<res.length; h++)
                    sb.append(res[h]).append("HXH");
                if (sb.length() > 3)
                    sb.setLength(sb.length() - 3);
                else
                    sb.setLength(0);
                values.put(fieldName, sb.toString());
            }
            else if (fieldType.equals(Byte[].class) || fieldType.equals(byte[].class)) {
                values.put(fieldName, (byte[]) value);
            }
        }

        return values;
    }
}
