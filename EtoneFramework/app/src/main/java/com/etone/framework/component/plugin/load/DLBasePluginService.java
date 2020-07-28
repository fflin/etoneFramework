/*
 * Copyright (C) 2014 singwhatiwanna(任玉刚) <singwhatiwanna@gmail.com>
 *
 * collaborator:zhangjie1980(张杰)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.etone.framework.component.plugin.load;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;

import com.etone.framework.component.plugin.load.internal.DLPluginPackage;
import com.etone.framework.component.plugin.utils.DLConstants;
import com.etone.framework.utils.LogUtils;

public class DLBasePluginService extends Service implements DLServicePlugin {

    public static final String TAG = "DLBasePluginService";
    private Service mProxyService;
    private DLPluginPackage mPluginPackage;
    protected Service that = this;
    protected int mFrom = DLConstants.FROM_INTERNAL;

    public Service getThatService()
    {
        return that;
    }
    
    @Override
    public void attach(Service proxyService, DLPluginPackage pluginPackage) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " attach");
        mProxyService = proxyService;
        mPluginPackage = pluginPackage;
        that = mProxyService;
        mFrom = DLConstants.FROM_EXTERNAL;
    }
    
    protected boolean isInternalCall() {
        return mFrom == DLConstants.FROM_INTERNAL;
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onBind");
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onStartCommand");
        return 0;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onDestroy");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onConfigurationChanged");
    }

    @Override
    public void onLowMemory() {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onLowMemory");
    }

    @Override
    public void onTrimMemory(int level) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onTrimMemory");
        
    }

    @Override
    public boolean onUnbind(Intent intent) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onUnbind");
        return false;
    }

    @Override
    public void onRebind(Intent intent) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onRebind");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        // TODO Auto-generated method stub
        LogUtils.d(TAG + " onTaskRemoved");
    }

}
