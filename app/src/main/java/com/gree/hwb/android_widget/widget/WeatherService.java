package com.gree.hwb.android_widget.widget;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.services.weather.LocalDayWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecast;
import com.amap.api.services.weather.LocalWeatherForecastResult;
import com.amap.api.services.weather.LocalWeatherLive;
import com.amap.api.services.weather.LocalWeatherLiveResult;
import com.amap.api.services.weather.WeatherSearch;
import com.amap.api.services.weather.WeatherSearchQuery;
import com.gree.hwb.android_widget.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2016/08/29.
 */
public class WeatherService extends Service implements AMapLocationListener, WeatherSearch.OnWeatherSearchListener
{
	private Timer timer;
	private AMapLocationClient client;
	private AMapLocationClientOption aMapLocationClientOption;
	private String city = "珠海";
	private String district = "";
	private String weatherInfo;
	private WeatherSearchQuery query;

	@Nullable
	@Override
	public IBinder onBind(Intent intent)
	{
		return null;
	}

	@Override
	public void onCreate()
	{
		super.onCreate();
		timer = new Timer();
		timer.schedule(new TimerTask()
		{
			@Override
			public void run()
			{
				updateView();
				Log.i("zx", "updateView");
			}
		}, 0, 1000);
		//定位服务初始化
		initLocation();
		//获取实时天气
		searchLiveWeather();
		//获取预报天气
		searchForecastWeather();
	}

	private void initLocation()
	{
		client = new AMapLocationClient(getApplicationContext());
		aMapLocationClientOption = new AMapLocationClientOption();
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
		//设置是否返回地址信息（默认返回地址信息）
		aMapLocationClientOption.setNeedAddress(true);
		//设置是否只定位一次,默认为false
		aMapLocationClientOption.setOnceLocation(false);
		//设置是否强制刷新WIFI，默认为强制刷，如果为true则自动变化为单次定位，持续定位时不要使用
		//		aMapLocationClientOption.setWifiActiveScan(true);
		//设置是否允许模拟位置,默认为false，不允许模拟位置
		aMapLocationClientOption.setMockEnable(true);
		//设置定位间隔,单位毫秒,默认为2000ms
		aMapLocationClientOption.setInterval(2000);
		//给定位客户端对象设置定位参数
		client.setLocationOption(aMapLocationClientOption);
		//开始定位
		client.startLocation();
		client.setLocationListener(this);
	}

