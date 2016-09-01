package com.gree.hwb.android_widget.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

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

import java.util.List;

public class MainActivity extends AppCompatActivity implements AMapLocationListener,WeatherSearch.OnWeatherSearchListener
{

	private AMapLocationClient client;
	private AMapLocationClientOption aMapLocationClientOption;
	private TextView address,weatherInfo;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		address = (TextView) findViewById(R.id.tv_address);
		weatherInfo = (TextView) findViewById(R.id.tv_weatherInfo);

		client = new AMapLocationClient(this.getApplicationContext());
		aMapLocationClientOption = new AMapLocationClientOption();
		//设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
		aMapLocationClientOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);

		//设置是否返回地址信息（默认返回地址信息）
		aMapLocationClientOption.setNeedAddress(true);
		//设置是否只定位一次,默认为false
		aMapLocationClientOption.setOnceLocation(true);
		//设置是否强制刷新WIFI，默认为强制刷新
		aMapLocationClientOption.setWifiActiveScan(true);
		//设置是否允许模拟位置,默认为false，不允许模拟位置
		aMapLocationClientOption.setMockEnable(true);
		//设置定位间隔,单位毫秒,默认为2000ms
		aMapLocationClientOption.setInterval(2000);

		//给定位客户端对象设置定位参数
		client.setLocationOption(aMapLocationClientOption);
		//开始定位
		client.startLocation();
		client.setLocationListener(this);

		WeatherSearchQuery query = new WeatherSearchQuery("珠海", WeatherSearchQuery.WEATHER_TYPE_FORECAST);//查询未来三天天气
		WeatherSearch weatherSearch = new WeatherSearch(getApplicationContext());
		weatherSearch.setQuery(query);
		weatherSearch.setOnWeatherSearchListener(MainActivity.this);
		weatherSearch.searchWeatherAsyn();
	}
	@Override
	protected void onDestroy()
	{
		super.onDestroy();
		client.onDestroy();
	}

	@Override
	public void onLocationChanged(AMapLocation aMapLocation)
	{
		//定位回调
		if(aMapLocation != null && aMapLocation.getErrorCode() == 0)
		{
			address.setText(aMapLocation.getAddress());
		}
		else
		{
			address.setText("获取定位失败");
		}
	}

	//当天天气
	@Override
	public void onWeatherLiveSearched(LocalWeatherLiveResult localWeatherLiveResult, int rCode)
	{
		if(rCode == 1000)
		{
			LocalWeatherLive liveWeather = localWeatherLiveResult.getLiveResult();
			Log.i("zx", liveWeather.toString());
		}
		else
		{
			Log.i("zx", "查询天气失败");
		}
	}

	//未来天气
	@Override
	public void onWeatherForecastSearched(LocalWeatherForecastResult localWeatherForecastResult, int i)
	{
		LocalWeatherForecast weatherForecast = localWeatherForecastResult.getForecastResult();
		List<LocalDayWeatherForecast> dayWeatherList = weatherForecast.getWeatherForecast();
		String forestMsgInString = "";
		for(int j = 0; j < dayWeatherList.size(); j++)
		{
			forestMsgInString += dayWeatherList.get(j).getDate() + "\n";
			forestMsgInString += "白天风力 " + dayWeatherList.get(j).getDayWindPower() + "级" + "\n";
//			forestMsgInString += "白天风向 " + dayWeatherList.get(j).getDayWindDirection() + "\n";
			forestMsgInString += dayWeatherList.get(j).getDayWeather() + "\n";
			forestMsgInString += String.format("%-3s~%3s",dayWeatherList.get(j).getDayTemp(),dayWeatherList.get(j).getNightTemp()) + "℃" + "\n";
		}
		weatherInfo.setText(forestMsgInString);
		Log.i("zx",forestMsgInString);
	}
}
