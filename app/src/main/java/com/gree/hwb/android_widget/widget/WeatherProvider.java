package com.gree.hwb.android_widget.widget;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Administrator on 2016/08/29.
 */
public class WeatherProvider extends AppWidgetProvider
{

	//widget从屏幕中移除
	@Override
	public void onDeleted(Context context, int[] appWidgetIds)
	{
		super.onDeleted(context, appWidgetIds);
		context.stopService(new Intent(context,WeatherService.class));
		Log.i("zx","stopService");
	}

	//最后一个widget被从屏幕移除
	@Override
	public void onDisabled(Context context)
	{
		super.onDisabled(context);
	}

	//第一个widget添加到屏幕上
	@Override
	public void onEnabled(Context context)
	{
		super.onEnabled(context);
		context.startService(new Intent(context,WeatherService.class));
		Log.i("zx","startService");
	}

	@Override
	public void onReceive(Context context, Intent intent)
	{
		super.onReceive(context, intent);
	}

	//刷新widget  通过remoteViews和AppWidgetManager
	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds)
	{
		super.onUpdate(context, appWidgetManager, appWidgetIds);
	}
}