	//获取实时天气
	private void searchLiveWeather()
	{
		query = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_LIVE);
		WeatherSearch weatherSearch = new WeatherSearch(getApplicationContext());
		weatherSearch.setQuery(query);
		weatherSearch.setOnWeatherSearchListener(WeatherService.this);
		weatherSearch.searchWeatherAsyn();//异步搜索
	}

	//获取预报天气
	private void searchForecastWeather()
	{
		query = new WeatherSearchQuery(city, WeatherSearchQuery.WEATHER_TYPE_FORECAST);
		WeatherSearch weatherSearch = new WeatherSearch(getApplicationContext());
		weatherSearch.setQuery(query);
		weatherSearch.setOnWeatherSearchListener(WeatherService.this);
		weatherSearch.searchWeatherAsyn();//异步搜索
	}

	private void updateView()
	{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss E");
		String time = sdf.format(new Date());
		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
		rv.setTextViewText(R.id.tv_time, time);
		AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
		ComponentName cn = new ComponentName(getApplicationContext(), WeatherProvider.class);
		manager.updateAppWidget(cn, rv);
	}

	@Override
	public void onDestroy()
	{
		super.onDestroy();
		timer = null;
		client.onDestroy();
		client = null;
		aMapLocationClientOption = null;
		Log.i("zx", "onDestroy");
	}


	@Override
	public void onLocationChanged(AMapLocation aMapLocation)
	{
		StringBuffer sb = new StringBuffer();
		//定位回调
		if(aMapLocation != null && aMapLocation.getErrorCode() == 0)
		{
			sb.append("定位成功" + "\n");
			sb.append("定位类型    : " + aMapLocation.getLocationType() + "\n");
			sb.append("经    度    : " + aMapLocation.getLongitude() + "\n");
			sb.append("纬    度    : " + aMapLocation.getLatitude() + "\n");
			sb.append("精    度    : " + aMapLocation.getAccuracy() + "米" + "\n");
			sb.append("提供者      : " + aMapLocation.getProvider() + "\n");

			if(aMapLocation.getProvider().equalsIgnoreCase(android.location.LocationManager.GPS_PROVIDER))
			{
				// 以下信息只有提供者是GPS时才会有
				sb.append("速    度    : " + aMapLocation.getSpeed() + "米/秒" + "\n");
				sb.append("角    度    : " + aMapLocation.getBearing() + "\n");
				// 获取当前提供定位服务的卫星个数
				sb.append("星    数    : " + aMapLocation.getSatellites() + "\n");
			}
			else
			{
				city = aMapLocation.getCity();
				district = aMapLocation.getDistrict();
				// 提供者是GPS时是没有以下信息的
				sb.append("国    家    : " + aMapLocation.getCountry() + "\n");
				sb.append("省            : " + aMapLocation.getProvince() + "\n");
				sb.append("市            : " + aMapLocation.getCity() + "\n");
				sb.append("城市编码 : " + aMapLocation.getCityCode() + "\n");
				sb.append("区            : " + aMapLocation.getDistrict() + "\n");
				sb.append("区域 码   : " + aMapLocation.getAdCode() + "\n");
				sb.append("地    址    : " + aMapLocation.getAddress() + "\n");
				sb.append("兴趣点    : " + aMapLocation.getPoiName() + "\n");
				//定位完成的时间
				sb.append("定位时间: " + formatUTC(aMapLocation.getTime(), "yyyy-MM-dd HH:mm:ss:sss") + "\n");
			}
		}
		else
		{
			//定位失败
			sb.append("定位失败" + "\n");
			sb.append("错误码:" + aMapLocation.getErrorCode() + "\n");
			sb.append("错误信息:" + aMapLocation.getErrorInfo() + "\n");
			sb.append("错误描述:" + aMapLocation.getLocationDetail() + "\n");
			Toast.makeText(getApplicationContext(), "获取定位失败" + aMapLocation.getErrorCode(), Toast.LENGTH_SHORT).show();
		}

		RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
		rv.setTextViewText(R.id.tv_address, sb.toString() + city + district);
		AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
		ComponentName cn = new ComponentName(getApplicationContext(), WeatherProvider.class);
		manager.updateAppWidget(cn, rv);
	}

	private static SimpleDateFormat sdf = null;

	public synchronized static String formatUTC(long l, String strPattern)
	{
		if(TextUtils.isEmpty(strPattern))
		{
			strPattern = "yyyy-MM-dd HH:mm:ss";
		}
		if(sdf == null)
		{
			try
			{
				sdf = new SimpleDateFormat(strPattern, Locale.CHINA);
			}
			catch (Throwable e)
			{
			}
		}
		else
		{
			sdf.applyPattern(strPattern);
		}
		if(l <= 0l)
		{
			l = System.currentTimeMillis();
		}
		return sdf == null ? "NULL" : sdf.format(l);
	}

	//实时天气回调
	@Override
	public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int rCode)
	{
		if(rCode == 1000)
		{
			if(localWeatherLiveResult != null && localWeatherLiveResult.getLiveResult() != null)
			{
				LocalWeatherLive liveWeather = localWeatherLiveResult.getLiveResult();
				Log.i("zx", "liveWeather:" + localWeatherLiveResult.toString());
				String city = liveWeather.getCity();
				String weather = liveWeather.getWeather();//天气情况
				String windDir = liveWeather.getWindDirection();//风向
				String windPower = liveWeather.getWindPower();//风力
				String humidity = liveWeather.getHumidity();//空气湿度
				String reportTime = liveWeather.getReportTime();//更新时间
				String temp = liveWeather.getTemperature();//温度
				weatherInfo = weather + " " + temp + "℃" + " 风力" + windPower + "级";
				RemoteViews rv = new RemoteViews(getPackageName(), R.layout.widget_layout);
				rv.setTextViewText(R.id.tv_weatherInfo, weatherInfo);
				AppWidgetManager manager = AppWidgetManager.getInstance(getApplicationContext());
				ComponentName cn = new ComponentName(getApplicationContext(), WeatherProvider.class);
				manager.updateAppWidget(cn, rv);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "获取天气失败" + rCode, Toast.LENGTH_SHORT).show();
		}
	}

	//未来三天天气
	@Override
	public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int rCode)
	{
		if(rCode == 1000)
		{
			if(localWeatherForecastResult != null && localWeatherForecastResult.getForecastResult() != null && localWeatherForecastResult.getForecastResult().getWeatherForecast() != null && localWeatherForecastResult.getForecastResult().getWeatherForecast().size() > 0)
			{
				LocalWeatherForecast weatherForecast = localWeatherForecastResult.getForecastResult();
				List<LocalDayWeatherForecast> dayWeatherList = weatherForecast.getWeatherForecast();
				String forestMsgInString = "";
				for(int j = 0; j < dayWeatherList.size(); j++)
				{
					forestMsgInString += dayWeatherList.get(j).getDate() + "\n";
					forestMsgInString += dayWeatherList.get(j).getDayWeather() + "\n";
					forestMsgInString += dayWeatherList.get(j).getDayTemp() + "\n";
				}
				Log.i("zx", forestMsgInString);
			}
			else
			{
				Toast.makeText(getApplicationContext(), "对不起，没有搜索到相关数据！", Toast.LENGTH_SHORT).show();
			}
		}
		else
		{
			Toast.makeText(getApplicationContext(), "获取天气失败" + rCode, Toast.LENGTH_SHORT).show();
		}
	}
}
