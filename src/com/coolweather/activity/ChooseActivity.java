package com.coolweather.activity;

import java.util.ArrayList;
import java.util.List;

import com.coolweather.app.R;
import com.coolweather.db.CoolWeatherDB;
import com.coolweather.model.City;
import com.coolweather.model.Country;
import com.coolweather.model.Province;
import com.coolweather.util.HttpCallbackListener;
import com.coolweather.util.HttpUtil;
import com.coolweather.util.Utility;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ChooseActivity extends Activity {
	
	public static final int LEVEL_PROVINCE = 0;
	public static final int LEVEL_CITY = 1;
	public static final int LEVEL_COUNTRY = 2;
	
	private int currentLevel;

	private TextView titleText;
	private ListView listView;
	
	private ArrayAdapter<String> adapter;
	private CoolWeatherDB coolWeatherDB;
	private ProgressDialog progressDialog;
	
	private List<String> dataList = new ArrayList<String>();
	
	private List<Province> provinceList;         //省列表
	
	private List<City> cityList;          //市列表
	
	private List<Country> countryList;      //县列表
	
	private Province selecteProvince;      //选中的省
	
	private City selecteCity;     //选中的市
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choose_area);
		
		titleText = (TextView)findViewById(R.id.title_text);
		listView = (ListView)findViewById(R.id.list_view);
		initialize();
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// TODO Auto-generated method stub
				if(currentLevel == LEVEL_PROVINCE){
					selecteProvince = provinceList.get(position);
					queryCities();
				}else if(currentLevel == LEVEL_CITY){
					selecteCity = cityList.get(position);
					queryCountry();
				}
			}		
		});
		queryProvinces();
	}
	
	public void initialize(){
		adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, dataList);
		coolWeatherDB = CoolWeatherDB.getInstance(this);
	}

	
	//  查询全国所有的省，优先从数据库中查询，如果没有查询到再去服务器中查询
	private void queryProvinces(){
		provinceList = coolWeatherDB.loadProvinces();
		if(provinceList.size() > 0){
			dataList.clear();
			for(Province p : provinceList){
				dataList.add(p.getProvinceName());
			}
			adapter.notifyDataSetChanged();
			titleText.setText("中国");
			currentLevel = LEVEL_PROVINCE;
		}else{
			queryFromServer(null, "province");
		}
	}
	
	
	//  查询所选省市的所有城市，优先从数据库中查询， 如果没有查询到再去服务器中查询
	private void queryCities() {
		cityList = coolWeatherDB.loadCities(selecteProvince.getId());
		if(cityList.size() > 0){
			dataList.clear();
			for(City c : cityList){
				dataList.add(c.getCityName());
			}
			adapter.notifyDataSetChanged();
			titleText.setText(selecteProvince.getProvinceName());
			currentLevel = LEVEL_CITY;
		}else{
			queryFromServer(selecteProvince.getProvinceCode(), "city");
		}		
	}
	
	
	//  查询所选城市的所有县， 优先从数据库中查询， 如果没有查询到再去服务器中查询
	private void queryCountry(){
		countryList = coolWeatherDB.loadCountry(selecteCity.getId());
		if(countryList.size() > 0){
			dataList.clear();
			for(Country c : countryList){
				dataList.add(c.getCountryName());
			}
			adapter.notifyDataSetChanged();
			titleText.setText(selecteCity.getCityName());
			currentLevel = LEVEL_COUNTRY;
		}else{
			queryFromServer(selecteCity.getCityCode(), "country");
		}
	}
	
	/*
	 * queryFromServer   一直在执行
	 * */
	private void queryFromServer(final String code, final String type){
		String address;
		if(!TextUtils.isEmpty(code)){
			address = "http://www.weather.com.cn/data/list3/city" + code + ".xml";
//			Log.d("ChooseActivity", address);
		}else{
			address = "http://www.weather.com.cn/data/list3/city.xml";
		}
		showProgressDialog();
		HttpUtil.sendHttpRequest(address, new HttpCallbackListener() {
			
			@Override
			public void onFinish(String response) {
				// TODO Auto-generated method stub
				boolean result = false;
				if(type.equals("province")){
					result = Utility.handleProvinceResponse(coolWeatherDB, response);
				}else if(type.equals("city")){
					result = Utility.handleCitiesResponse(coolWeatherDB, response, selecteProvince.getId());
				}else if(type.equals("country")){
					result = Utility.handleCountriesResponse(coolWeatherDB, response, selecteCity.getId());
				}
				
				if(result == true){
					//通过  runOnUiThread()  方法回到主线程处理逻辑
					runOnUiThread(new Runnable() {
						
						@Override
						public void run() {
							closeProgressDialog();
							// TODO Auto-generated method stub
							if("province".equals(type)){
								queryProvinces();
							}else if("city".equals(type)){
								queryCities();
							}else if("country".equals(type)){
								queryCountry();
							}
						}
					});  					
				}
			}
			
			@Override
			public void onError(Exception e) {
				// TODO Auto-generated method stub
				runOnUiThread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						closeProgressDialog();
						Toast.makeText(ChooseActivity.this, "加载失败...", Toast.LENGTH_SHORT).show();
					}
				});
			}
		});
	}
	
	
	//显示进度条
	private void showProgressDialog(){
		if(progressDialog == null){
			progressDialog = new ProgressDialog(this);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage("正在加载...");
		}
		progressDialog.show();
	}
	
	//关闭进度条
	private void closeProgressDialog(){
		if(progressDialog != null){
			progressDialog.dismiss();
		}
	}
	
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		if(currentLevel == LEVEL_COUNTRY){
			queryCities();
		}else if(currentLevel == LEVEL_CITY){
			queryProvinces();
		}else{
			finish();
		}
	}

}
