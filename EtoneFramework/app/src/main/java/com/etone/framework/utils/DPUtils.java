package com.etone.framework.utils;

import android.content.Context;

/**
 * Created by Administrator on 2016/8/23.
 */
public class DPUtils
{
    public static int dp2px(Context context, float dipValue)
    {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
