/*
 * Copyright (C) 2014 singwhatiwanna(任玉刚) <singwhatiwanna@gmail.com>
 *
 * collaborator:田啸,宋思宇,Mr.Simple
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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;

import com.etone.framework.component.plugin.load.internal.DLAttachable;
import com.etone.framework.component.plugin.load.internal.DLPluginManager;
import com.etone.framework.component.plugin.load.internal.DLProxyImpl;
import com.etone.framework.event.EventBus;
import com.etone.framework.utils.LogUtils;

public class DLProxyActivity extends Activity implements DLAttachable {

    protected DLPlugin mRemoteActivity;
    private DLProxyImpl impl = new DLProxyImpl(this);

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        try
        {
            super.onCreate(savedInstanceState);
            impl.onCreate(getIntent());
        }
        catch (Throwable e)
        {
            LogUtils.e("catch Fragment error");
            EventBus.onPostReceived("APP_ERROR_EXIT", null);
            Toast.makeText(this, "宝宝迷路了，重启一下吧", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            this.finish();
        }
    }

    @Override
    public void attach(DLPlugin remoteActivity, DLPluginManager pluginManager) {
        mRemoteActivity = remoteActivity;
    }

    @Override
    public AssetManager getAssets() {
        return impl.getAssets() == null ? super.getAssets() : impl.getAssets();
    }

    @Override
    public Resources getResources()
    {
        Resources res = impl.getResources() == null ? super.getResources() : impl.getResources();

        Configuration config = new Configuration();
        config.setToDefaults();
        res.updateConfiguration(config,res.getDisplayMetrics());

        return res;
    }

    @Override
    public Theme getTheme() {
        return impl.getTheme() == null ? super.getTheme() : impl.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        return impl.getClassLoader();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (mRemoteActivity == null)
        {
            super.onActivityResult(requestCode, resultCode, data);
            return;
        }
        mRemoteActivity.onActivityResult(requestCode, resultCode, data);
        if (mRemoteActivity.callSuperActivityResult())
            super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        if (mRemoteActivity != null)
            mRemoteActivity.onStart();
        super.onStart();
    }

    @Override
    protected void onRestart()
    {
        if (mRemoteActivity != null)
            mRemoteActivity.onRestart();
        super.onRestart();
    }

    @Override
    protected void onResume() {
        if (mRemoteActivity != null)
            mRemoteActivity.onResume();
        super.onResume();
    }

    @Override
    protected void onPause() {
        if (mRemoteActivity != null)
            mRemoteActivity.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        if (mRemoteActivity != null)
            mRemoteActivity.onStop();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if (mRemoteActivity != null)
            mRemoteActivity.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (mRemoteActivity != null)
            mRemoteActivity.onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (mRemoteActivity != null)
            mRemoteActivity.onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (mRemoteActivity != null)
            mRemoteActivity.onNewIntent(intent);
        super.onNewIntent(intent);
    }

    @Override
    public void onBackPressed()
    {
        if (mRemoteActivity == null)
        {
            super.onBackPressed();
            return;
        }
        mRemoteActivity.onBackPressed();
        if (mRemoteActivity.callSuperBackPressed())
            super.onBackPressed();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event)
    {
        boolean flag = super.onTouchEvent(event);
        if (mRemoteActivity == null)
            return flag;
        return mRemoteActivity.onTouchEvent(event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event)
    {
        boolean flag = super.onKeyUp(keyCode, event);
        if (mRemoteActivity == null)
            return flag;
        return mRemoteActivity.onKeyUp(keyCode, event);
    }

    @Override
    public void onWindowAttributesChanged(LayoutParams params)
    {
        if (mRemoteActivity != null)
            mRemoteActivity.onWindowAttributesChanged(params);
        super.onWindowAttributesChanged(params);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (mRemoteActivity != null)
            mRemoteActivity.onWindowFocusChanged(hasFocus);
        super.onWindowFocusChanged(hasFocus);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (mRemoteActivity != null)
            mRemoteActivity.onCreateOptionsMenu(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mRemoteActivity != null)
            mRemoteActivity.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    public ComponentName startService(Intent service) {
        return super.startService(service);
    }
}
