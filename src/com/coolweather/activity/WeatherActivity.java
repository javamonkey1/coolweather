package com.coolweather.activity;

import com.coolweather.app.R;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WeatherActivity extends Activity {
	
	private TextView cityNameText;      //用于显示城市名
	private TextView publishText;       //用于显示发布时间
	private TextView currentDateText;   //用于显示当前的日期
	private TextView weatherDespText;   //用于显示天气描述信息
	private TextView temp1Text;         //用于显示最低气温
	private TextView temp2Text;         //用于显示最高气温
	private LinearLayout weatherInfoLayout;        

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.weather_layout);
		
		cityNameText = (TextView)findViewById(R.id.city_name);
		publishText = (TextView)findViewById(R.id.publish_text);
		currentDateText = (TextView)findViewById(R.id.current_date);
		weatherDespText = (TextView)findViewById(R.id.weather_desp);
		temp1Text = (TextView)findViewById(R.id.temp2);
		temp2Text = (TextView)findViewById(R.id.temp1);
		weatherInfoLayout = (LinearLayout)findViewById(R.id.weather_info_layout);
		
		String countryCode = getIntent().getStringExtra("country_code");
		if(!TextUtils.isEmpty(countryCode)){
			publishText.setText("同步中...");
			cityNameText.setVisibility(View.INVISIBLE);
			weatherInfoLayout.setVisibility(View.INVISIBLE);
			queryWeatherCode(countryCode);
		}else{
			showWeather();
		}
	}
	
	/*	通过县级code 查询到对应的天气代号	*/
	public void queryWeatherCode(String countryCode){
		String address = "http://www.weather.com.cn/data/list3/city" + countryCode + ".xml";
		queryFromServer(address, "countryCode");
	}
	
	/*	通过天气代号查询对应的天气	*/
	public void queryWeatherInfo(String weatherCode){
		String address = "http://www.weather.com.cn/data/cityinfo/" + weatherCode + ".html";
		queryFromServer(address, "weatherCode");
	}
	
	/*	根据传入的地址和类型向服务器查询天气代号或者天气信息	*/
	public void queryFromServer(final String address, final String type){
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				
				if("countryCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						String[] str = response.split("\\|");
						if(str != null && str.length == 2){
							String weatherCode = str[1];
							queryWeatherInfo(weatherCode);
						}
					}
				}else if("weatherCode".equals(type)){
					if(!TextUtils.isEmpty(response)){
						Log.d("WeatherActivity", response);
						Utility.handleWeatherResponse(WeatherActivity.this, response);
						runOnUiThread(new Runnable() {
							public void run() {
								showWeather();
							}
						});
					}
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					public void run() {
						publishText.setText("同步失败...");
					}
				});
			}
		});
	}
	
	/*	从 sharePreferences 文件中读取存储的天气信息， 并显示在界面上	*/
	public void showWeather(){
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		weatherInfoLayout.setVisibility(View.VISIBLE);
		cityNameText.setVisibility(View.VISIBLE);
		cityNameText.setText(prefs.getString("city_name", ""));
		publishText.setText("今天" + prefs.getString("publish_time", "") + "发布");
		currentDateText.setText(prefs.getString("current_time", ""));
		weatherDespText.setText(prefs.getString("weather_desp", ""));
		temp1Text.setText(prefs.getString("temp1", ""));
		temp2Text.setText(prefs.getString("temp2", ""));
	}
}
