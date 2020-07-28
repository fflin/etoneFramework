package com.etone.framework.utils;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

/**
 * Toast统一管理类
 * 
 * @author zhuo
 * 
 */
public class EToast
{
	// Toast
	private Toast toast;
	private final Thread mainThread;

	private View defaultToastView;
	private int defaultGravity;
	private int defaultXOffset;
	private int defaultYOffset;

	private final Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			Object[] objs = (Object[]) msg.obj;
			String mesg = (String) objs[0];
			int duration = (int) objs[1];
			View view = (View) objs[2];
			setToastInfo(view, mesg, duration);
		}
	};

	private static final EToast instance = new EToast();

	private EToast()
	{
		mainThread = Thread.currentThread();
	}

	/*必须在主线程中进行初始化、否则会出事*/
	public static void init(Context context)
	{
		instance.toast = Toast.makeText(context, "", Toast.LENGTH_SHORT);
		instance.defaultToastView = instance.toast.getView();
		instance.defaultGravity = instance.toast.getGravity();
		instance.defaultXOffset = instance.toast.getXOffset();
		instance.defaultYOffset = instance.toast.getYOffset();
	}

	public static void show(String msg)
	{
		showWithDuration(msg, Toast.LENGTH_SHORT, instance.defaultToastView);
	}

	public static void show(String msg, int duration)
	{
		showWithDuration(msg, duration, instance.defaultToastView);
	}

	public static void showSayHi(Context context, String msg)
	{
		View view = getSayHiLayout(context, msg);
		showWithDuration(msg, Toast.LENGTH_SHORT, view);
	}

	private static void showWithDuration(String msg, int duration, View view)
	{
		if (instance.mainThread == Thread.currentThread())
			setToastInfo(view, msg, duration);
		else
		{
			Message message = Message.obtain();
			message.obj = new Object[]{msg, duration, view};
			instance.handler.sendMessage(message);
		}
	}

	private static void setToastInfo(View view, String msg, int duration)
	{
		if (instance.toast.getView() != view)
			instance.toast.setView(view);

		View cView = instance.toast.getView();
		if (cView == instance.defaultToastView)
		{
			instance.toast.setGravity(instance.defaultGravity, instance.defaultXOffset, instance.defaultYOffset);
			instance.toast.setText(msg);
		}
		else
		{
			instance.toast.setGravity(Gravity.CENTER, 0, 0);
		}

		instance.toast.setDuration(duration);
		instance.toast.show();
	}

	private static LinearLayout getSayHiLayout(Context context, String msg)
	{
		GradientDrawable backgroundDrawable = new GradientDrawable();
		backgroundDrawable.setColor(Color.parseColor("#99000000"));
		backgroundDrawable.setCornerRadius(DPUtils.dp2px(context, 10));

		LinearLayout defaultLayout = new LinearLayout(context);
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
		defaultLayout.setGravity(Gravity.CENTER);
		defaultLayout.setOrientation(LinearLayout.HORIZONTAL);
		int px = DPUtils.dp2px(context, 30);
		int px2 = DPUtils.dp2px(context, 20);
		defaultLayout.setPadding(px, px2, px, px2);
		defaultLayout.setLayoutParams(lp);
		defaultLayout.setMinimumHeight(DPUtils.dp2px(context, 100));
		defaultLayout.setMinimumWidth(DPUtils.dp2px(context, 100));
		defaultLayout.setBackgroundDrawable(backgroundDrawable);

		TextView textView = new TextView(context);
		LinearLayout.LayoutParams tvlp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		tvlp.gravity = Gravity.CENTER;
		textView.setTextColor(Color.WHITE);
		textView.setTextSize(16);
		textView.setText(msg);
		textView.setLayoutParams(tvlp);
		defaultLayout.addView(textView);


		ImageView imageView = new ImageView(context);
		imageView.setLayoutParams(tvlp);
		tvlp.setMargins(DPUtils.dp2px(context, 2), DPUtils.dp2px(context, 5), DPUtils.dp2px(context, 3), DPUtils.dp2px(context, 5));
		imageView.setLayoutParams(tvlp);
		Resources res = context.getResources();
		try
		{
			BitmapDrawable bd = new BitmapDrawable(res, res.getAssets().open("hello.png"));
			imageView.setImageBitmap(bd.getBitmap());
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
		defaultLayout.addView(imageView);

		return defaultLayout;

	}
}
