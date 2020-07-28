package com.etone.framework.component.db;

import com.etone.framework.utils.LogUtils;
import com.etone.framework.utils.StringUtils;

import org.json.JSONObject;

import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class Model
{
    /*数据库中的主键，目前仅支持自己的主键*/
    public Long mId;

    /*这个类对应的表的名称*/
    private String tableName;
    public String dbName;

    /*在new这个类的时候，就要先把table的信息从缓存中找出来*/
    private Table table;

    public Model() {
        this.mId = null;
        if (StringUtils.isEmpty(tableName))
            tableName = this.getClass().getSimpleName();

        getAnnotationDbName();

        /*如果之前没有获取过table的话，那么就获取一下它的instance*/
        if (table == null) {
            SQLiteUtils instance = SQLiteUtils.getDatabase(dbName);
            //如果有的Model不需要入库，那么table就只能为空了
            if (instance == null) {
                table = null;
                LogUtils.e("table == null");
                //throw new NullPointerException("no such database. have you declared the annotation on the model?");
            } else
                table = instance.findTableByName(tableName);
        }
    }

    private void getAnnotationDbName()
    {
        Class<?> clz = this.getClass();
        /*如果没有这个注解，那么不允许它继续运行，因为这是链接数据库和model的必经之路*/
        String database = SQLiteUtils.getDbNameCache(clz);

        /*如果要是有的类不需要数据库，那么可以不用这样*/
        if (database == null)
            return;
            //throw new NullPointerException("no such database. have you declared the annotation on the model?");

        this.dbName = database;
    }

    private void setTableValue()
    {
        if (dbName != null && tableName != null)
        {
            SQLiteUtils instance = SQLiteUtils.getDatabase(dbName);
            if (instance != null)
            {
                table = instance.findTableByName(tableName);
                if (table != null)
                    return;
            }
        }

        throw new NullPointerException("no such database. have you declared the annotation on the model?");
    }

    /*这个方法保留给框架使用, 从数据库读取数据的时候会用到它*/
    public Model(Long id)
    {
        this.mId = id;
    }

    /*获取当前表的版本号，如果版本号比之前的版本号大，那么执行更新表结构的操作*/
    public abstract int getVersion();

    public Table getTable()
    {
        if (table == null)
            setTableValue();

        return table;
    }

    public void save()
    {
        if (table == null)
        {
            LogUtils.e("dbName:" + dbName);
            LogUtils.e("tableName:" + tableName);
            setTableValue();
        }

        table.saveModel(this);
    }
    
    public void delete()
    {
    	if (table == null)
            setTableValue();

        //mId为空说明当前此条记录还没有入库，所以不需要进行数据库操作
        if (this.mId == null)
            return;

    	table.deleteModel(this);
    }

    public ArrayList<?> find(String where)
    {
        if (table == null)
            setTableValue();

        return table.select(where);
    }

    public ArrayList<?> findAll()
    {
        return find("");
    }

    public JSONObject toJson()
    {
        return JsonConvertUtils.toJson(this);
    }

    public void fromJson(String json)
    {
        JsonConvertUtils.fromJson(json, this);
    }

    public String toJsonStr()
    {
        return toJson().toString();
    }

    public String toString()
    {
        StringBuilder sb = new StringBuilder();
        Class<?> clzType = this.getClass();
        sb.append("Model->").append(clzType.getSimpleName()).append("|||");
        Field[] fields = clzType.getDeclaredFields();
        Field field;
        String fieldName;
        try
        {
            for (int i = 0; i < fields.length; i++)
            {
                field = fields[i];
                fieldName = field.getName();
                if (fieldName.contains("$"))
                    continue;

                sb.append(fieldName).append(":").append(field.get(this)).append(", ");
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return sb.toString();
    }
}