package com.etone.framework.utils;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

/**
 * Created by Administrator on 2016/7/29.
 */
public class ListViewUtils
{
    public static void setListViewHeightBasedOnChildren(ListView listView)
    {
        ListAdapter listAdapter = listView.getAdapter();

        if (listAdapter == null)
            return;

        int totalHeight = 0;
        int count = listAdapter.getCount();
        View listItem;
        for (int i=0; i<count; i++)
        {
            listItem = listAdapter.getView (i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (count-1));
        listView.setLayoutParams(params);
    }
}
