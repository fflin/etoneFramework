package com.etone.framework.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

/**
 * Created by zhuo on 2016/3/10.
 */
public class ChannelUtils {

    public static String getChannelCode(Context context) {

        String code = getMetaData(context, "UMENG_CHANNEL");

        if (code != null) {

            return code;

        }

        return "0";

    }



    private static String getMetaData(Context context, String key) {

        try {

            ApplicationInfo ai = context.getPackageManager().getApplicationInfo(

                    context.getPackageName(), PackageManager.GET_META_DATA);

            Object value = ai.metaData.get(key);

            if (value != null) {

                return value.toString();

            }

        } catch (Exception e) {

            //

        }

        return null;

    }

}
