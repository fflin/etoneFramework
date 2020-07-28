package com.etone.framework.base;

import android.content.Context;
import android.view.View;
import android.widget.AbsListView;
import android.widget.GridView;
import android.widget.ListView;

import com.etone.framework.utils.LogUtils;

import java.util.ArrayList;

/**
 * Created by Administrator on 2016/9/20.
 */
public abstract class ItemRefreshListAdapter extends BaseArrayListAdapter
{
    private AbsListView parent;
    public ItemRefreshListAdapter(Context context, ArrayList resource, AbsListView parent, boolean wantNewHolder)
    {
        super(context, resource, wantNewHolder);
        this.parent = parent;
    }

    @Override
    public Object getItem(int position)
    {
        return super.getItem(position);
    }

    public void updateItem(Object item)
    {
        if (resource == null || resource.size() == 0)
            return;

        int position = -1;
        Object res = null;
        for (int i=0; i<resource.size(); i++)
        {
            res = resource.get(i);
            if (itemCompare(res, item))
            {
                position = i;
                break;
            }
        }

        if (position == -1) //not found
            return;

        itemReset(res, item);
        updateUI(position, res);
    }

    private void updateUI(int position, Object item)
    {
        int visiblePosition = parent.getFirstVisiblePosition();
        View view = parent.getChildAt(position - visiblePosition);
        LogUtils.e("position-visiblePosition=" + (position-visiblePosition));
        if (view == null)
            return;
        BaseHolder holder = (BaseHolder) view.getTag();
        if (holder == null)
            return;

        refresh(holder, position, item);
    }

    public abstract boolean itemCompare(Object item1, Object item2);
    public abstract void itemReset(Object item1, Object item2);
    public abstract void refresh(BaseHolder holder, int position, Object item);
}
