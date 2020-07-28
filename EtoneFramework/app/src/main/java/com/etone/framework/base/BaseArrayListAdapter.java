package com.etone.framework.base;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.etone.framework.annotation.InjectUtils;
import com.etone.framework.component.db.Table;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/7/15.
 */
public abstract class BaseArrayListAdapter<E> extends BaseAdapter
{
    public LayoutInflater inflater = null;
    public ArrayList<E> resource = null;
    private Context context = null;
    public int layout = 0;
    private boolean wantNewHolder;

    public BaseArrayListAdapter(Context context, ArrayList<E> resource, boolean wantNewHolder)
    {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.resource = resource;
        this.wantNewHolder = wantNewHolder;
        this.layout = InjectUtils.getAdapterInjectLayout(this);
    }

    @Override
    public int getCount()
    {
        ArrayList<E> list = this.resource;
        return list == null ? 0 : list.size();
    }

    @Override
    public E getItem(int position)
    {
        ArrayList<E> list = this.resource;
        return list == null ? null : list.get(position);
    }

    public void setData(ArrayList<E> list)
    {
        this.resource = list;
    }

    public void addData(ArrayList<E> list)
    {
        synchronized (this)
        {
            for (int i = 0; i < list.size(); i++)
                this.resource.add(list.get(i));
        }

        this.notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView (int position, View convertView, ViewGroup parent)
    {
        E item = resource.get(position);

        BaseHolder holder;
        if (convertView == null || wantNewHolder)
        {
            /*在这里做代码注入操作*/
            convertView = inflater.inflate(layout, null);
            holder = InjectUtils.injectAdapterGetView(this.getClass(), convertView);
            convertView.setTag(holder);
        }
        else
        {
            holder = (BaseHolder) convertView.getTag();
        }

        initHolderData(holder, position, item);

        return convertView;
    }

    public abstract void initHolderData(BaseHolder holder, int position, E item);

    public interface BaseHolder
    {

    }
}
